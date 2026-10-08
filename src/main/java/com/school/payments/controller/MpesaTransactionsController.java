package com.school.payments.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.model.MpesaTransactionRequest;
import com.school.payments.model.MpesaTransactionsResponse;
import com.school.payments.repository.MpesaTransactionsRepository;
import com.school.payments.service.MpesaStkPushQuery;
import com.school.payments.service.MpesaTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * No @CrossOrigin here: CORS is configured once, globally, in SecurityConfig.
 */
@RestController
@Slf4j
public class MpesaTransactionsController {

    private static final Map<String, Object> ACK = Map.of("ResultCode", 0, "ResultDesc", "Accepted");

    private final MpesaTransactionService mpesaTransactionService;
    private final MpesaTransactionsRepository transactionsRepository;
    private final MpesaStkPushQuery queryService;
    private final String callbackSecret;
    private final List<String> allowedCallbackIps;

    public MpesaTransactionsController(
            MpesaTransactionService mpesaTransactionService,
            MpesaTransactionsRepository transactionsRepository,
            MpesaStkPushQuery queryService,
            @Value("${mpesa.callback.secret}") String callbackSecret,
            // Optional. Take the current list from Safaricom's Daraja docs.
            // Behind a reverse proxy, make sure getRemoteAddr() is the real client IP.
            @Value("${mpesa.callback.allowed-ips:}") List<String> allowedCallbackIps) {
        if (callbackSecret == null || callbackSecret.length() < 24) {
            throw new IllegalStateException("mpesa.callback.secret must be set and at least 24 characters");
        }
        this.mpesaTransactionService = mpesaTransactionService;
        this.transactionsRepository = transactionsRepository;
        this.queryService = queryService;
        this.callbackSecret = callbackSecret;
        this.allowedCallbackIps = allowedCallbackIps;
    }

