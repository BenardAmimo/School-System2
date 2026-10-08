package com.school.payments.service;

import com.school.entity.Funds;
import com.school.entity.Parent;
import com.school.error.MpesaException;
import com.school.payments.MpesaConfig;
import com.school.payments.entity.IdempotencyRecord;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.model.MpesaTransactionRequest;
import com.school.payments.model.MpesaTransactionsResponse;
import com.school.payments.repository.IdempotencyKeyRepo;
import com.school.payments.repository.MpesaTransactionsRepository;
import com.school.repo.FundsRepository;
import com.school.repo.ParentRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MpesaTransactionService implements MpesaTransServe {

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");
    private static final DateTimeFormatter DARAJA_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern KE_MSISDN = Pattern.compile("^254[17]\\d{8}$");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("250000"); // check against your Daraja/M-Pesa limits
    // A PENDING prompt reserves its amount for this long, so two prompts cannot overpay the same fee.
    private static final Duration PENDING_HOLD = Duration.ofMinutes(5);

    private final MpesaTransactionsRepository transactionsRepository;
    private final IdempotencyKeyRepo idempotencyKeyRepo;
    private final MpesaAuthService authService;
    private final MpesaConfig mpesaConfig;
    private final WebClient mpesaWebclient;
    private final FundsRepository fundsRepository;
    private final ParentRepo parentRepo;
    private final TransactionTemplate txTemplate;
    private final EntityManager em;

    public MpesaTransactionService(MpesaTransactionsRepository transactionsRepository,
                                   IdempotencyKeyRepo idempotencyKeyRepo,
                                   MpesaAuthService authService,
                                   MpesaConfig mpesaConfig,
                                   WebClient mpesaWebclient,
                                   FundsRepository fundsRepository,
                                   ParentRepo parentRepo,
                                   TransactionTemplate txTemplate,
                                   EntityManager em) {
        this.transactionsRepository = transactionsRepository;
        this.idempotencyKeyRepo = idempotencyKeyRepo;
        this.authService = authService;
        this.mpesaConfig = mpesaConfig;
        this.mpesaWebclient = mpesaWebclient;
        this.fundsRepository = fundsRepository;
        this.parentRepo = parentRepo;
        this.txTemplate = txTemplate;
        this.em = em;
    }

    /**
     * Flow: validate -> reserve (idempotency row + PENDING transaction, one short DB tx)
     *       -> call Daraja (no DB tx held) -> store checkoutRequestId.
     * The reservation happens BEFORE the Daraja call, so a double-click or a timeout can never
     * produce two prompts or an untracked payment.
     */
    @Override
    public MpesaTransactionsResponse initiateStkPush(MpesaTransactionRequest req, String rawKey, Authentication auth) {
        // Scope the key to the user so one user can never read or replay another user's key.
        String key = scopedKey(auth.getName(), requireValidKey(rawKey));

        Optional<MpesaTransactionsResponse> replay = replay(key);
        if (replay.isPresent()) return replay.get();

        String phone = normalizePhone(req.getPhoneNumber());
        BigDecimal amount = requireValidAmount(req.getAmount());

        // Phase 1: reserve.
        MpesaTransactions pending;
        try {
            pending = txTemplate.execute(status -> reserve(req, amount, phone, key, auth));
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // Same key arrived at the same moment: the other request owns it.
            return replay(key).orElseThrow(() ->
                    new MpesaException("Could not record the payment request. Please try again."));
        }

        // Phase 2: call Daraja.
        Map<String, Object> response;
        try {
            response = callDaraja(req, phone, amount);
        } catch (MpesaException e) {
            failReservation(pending, key); // Safaricom definitely did not accept it
            throw e;
        } catch (RuntimeException e) {
            // Timeout / network error: the prompt MAY have been sent. Leave it PENDING for reconciliation.
            log.error("STK push outcome unknown for transaction created at {}", pending.getCreatedAt(), e);
            throw new MpesaException(
                    "We could not confirm the payment request. Check your phone and the payment status before trying again.");
        }

        if (!"0".equals(String.valueOf(response.get("ResponseCode")))) {
            log.warn("STK push rejected: {}", response.get("ResponseDescription"));
            failReservation(pending, key);
            throw new MpesaException("STK push rejected: " + response.get("ResponseDescription"));
        }

        // Phase 3: attach Safaricom's ids.
        String checkoutRequestId = (String) response.get("CheckoutRequestID");
        String merchantRequestId = (String) response.get("MerchantRequestID");
        try {
            MpesaTransactions saved = txTemplate.execute(status -> {
                pending.setCheckoutRequestId(checkoutRequestId);
                pending.setMerchantRequestId(merchantRequestId);
                pending.setUpdatedAt(LocalDateTime.now());
                MpesaTransactions t = transactionsRepository.save(pending);
                idempotencyKeyRepo.findById(key).ifPresent(r -> {
                    r.setCheckoutRequestId(checkoutRequestId);
                    idempotencyKeyRepo.save(r);
                });
                return t;
            });
            return toDto(saved, (String) response.get("ResponseDescription"));
        } catch (RuntimeException e) {
            // The customer has been prompted but we could not store the ids. Keep them for manual repair.
            log.error("Could not store STK ids: checkoutRequestId={} merchantRequestId={}",
                    checkoutRequestId, merchantRequestId, e);
            throw e;
        }
    }

    /* ---------------- phase 1 ---------------- */

    private MpesaTransactions reserve(MpesaTransactionRequest req, BigDecimal amount, String phone,
                                      String key, Authentication auth) {
        // Row lock: concurrent payments for the same fee queue up here, so the balance check below is safe.
        Funds funds = em.find(Funds.class, req.getFundsId(), LockModeType.PESSIMISTIC_WRITE);
        if (funds == null) throw new MpesaException("Fee record not found");

        authorizePayer(funds, auth);

        BigDecimal outstanding = outstanding(funds);
        if (outstanding.signum() <= 0) {
            throw new MpesaException("This fee is already fully paid, or a payment for it is in progress.");
        }
        if (amount.compareTo(outstanding) > 0) {
            throw new MpesaException("Amount exceeds the outstanding balance of " + outstanding.toPlainString());
        }

        IdempotencyRecord rec = new IdempotencyRecord();
        rec.setIdempotencyKey(key);
        rec.setStatus(Status.PENDING);
        rec.setCreatedAt(LocalDateTime.now());
        em.persist(rec);
        em.flush(); // a duplicate key fails right here

        MpesaTransactions t = new MpesaTransactions();
        t.setAmount(req.getAmount());
        t.setFunds(funds);
        t.setAccountReference(truncate(req.getAccountRef(), 12, "SCHOOLFEES"));
        t.setStatus(Status.PENDING);
        t.setCreatedAt(LocalDateTime.now());
        t.setPhoneNumber(phone);
        return transactionsRepository.save(t);
    }

    /** Amount due minus SUCCESS payments minus recent PENDING prompts (older PENDING ones are ignored). */
    private BigDecimal outstanding(Funds funds) {
        LocalDateTime cutoff = LocalDateTime.now().minus(PENDING_HOLD);
        BigDecimal due = funds.getAmount() == null ? BigDecimal.ZERO : funds.getAmount();
        BigDecimal committed = funds.getMpesaTransactions() == null
                ? BigDecimal.ZERO
                : funds.getMpesaTransactions().stream()
                .filter(t -> t.getStatus() == Status.SUCCESS
                        || (t.getStatus() == Status.PENDING && t.getCreatedAt() != null
                        && t.getCreatedAt().isAfter(cutoff)))
                .map(MpesaTransactions::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return due.subtract(committed);
    }

    /** SHA-256(user + key): fixed length, scoped per user, and no email stored in the table. */
    private String scopedKey(String user, String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((user.toLowerCase() + ":" + key).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Allow-list, not block-list: anything that is not ADMIN, SUPER_ADMIN or the owning PARENT is denied. */
    private void authorizePayer(Funds funds, Authentication auth) {
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN")) return;
        if (!roles.contains("ROLE_PARENT")) {
            throw new AccessDeniedException("You are not allowed to pay fees.");
        }

        Parent parent = parentRepo.findByUserReg_Email(auth.getName())
                .orElseThrow(() -> new MpesaException("This account is not linked to a parent record"));
        Parent owner = funds.getStudents().getParent();
        if (owner == null || !owner.getParentId().equals(parent.getParentId())) {
            throw new AccessDeniedException("You can only pay fees for your own children.");
        }
    }

    private void failReservation(MpesaTransactions t, String key) {
        txTemplate.executeWithoutResult(status -> {
            t.setStatus(Status.FAILED);
            t.setUpdatedAt(LocalDateTime.now());
            transactionsRepository.save(t);
            idempotencyKeyRepo.deleteById(key); // a definite failure may be retried with the same key
        });
    }

    /* ---------------- phase 2 ---------------- */

    private Map<String, Object> callDaraja(MpesaTransactionRequest req, String phone, BigDecimal amount) {
        String token = authService.generateAccessToken();
        String timestamp = ZonedDateTime.now(NAIROBI).format(DARAJA_TS);
        String password = Base64.getEncoder().encodeToString(
                (mpesaConfig.getShortCode() + mpesaConfig.getPasskey() + timestamp).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("BusinessShortCode", mpesaConfig.getShortCode());
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", amount.longValueExact());
        body.put("PartyA", phone);
        body.put("PartyB", mpesaConfig.getShortCode());
        body.put("PhoneNumber", phone);
        body.put("CallBackURL", mpesaConfig.getCallbackUrl()); // must be https://host/stk/callback/{secret}
        body.put("AccountReference", truncate(req.getAccountRef(), 12, "SCHOOLFEES"));
        body.put("TransactionDesc", truncate(req.getTransactionDescription(), 13, "School fees"));

        // NEVER log the token, the body or the password: Base64(shortcode+passkey+timestamp) reveals the passkey.

        Map<String, Object> response = mpesaWebclient
                .post()
                .uri(mpesaConfig.getStkPushUrl())
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .flatMap(error -> {
                            log.error("Mpesa STK push HTTP error: {}", error);
                            return Mono.error(new MpesaException("Mpesa rejected the STK push request"));
                        }))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(15));

        if (response == null) {
            throw new IllegalStateException("Empty response from Safaricom"); // outcome unknown, not a clean rejection
        }
        return response;
    }

    /* ---------------- idempotent replay ---------------- */

    private Optional<MpesaTransactionsResponse> replay(String key) {
        return idempotencyKeyRepo.findById(key).map(rec -> {
            if (rec.getCheckoutRequestId() == null) {
                throw new MpesaException(
                        "This payment request is still being processed. Please wait a moment and check the payment status.");
            }
            MpesaTransactions t = transactionsRepository.findByCheckoutRequestId(rec.getCheckoutRequestId())
                    .orElseThrow(() -> new MpesaException("Mpesa transaction not found for idempotency key"));
            return toDto(t, "Duplicate Request");
        });
    }

    /* ---------------- validation ---------------- */

    private String requireValidKey(String key) {
        if (key == null || !key.matches("[A-Za-z0-9-]{8,64}")) {
            throw new MpesaException("Invalid Idempotency-Key");
        }
        return key;
    }

    private BigDecimal requireValidAmount(Object raw) {
        BigDecimal a;
        try {
            a = new BigDecimal(String.valueOf(raw));
        } catch (NumberFormatException e) {
            throw new MpesaException("Invalid amount");
        }
        // STK push amounts are whole shillings.
        if (a.signum() <= 0 || a.stripTrailingZeros().scale() > 0 || a.compareTo(MAX_AMOUNT) > 0) {
            throw new MpesaException("Amount must be a whole number between 1 and " + MAX_AMOUNT);
        }
        return a;
    }

    /** Accepts 07XXXXXXXX, 01XXXXXXXX, +2547XXXXXXXX, 2547XXXXXXXX. Returns 254XXXXXXXXX. */
    private String normalizePhone(String raw) {
        if (raw == null) throw new MpesaException("Phone number is required");
        String p = raw.replaceAll("[\\s\\-()]", "");
        if (p.startsWith("+")) p = p.substring(1);
        if (p.startsWith("0")) p = "254" + p.substring(1);
        if (!KE_MSISDN.matcher(p).matches()) {
            throw new MpesaException("Invalid phone number. Use 07XXXXXXXX or 2547XXXXXXXX.");
        }
        return p;
    }

    // Daraja caps these fields at a few characters; check the current limits in the docs.
    private String truncate(String value, int max, String fallback) {
        String v = (value == null || value.isBlank()) ? fallback : value.trim();
        return v.length() <= max ? v : v.substring(0, max);
    }

    public MpesaTransactionsResponse toDto(MpesaTransactions transaction, String description) {
        MpesaTransactionsResponse transact = new MpesaTransactionsResponse();
        transact.setCheckoutRequestId(transaction.getCheckoutRequestId());
        transact.setMerchantRequestId(transaction.getMerchantRequestId());
        transact.setResponseDescription(description);
        transact.setStatus(transaction.getStatus());
        return transact;
    }
}