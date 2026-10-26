package com.school.payouts.service;

import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutStatus;
import com.school.payouts.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayoutStuckJob {
    private final PayoutRepository payouts;
    private final TransactionTemplate tx;

    /** Does not ask Safaricom and never retries. It hands the payout to a person. */
    @Scheduled(initialDelay = 120_000, fixedDelay = 300_000)
    public void flagStuck() {
        for (Payout p : payouts.findByStatusAndSentAtBefore(PayoutStatus.SENT, LocalDateTime.now().minusMinutes(15))) {
            try {
                tx.executeWithoutResult(s -> payouts.findById(p.getPayoutId())
                        .filter(x -> x.getStatus() == PayoutStatus.SENT)
                        .ifPresent(x -> {
                            x.setStatus(PayoutStatus.NEEDS_REVIEW);
                            x.setResultDescription("No result from Safaricom after 15 minutes. Check the statement.");
                            log.warn("Payout {} flagged for review", x.getPayoutId());
                        }));
            } catch (Exception e) {
                log.error("Could not flag payout {}", p.getPayoutId(), e);
            }
        }
    }
}