    @PostMapping("/stkPush")
    public ResponseEntity<MpesaTransactionsResponse> initiateStkPush(
            @RequestBody MpesaTransactionRequest transactionRequest,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {
        MpesaTransactionsResponse response = mpesaTransactionService
                .initiateStkPush(transactionRequest, idempotencyKey, authentication);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Register this full URL (with the secret) as the CallbackURL when you call Daraja:
     * https://your-host/stk/callback/{mpesa.callback.secret}
     */
    @PostMapping("/stk/callback/{secret}")
    @Transactional
    public ResponseEntity<Map<String, Object>> callback(@PathVariable String secret,
                                                        @RequestBody JsonNode payload,
                                                        HttpServletRequest request) {
        // 1. Only Safaricom (who knows the secret URL) may call this. Answer 404, reveal nothing.
        if (!secretMatches(secret) || !ipAllowed(request.getRemoteAddr())) {
            log.warn("Rejected STK callback from {}", request.getRemoteAddr());
            return ResponseEntity.notFound().build();
        }

        JsonNode cb = payload.path("Body").path("stkCallback");
        String checkoutRequestId = cb.path("CheckoutRequestID").asText(null);
        if (checkoutRequestId == null || checkoutRequestId.isBlank() || !cb.path("ResultCode").canConvertToInt()) {
            log.warn("Malformed STK callback ignored");
            return ResponseEntity.ok(ACK);
        }
        int resultCode = cb.path("ResultCode").asInt();
        String resultDesc = cb.path("ResultDesc").asText("");

        MpesaTransactions tx = transactionsRepository.findByCheckoutRequestId(checkoutRequestId).orElse(null);
        if (tx == null) {
            log.warn("Callback for unknown checkoutRequestId");
            return ResponseEntity.ok(ACK);
        }

        // 2. Replay / duplicate guard: only a PENDING transaction may change state.
        //    (Also add a @Version column and a UNIQUE constraint on mpesaReceiptNumber.)
        if (tx.getStatus() != Status.PENDING) {
            log.info("Ignoring repeat callback for {} (already {})", checkoutRequestId, tx.getStatus());
            return ResponseEntity.ok(ACK);
        }

        if (resultCode == 0) {
            String receipt = null;
            BigDecimal paid = null;
            for (JsonNode item : cb.path("CallbackMetadata").path("Item")) {
                String name = item.path("Name").asText("");
                if ("MpesaReceiptNumber".equals(name)) receipt = item.path("Value").asText(null);
                else if ("Amount".equals(name)) paid = item.path("Value").decimalValue();
            }

            // 3. Validate BEFORE changing anything (managed entities are saved on commit).
            if (receipt == null || paid == null || !amountMatches(tx, paid)) {
                log.error("Success callback failed validation for {}; left PENDING for review", checkoutRequestId);
                return ResponseEntity.ok(ACK);
            }

            // Ask Safaricom directly. Anyone who got past the secret URL still cannot fake this answer.
            // If Daraja is busy or disagrees, the payment stays PENDING and the reconciliation job settles it.
            if (!confirmedByDaraja(checkoutRequestId)) {
                log.warn("Success callback for {} not confirmed by STK query; left PENDING", checkoutRequestId);
                return ResponseEntity.ok(ACK);
            }

            // No separate crediting step: FundsService derives paid/balance from SUCCESS transactions,
            // so this status change IS the credit. That is why every check above matters.
            tx.setMpesaReceiptNumber(receipt);
            tx.setStatus(Status.SUCCESS);
        } else {
            tx.setStatus(Status.FAILED);
        }

        tx.setResultDescription(resultDesc);
        tx.setUpdatedAt(LocalDateTime.now());
        transactionsRepository.save(tx);

        // No catch-all: if the database fails we want a 500, not a silent "Accepted".
        return ResponseEntity.ok(ACK);
    }

    @GetMapping("/stkpush/{checkoutRequestId}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable String checkoutRequestId,
                                                           Authentication authentication) {
        return transactionsRepository.findByCheckoutRequestId(checkoutRequestId)
                .filter(tx -> canView(tx, authentication))
                .map(tx -> ResponseEntity.ok(new PaymentStatusResponse(
                        String.valueOf(tx.getStatus()),
                        tx.getResultDescription(),
                        tx.getMpesaReceiptNumber())))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Only what the client needs. Never return the entity (phone numbers, internal ids). */
    public record PaymentStatusResponse(String status, String message, String receiptNumber) {}

    /* ---------------- helpers ---------------- */

    private boolean confirmedByDaraja(String checkoutRequestId) {
        try {
            Map<String, Object> result = queryService.queryStatus(checkoutRequestId);
            return result != null && "0".equals(String.valueOf(result.get("ResultCode")));
        } catch (Exception e) {
            return false; // no answer is not a yes
        }
    }

    private boolean secretMatches(String provided) {
        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                callbackSecret.getBytes(StandardCharsets.UTF_8));
    }

    private boolean ipAllowed(String ip) {
        return allowedCallbackIps.isEmpty() || allowedCallbackIps.contains(ip);
    }

    private boolean amountMatches(MpesaTransactions tx, BigDecimal paid) {
        // Adjust getAmount() to your entity's field name.
        return new BigDecimal(String.valueOf(tx.getAmount())).compareTo(paid) == 0;
    }

    private boolean canView(MpesaTransactions tx, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        return admin || ownedBy(tx, auth.getName());
    }

    private boolean ownedBy(MpesaTransactions tx, String email) {
        // A parent can only pay for their own children, so the child's parent is the payer.
        // Assumes Parent has `userReg` with an `email` (as ParentRepo.findByUserReg_Email suggests).
        // Fails closed (false) if any link is missing.
        if (tx.getFunds() == null || tx.getFunds().getStudents() == null) return false;
        var parent = tx.getFunds().getStudents().getParent();
        return parent != null
                && parent.getUserReg() != null
                && email.equalsIgnoreCase(parent.getUserReg().getEmail());
    }
}