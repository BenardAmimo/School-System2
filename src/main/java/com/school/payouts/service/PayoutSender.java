package com.school.payouts.service;

import com.school.payouts.config.B2cConfig;
import com.school.payouts.config.DarajaB2cClient;
import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutChannel;
import com.school.payouts.entity.PayoutStatus;
import com.school.payouts.exception.B2cRejectedException;
import com.school.payouts.repository.PayoutRepository;
import com.school.payouts.response.B2cAck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutSender {
    private final PayoutRepository payouts;
    private final DarajaB2cClient client;
    private final B2cConfig config;
    private final TransactionTemplate tx;

    /** Deliberately NOT @Transactional: no database transaction is held while talking to Safaricom. */
    public Payout send(Long id, Long version) {
        config.assertReady();

        // Phase A: commit SENT before calling Safaricom, so a crash can never lead to a second send.
        Payout p = tx.execute(s -> {
            Payout x = payouts.findById(id).orElseThrow(() -> new IllegalArgumentException("Payout not found"));
            if (version == null || !version.equals(x.getVersion()))
                throw new ObjectOptimisticLockingFailureException(Payout.class, id);
            if (x.getStatus() != PayoutStatus.APPROVED)
                throw new IllegalStateException("Only an approved payout can be sent. This one is " + x.getStatus() + ".");
            if (x.getAmount().stripTrailingZeros().scale() > 0)
                throw new IllegalStateException("M-Pesa payouts must be whole shillings.");
            x.setStatus(PayoutStatus.SENT);
            x.setChannel(PayoutChannel.MPESA_B2C);
            x.setOriginatorConversationId(UUID.randomUUID().toString());
            x.setSentAt(LocalDateTime.now());
            return payouts.saveAndFlush(x);
        });

        // Phase B: the call, outside any transaction.
        B2cAck ack;
        try {
            ack = client.send(p);
        } catch (B2cRejectedException e) {
            moveFromSent(id, PayoutStatus.FAILED, e.getMessage());
            return payouts.findById(id).orElseThrow();
        } catch (Exception e) {
            log.error("B2C send for payout {} gave no clear answer", id, e);
            moveFromSent(id, PayoutStatus.NEEDS_REVIEW,
                    "No clear answer from Safaricom. Check the account statement before retrying.");
            return payouts.findById(id).orElseThrow();
        }

        // Phase C: remember Safaricom's ids. Best effort: the result callback may already have settled it.
        try {
            tx.executeWithoutResult(s -> payouts.findById(id).ifPresent(x -> {
                x.setConversationId(ack.conversationId());
                // CHECK: if your Daraja version generates its own originator id, callbacks carry that one.
                String theirs = ack.originatorConversationId();
                if (theirs != null && !theirs.isBlank() && !"null".equals(theirs)
                        && !theirs.equals(x.getOriginatorConversationId())
                        && x.getStatus() == PayoutStatus.SENT) {
                    x.setOriginatorConversationId(theirs);
                }
            }));
        } catch (Exception e) {
            log.warn("Could not record Safaricom's ids for payout {}: {}", id, e.getMessage());
        }
        return payouts.findById(id).orElseThrow();
    }

    /** Only moves a payout that is still SENT (the result callback may already have settled it). */
    private void moveFromSent(Long id, PayoutStatus status, String why) {
        tx.executeWithoutResult(s -> payouts.findById(id)
                .filter(x -> x.getStatus() == PayoutStatus.SENT)
                .ifPresent(x -> {
                    x.setStatus(status);
                    x.setResultDescription(why);
                    x.setCompletedAt(LocalDateTime.now());
                }));
    }
}