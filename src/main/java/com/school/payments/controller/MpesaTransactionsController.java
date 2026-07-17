package com.school.payments.controller;

import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.model.MpesaTransactionRequest;
import com.school.payments.model.MpesaTransactionsResponse;
import com.school.payments.repository.MpesaTransactionsRepository;
import com.school.payments.service.MpesaTransactionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class MpesaTransactionsController{
    private final MpesaTransactionService mpesaTransactionService;
    private final MpesaTransactionsRepository transactionsRepository;

    public MpesaTransactionsController(MpesaTransactionService mpesaTransactionService, MpesaTransactionsRepository transactionsRepository) {
        this.mpesaTransactionService = mpesaTransactionService;
        this.transactionsRepository = transactionsRepository;
    }
    @PostMapping("/stkPush")
    public ResponseEntity<MpesaTransactionsResponse> initiateStkPush(@RequestBody MpesaTransactionRequest transactionRequest,
                                                                     @RequestHeader("idempotency-key") String idempotencyKey){
        MpesaTransactionsResponse response = mpesaTransactionService
                .initiateStkPush(transactionRequest,idempotencyKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    @PostMapping("/stk/callback")
    @Transactional
    public ResponseEntity<Map<String, Object>> callback(@RequestBody Map<String, Object> payload) {
        try {
            Map<String, Object> body = (Map<String, Object>) payload.get("Body");
            Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");

            String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
            int resultCode = (int) stkCallback.get("ResultCode");
            String resultDesc = (String) stkCallback.get("ResultDesc");

            MpesaTransactions transaction = transactionsRepository
                    .findByCheckoutRequestId(checkoutRequestId)
                    .orElse(null);

            if (transaction == null) {
                log.warn("Callback received for unknown checkoutRequestId: {}", checkoutRequestId);
                // still acknowledge — Safaricom doesn't care that we don't recognize it,
                // returning an error here just causes pointless retries
                return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
            }

            transaction.setResultDescription(resultDesc);
            transaction.setUpdatedAt(LocalDateTime.now());

            if (resultCode == 0) {
                transaction.setStatus(Status.SUCCESS);

                Object metadataObj = stkCallback.get("CallbackMetadata");
                if (metadataObj != null) {
                    List<Map<String, Object>> items =
                            (List<Map<String, Object>>) ((Map<String, Object>) metadataObj).get("Item");
                    for (Map<String, Object> item : items) {
                        if ("MpesaReceiptNumber".equals(item.get("Name"))) {
                            transaction.setMpesaReceiptNumber(String.valueOf(item.get("Value")));
                        }
                    }
                }
            } else {
                transaction.setStatus(Status.FAILED);
            }

            transactionsRepository.save(transaction);

        } catch (Exception ex) {
            // log and swallow — never let a parsing bug cause Safaricom to retry indefinitely
            log.error("Failed to process STK callback: {}", payload, ex);
        }

        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }

    @GetMapping("/stkpush/{checkoutRequestId}")
    public ResponseEntity<MpesaTransactions> getStatus(@PathVariable String checkoutRequestId) {
        return transactionsRepository.findByCheckoutRequestId(checkoutRequestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
