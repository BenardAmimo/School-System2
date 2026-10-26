package com.school.payouts.service;

import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutStatus;
import com.school.payouts.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutResultService {
    private final PayoutRepository payouts;

    @Transactional
    public void onResult(JsonNode payload) {
        JsonNode r = payload.path("Result");
        Payout p = find(r);
        if (p == null) return;

        boolean success = r.path("ResultCode").asInt(-1) == 0;

        // A late success for a payout a person marked "not paid" is serious: money moved. Raise it.
        if (p.getStatus() == PayoutStatus.FAILED && success) {
            log.error("B2C success reported for payout {} that is marked FAILED. Check the statement.", p.getPayoutId());
            p.setStatus(PayoutStatus.NEEDS_REVIEW);
            p.setResultDescription("Safaricom reports success for a payout marked as failed. Check the statement.");
            return;
        }
        // Settled already? A repeat or late callback changes nothing.
        if (p.getStatus() != PayoutStatus.SENT && p.getStatus() != PayoutStatus.NEEDS_REVIEW) return;

        p.setConversationId(r.path("ConversationID").asText(p.getConversationId()));
        p.setCompletedAt(LocalDateTime.now());

        if (!success) {
            p.setStatus(PayoutStatus.FAILED);
            p.setResultDescription(r.path("ResultDesc").asText("Failed"));
            return;
        }

        String receipt = null;
        BigDecimal amount = null;
        for (JsonNode item : r.path("ResultParameters").path("ResultParameter")) {   // CHECK the payload shape
            String key = item.path("Key").asText("");
            if ("TransactionReceipt".equals(key)) receipt = item.path("Value").asText(null);
            else if ("TransactionAmount".equals(key)) amount = toDecimal(item.path("Value").asText(null));
        }

        // Validate BEFORE marking paid: a success that does not match is left for a person.
        if (receipt == null || amount == null || amount.compareTo(p.getAmount()) != 0) {
            log.error("B2C success for payout {} failed validation (receipt={}, amount={})",
                    p.getPayoutId(), receipt, amount);
            p.setStatus(PayoutStatus.NEEDS_REVIEW);
            p.setResultDescription("Success reported but the receipt or amount did not match. Check the statement.");
            return;
        }
        p.setStatus(PayoutStatus.SUCCESS);
        p.setMpesaReceipt(receipt);
        p.setResultDescription(r.path("ResultDesc").asText(null));
    }

    /** A timeout is NOT a failure: the money may still have moved. */
    @Transactional
    public void onTimeout(JsonNode payload) {
        Payout p = find(payload.path("Result"));      // CHECK: the timeout payload may be shaped differently
        if (p != null && p.getStatus() == PayoutStatus.SENT) {
            p.setStatus(PayoutStatus.NEEDS_REVIEW);
            p.setResultDescription("Safaricom timed out. Check the account statement before retrying.");
        }
    }

    private Payout find(JsonNode r) {
        String originator = r.path("OriginatorConversationID").asText(null);
        if (originator == null || originator.isBlank()) {
            log.warn("B2C callback without an OriginatorConversationID");
            return null;
        }
        return payouts.findByOriginatorConversationId(originator).orElseGet(() -> {
            log.warn("B2C callback for an unknown payout");
            return null;
        });
    }

    private static BigDecimal toDecimal(String s) {
        try { return s == null ? null : new BigDecimal(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}