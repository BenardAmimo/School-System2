package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.MoneyProperties;
import com.school.money.config.DarajaClient;
import com.school.money.config.MoneyNotifier;
import com.school.money.entity.Channel;
import com.school.money.entity.OrderStatus;
import com.school.money.entity.PayeeType;
import com.school.money.entity.Purpose;
import com.school.money.entity.moneyEntities.PayeeAccount;
import com.school.money.entity.moneyEntities.PaymentOrder;
import com.school.money.repository.PayeeAccountRepository;
import com.school.money.repository.PaymentOrderRepository;
import com.school.money.request.*;
import com.school.money.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Turns approved payments into money moving. Rules that always hold:
 *  - an order exists only after the business approval (see the source services);
 *  - the person who approved the source cannot also be the person who pays it;
 *  - SENT is committed BEFORE the call to M-Pesa, so a crash can never lead to a silent double payment;
 *  - an unknown outcome is NEEDS_REVIEW, never an automatic resend.
 */
@Service
public class PaymentOrderService {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrderService.class);
    private static final Object SEND_LOCK = new Object();

    private final PaymentOrderRepository orders;
    private final PayeeAccountRepository payees;
    private final DarajaClient daraja;
    private final MoneyProperties props;
    private final ApplicationEventPublisher events;
    private final TransactionTemplate tx;
    private MoneyNotifier notifier;

    public PaymentOrderService(PaymentOrderRepository orders, PayeeAccountRepository payees, DarajaClient daraja,
                               MoneyProperties props, ApplicationEventPublisher events, TransactionTemplate tx,
                               MoneyNotifier notifier) {
        this.orders = orders;
        this.payees = payees;
        this.daraja = daraja;
        this.props = props;
        this.events = events;
        this.tx = tx;
        this.notifier = notifier;
    }

    // ---------- creating orders (called by the source modules) ----------

    /** Pays a registered payee, using their verified account. Idempotent per (purpose, sourceType, sourceId). */
    @Transactional
    public PaymentOrder create(Purpose purpose, String sourceType, Long sourceId, BigDecimal amount,
                               PayeeType payeeType, String payeeRef, String description, String approvedBy) {
        var existing = orders.findByPurposeAndSourceTypeAndSourceId(purpose, sourceType, sourceId);
        if (existing.isPresent()) return existing.get();

        PayeeAccount a = payees.findByPayeeTypeAndPayeeRef(payeeType, payeeRef)
                .orElseThrow(() -> MoneyException.conflict(
                        "No payment details on file for " + payeeType + " " + payeeRef + ". Add them first."));
        if (!a.isVerified()) {
            throw MoneyException.conflict("Payment details for " + a.getDisplayName()
                    + " have changed and are not verified yet. A second person must verify them.");
        }
        PaymentOrder o = base(purpose, sourceType, sourceId, amount, description, approvedBy);
        o.setPayeeName(a.getDisplayName());
        o.setChannel(a.getChannel());
        o.setPhone(a.getPhone());
        o.setBusinessNumber(a.getBusinessNumber());
        o.setAccountReference(a.getAccountReference());
        o.setBankName(a.getBankName());
        o.setBankAccountNumber(a.getBankAccountNumber());
        o.setBankAccountName(a.getBankAccountName());
        validateDetails(o);
        return orders.save(o);
    }

    /** Pays a phone number directly (used for refunds to the original payer). */
    @Transactional
    public PaymentOrder createToPhone(Purpose purpose, String sourceType, Long sourceId, BigDecimal amount,
                                      String payeeName, String phone, String description, String approvedBy) {
        var existing = orders.findByPurposeAndSourceTypeAndSourceId(purpose, sourceType, sourceId);
        if (existing.isPresent()) return existing.get();
        PaymentOrder o = base(purpose, sourceType, sourceId, amount, description, approvedBy);
        o.setPayeeName(payeeName);
        o.setChannel(Channel.MPESA_B2C);
        o.setPhone(normalizePhone(phone));
        validateDetails(o);
        return orders.save(o);
    }

    private PaymentOrder base(Purpose purpose, String sourceType, Long sourceId, BigDecimal amount,
                              String description, String approvedBy) {
        if (amount == null || amount.signum() <= 0) throw MoneyException.bad("Amount must be above zero.");
        PaymentOrder o = new PaymentOrder();
        o.setPurpose(purpose);
        o.setSourceType(sourceType);
        o.setSourceId(sourceId);
        o.setAmount(amount.setScale(2, java.math.RoundingMode.HALF_UP));
        o.setDescription(description);
        o.setApprovedBy(approvedBy);
        o.setCreatedBy(approvedBy);
        o.setStatus(OrderStatus.APPROVED);
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        return o;
    }

    private void validateDetails(PaymentOrder o) {
        switch (o.getChannel()) {
            case MPESA_B2C -> {
                if (o.getPhone() == null || !o.getPhone().matches("2547\\d{8}|2541\\d{8}"))
                    throw MoneyException.bad("Phone must look like 2547XXXXXXXX.");
            }
            case MPESA_PAYBILL -> {
                if (blank(o.getBusinessNumber()) || blank(o.getAccountReference()))
                    throw MoneyException.bad("Paybill and account number are required.");
            }
            case MPESA_TILL -> {
                if (blank(o.getBusinessNumber())) throw MoneyException.bad("Till number is required.");
            }
            case BANK -> {
                if (blank(o.getBankName()) || blank(o.getBankAccountNumber()) || blank(o.getBankAccountName()))
                    throw MoneyException.bad("Bank name, account number and account name are required.");
            }
            default -> { }
        }
        if (o.getChannel().isMpesa() && o.getAmount().stripTrailingZeros().scale() > 0) {
            throw MoneyException.bad("M-Pesa pays whole shillings only. Amount " + o.getAmount() + " has cents.");
        }
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }

    public static String normalizePhone(String raw) {
        if (raw == null) return null;
        String p = raw.replaceAll("[^0-9]", "");
        if (p.startsWith("0")) p = "254" + p.substring(1);
        else if (p.startsWith("7") || p.startsWith("1")) p = "254" + p;
        return p;
    }

    // ---------- paying: M-Pesa ----------

    /** Sends an APPROVED M-Pesa order. Three phases, so the money is never sent twice. */
    public PaymentOrder sendMpesa(Long id, String actor) {
        // phase 1 (own transaction): check, lock the daily limit, commit SENT
        PaymentOrder o = tx.execute(status -> {
            synchronized (SEND_LOCK) {
                PaymentOrder x = get(id);
                if (!x.getChannel().isMpesa()) throw MoneyException.bad("This order is not an M-Pesa order.");
                if (x.getStatus() != OrderStatus.APPROVED) {
                    throw MoneyException.conflict("Order is " + x.getStatus() + ", only APPROVED orders can be sent.");
                }
                separation(x, actor);
                checkDailyLimit(x.getAmount());
                x.setStatus(OrderStatus.SENT);
                x.setSentAt(LocalDateTime.now());
                x.setPaidBy(actor);
                x.setUpdatedAt(LocalDateTime.now());
                return orders.saveAndFlush(x);
            }
        });

        // phase 2 (no transaction): talk to Safaricom
        DarajaClient.Sent sent;
        try {
            sent = daraja.send(o);
        } catch (MoneyException e) {
            // configuration problem before anything was sent: safe to put the order back
            tx.executeWithoutResult(s -> {
                PaymentOrder x = get(id);
                x.setStatus(OrderStatus.APPROVED);
                x.setSentAt(null);
                x.setUpdatedAt(LocalDateTime.now());
                orders.save(x);
            });
            throw e;
        } catch (RuntimeException e) {
            // timeout / connection lost: UNKNOWN outcome. Leave it SENT, the stuck job will flag it.
            log.error("Unknown outcome sending order {}", id, e);
            return get(id);
        }

        // phase 3: record the answer
        return tx.execute(s -> {
            PaymentOrder x = get(id);
            if (sent.accepted()) {
                x.setOriginatorConversationId(sent.originatorConversationId());
            } else if (x.getStatus() == OrderStatus.SENT) {
                x.setStatus(OrderStatus.FAILED);
                x.setFailureReason(sent.code() + ": " + sent.description());
                alertFailed(x);
            }
            x.setUpdatedAt(LocalDateTime.now());
            return orders.save(x);
        });
    }

    /** Called by the callback controller. Idempotent. */
    @Transactional
    public void applyMpesaResult(Long orderId, int resultCode, String description, String receipt) {
        PaymentOrder o = orders.findById(orderId).orElse(null);
        if (o == null) {
            log.warn("M-Pesa result for unknown order {}", orderId);
            return;
        }
        if (o.getStatus() == OrderStatus.PAID) return;
        if (o.getStatus() != OrderStatus.SENT && o.getStatus() != OrderStatus.NEEDS_REVIEW) {
            log.warn("M-Pesa result for order {} in status {} ignored", orderId, o.getStatus());
            return;
        }
        if (resultCode == 0) {
            settle(o, receipt == null ? "MPESA" : receipt, o.getPaidBy());
        } else {
            o.setStatus(OrderStatus.FAILED);
            o.setFailureReason(resultCode + ": " + description);
            o.setUpdatedAt(LocalDateTime.now());
            orders.save(o);
            alertFailed(o);
        }
    }

    @Transactional
    public void applyMpesaTimeout(Long orderId) {
        orders.findById(orderId).ifPresent(o -> {
            if (o.getStatus() == OrderStatus.SENT) {
                o.setStatus(OrderStatus.NEEDS_REVIEW);
                o.setFailureReason("Safaricom queue timeout: result unknown. Check the M-Pesa portal.");
                o.setUpdatedAt(LocalDateTime.now());
                orders.save(o);
                if (notifier != null) notifier.toRoles("Payment needs checking",
                        "Payment order " + o.getId() + " (" + o.getAmount() + " to " + o.getPayeeName() + ") has no result from M-Pesa.\n"
                                + "Open Money out > Payments. The system will ask Safaricom automatically; if it stays here, check the M-Pesa portal "
                                + "and choose 'It was paid' or 'It was not paid'. Do not resend it.",
                        "ADMIN", "SUPER_ADMIN", "BURSAR");
            }
        });
    }

    private void alertFailed(PaymentOrder o) {
        if (notifier != null) notifier.toRoles("Payment failed",
                "Payment order " + o.getId() + " (" + o.getAmount() + " to " + o.getPayeeName() + ") failed: " + o.getFailureReason()
                        + "\nOpen Money out > Payments to fix the details and retry.", "BURSAR", "ADMIN", "SUPER_ADMIN");
    }

    // ---------- asking Safaricom what happened ----------

    /** Every 30 minutes: ask Safaricom about NEEDS_REVIEW orders (at most 5 per round, each at most once an hour). */
    @Scheduled(fixedDelay = 1_800_000, initialDelay = 300_000)
    public void checkReviewOrders() {
        if (!props.getB2b().isEnabled()) return;
        int asked = 0;
        for (PaymentOrder o : orders.findByStatusOrderByCreatedAtAsc(OrderStatus.NEEDS_REVIEW)) {
            if (asked >= 5) break;
            if (o.getOriginatorConversationId() == null) continue;
            if (o.getLastStatusCheckAt() != null && o.getLastStatusCheckAt().isAfter(LocalDateTime.now().minusHours(1))) continue;
            try {
                requestStatusCheck(o.getId());
                asked++;
            } catch (RuntimeException e) {
                log.warn("Status check for order {} failed: {}", o.getId(), e.getMessage());
            }
        }
    }

    /** Sends the status question. The answer arrives at applyStatusResult. */
    public PaymentOrder requestStatusCheck(Long id) {
        PaymentOrder o = get(id);
        if (o.getStatus() != OrderStatus.NEEDS_REVIEW && o.getStatus() != OrderStatus.SENT) {
            throw MoneyException.conflict("Only SENT or NEEDS_REVIEW orders can be checked.");
        }
        DarajaClient.Sent s = daraja.queryStatus(o);
        return tx.execute(st -> {
            PaymentOrder x = get(id);
            x.setLastStatusCheckAt(LocalDateTime.now());
            if (!s.accepted()) x.setFailureReason("Status check refused: " + s.code() + " " + s.description());
            x.setUpdatedAt(LocalDateTime.now());
            return orders.save(x);
        });
    }

    /**
     * Safaricom answered the status question. Only a clear "Completed" settles the order; anything else
     * leaves it for a person, because an automatic FAILED could lead to paying twice.
     */
    @Transactional
    public void applyStatusResult(Long orderId, boolean completed, String receipt, String note) {
        PaymentOrder o = orders.findById(orderId).orElse(null);
        if (o == null || o.getStatus() == OrderStatus.PAID) return;
        if (o.getStatus() != OrderStatus.SENT && o.getStatus() != OrderStatus.NEEDS_REVIEW) return;
        if (completed && receipt != null && !receipt.isBlank()) {
            settle(o, receipt, "SAFARICOM-STATUS");
        } else {
            o.setFailureReason("Safaricom status check: " + (note == null ? "no clear answer" : note) + ". A person must confirm.");
            o.setUpdatedAt(LocalDateTime.now());
            orders.save(o);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> feesBetween(LocalDateTime from, LocalDateTime to) {
        BigDecimal total = orders.sumFeesBetween(from, to);
        return Map.of("totalFees", total == null ? BigDecimal.ZERO : total, "payments", orders.countFeedBetween(from, to));
    }

    /** SENT for more than 15 minutes with no result: flag it, never resend. */
    @Scheduled(fixedDelay = 300_000, initialDelay = 120_000)
    public void flagStuck() {
        List<PaymentOrder> stuck = orders.findByStatusAndSentAtBefore(OrderStatus.SENT, LocalDateTime.now().minusMinutes(15));
        for (PaymentOrder o : stuck) applyMpesaTimeout(o.getId());
        if (!stuck.isEmpty()) log.warn("{} payment order(s) moved to NEEDS_REVIEW", stuck.size());
    }

    /** A person checked the M-Pesa portal and settles a NEEDS_REVIEW order. */
    @Transactional
    public PaymentOrder settleReview(Long id, boolean paid, String receipt, String actor) {
        PaymentOrder o = get(id);
        if (o.getStatus() != OrderStatus.NEEDS_REVIEW && o.getStatus() != OrderStatus.SENT) {
            throw MoneyException.conflict("Only SENT or NEEDS_REVIEW orders can be settled this way.");
        }
        if (paid) {
            if (blank(receipt)) throw MoneyException.bad("Enter the M-Pesa receipt number you saw in the portal.");
            settle(o, receipt.trim(), actor);
        } else {
            o.setStatus(OrderStatus.FAILED);
            o.setFailureReason("Confirmed not paid by " + actor);
            o.setUpdatedAt(LocalDateTime.now());
        }
        return orders.save(o);
    }

    // ---------- paying: cash, cheque, bank ----------

    @Transactional
    public PaymentOrder markPaid(Long id, String reference, String actor) {
        PaymentOrder o = get(id);
        if (o.getChannel().isMpesa()) throw MoneyException.bad("M-Pesa orders are sent, not marked paid by hand.");
        if (o.getStatus() != OrderStatus.APPROVED && o.getStatus() != OrderStatus.IN_BATCH) {
            throw MoneyException.conflict("Order is " + o.getStatus() + ".");
        }
        if (blank(reference)) {
            throw MoneyException.bad(switch (o.getChannel()) {
                case CHEQUE -> "Enter the cheque number.";
                case CASH -> "Enter the cash voucher or receipt number.";
                default -> "Enter the bank reference.";
            });
        }
        separation(o, actor);
        if (o.getSentAt() == null) {
            checkDailyLimit(o.getAmount());
            o.setSentAt(LocalDateTime.now());
        }
        settle(o, reference.trim(), actor);
        return orders.save(o);
    }

    /** Builds the bank file for the chosen BANK orders and locks them into one batch. */
    @Transactional
    public Map.Entry<String, String> exportBankFile(List<Long> ids, String actor) {
        if (ids == null || ids.isEmpty()) throw MoneyException.bad("Choose at least one order.");
        List<PaymentOrder> list = orders.findAllById(ids);
        if (list.size() != ids.size()) throw MoneyException.notFound("Some orders were not found.");
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentOrder o : list) {
            if (o.getChannel() != Channel.BANK) throw MoneyException.bad("Order " + o.getId() + " is not a bank order.");
            if (o.getStatus() != OrderStatus.APPROVED) throw MoneyException.conflict("Order " + o.getId() + " is " + o.getStatus() + ".");
            separation(o, actor);
            total = total.add(o.getAmount());
        }
        checkDailyLimit(total);
        String batch = "BANK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        StringBuilder csv = new StringBuilder("Reference,Bank,AccountNumber,AccountName,Amount,Narration\r\n");
        for (PaymentOrder o : list) {
            o.setStatus(OrderStatus.IN_BATCH);
            o.setBatchId(batch);
            o.setSentAt(LocalDateTime.now());
            o.setPaidBy(actor);
            o.setUpdatedAt(LocalDateTime.now());
            csv.append(csv(batch + "-" + o.getId())).append(',')
                    .append(csv(o.getBankName())).append(',')
                    .append(csv(o.getBankAccountNumber())).append(',')
                    .append(csv(o.getBankAccountName())).append(',')
                    .append(o.getAmount().toPlainString()).append(',')
                    .append(csv(o.getPurpose() + " " + (o.getDescription() == null ? "" : o.getDescription())))
                    .append("\r\n");
        }
        orders.saveAll(list);
        return Map.entry(batch, csv.toString());
    }

    /** The bank confirmed the whole file. Each order gets the bank's reference. */
    @Transactional
    public List<PaymentOrder> confirmBatch(String batchId, String bankReference, String actor) {
        if (blank(bankReference)) throw MoneyException.bad("Enter the bank's batch reference.");
        List<PaymentOrder> list = orders.findByBatchId(batchId);
        if (list.isEmpty()) throw MoneyException.notFound("Batch not found.");
        for (PaymentOrder o : list) {
            if (o.getStatus() != OrderStatus.IN_BATCH) continue;
            settle(o, bankReference.trim() + "/" + o.getId(), actor);
        }
        return orders.saveAll(list);
    }

    /** The bank rejected the file: put the orders back so they can be fixed and re-exported. */
    @Transactional
    public List<PaymentOrder> releaseBatch(String batchId) {
        List<PaymentOrder> list = orders.findByBatchId(batchId);
        for (PaymentOrder o : list) {
            if (o.getStatus() == OrderStatus.IN_BATCH) {
                o.setStatus(OrderStatus.APPROVED);
                o.setBatchId(null);
                o.setSentAt(null);
                o.setUpdatedAt(LocalDateTime.now());
            }
        }
        return orders.saveAll(list);
    }

    @Transactional
    public PaymentOrder retry(Long id) {
        PaymentOrder o = get(id);
        if (o.getStatus() != OrderStatus.FAILED) throw MoneyException.conflict("Only FAILED orders can be retried.");
        o.setStatus(OrderStatus.APPROVED);
        o.setFailureReason(null);
        o.setOriginatorConversationId(null);
        o.setSentAt(null);
        o.setUpdatedAt(LocalDateTime.now());
        return orders.save(o);
    }

    // ---------- reading ----------

    @Transactional(readOnly = true)
    public List<PaymentOrder> list(OrderStatus status) {
        return status == null ? orders.findAllByOrderByCreatedAtDesc() : orders.findByStatusOrderByCreatedAtAsc(status);
    }

    public PaymentOrder get(Long id) {
        return orders.findById(id).orElseThrow(() -> MoneyException.notFound("Order " + id + " not found."));
    }

    // ---------- helpers ----------

    private void settle(PaymentOrder o, String reference, String actor) {
        o.setStatus(OrderStatus.PAID);
        o.setReference(reference);
        o.setPaidAt(LocalDateTime.now());
        o.setPaidBy(actor);
        o.setFailureReason(null);
        if (o.getChannel().isMpesa()) o.setFeeCharged(props.getFees().feeFor(o.getAmount()));
        o.setUpdatedAt(LocalDateTime.now());
        orders.save(o);
        events.publishEvent(new OrderSettledEvent(o));
    }

    private void separation(PaymentOrder o, String actor) {
        if (o.getApprovedBy() != null && o.getApprovedBy().equalsIgnoreCase(actor)) {
            throw MoneyException.forbidden("You approved this payment, so someone else must pay it.");
        }
    }

    private void checkDailyLimit(BigDecimal add) {
        BigDecimal today = orders.sumSentSince(LocalDate.now().atStartOfDay());
        if (today.add(add).compareTo(props.getDailyLimit()) > 0) {
            throw MoneyException.conflict("Daily payment limit of " + props.getDailyLimit()
                    + " would be exceeded (already " + today + " today).");
        }
    }

    private static String csv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        return (t.contains(",") || t.contains("\"") || t.contains("\n")) ? "\"" + t + "\"" : t;
    }

    // ---- Entry points for the controller: who may do it, the work, and the mapping to the response ----

    @Transactional(readOnly = true)
    public List<PaymentOrderResponse> list(OrderStatus status, Authentication auth) {
        Access.preparer(auth);
        return PaymentOrderResponse.list(list(status));
    }

    /** Not transactional on purpose: sending to M-Pesa runs in three separate phases. */
    public PaymentOrderResponse sendMpesa(Long id, Authentication auth) {
        return PaymentOrderResponse.from(sendMpesa(id, Access.preparer(auth)));
    }

    @Transactional
    public PaymentOrderResponse markPaid(Long id, MarkPaidRequest r, Authentication auth) {
        return PaymentOrderResponse.from(markPaid(id, r.reference(), Access.preparer(auth)));
    }

    /** Deciding that money was or was not paid is an approver's call. */
    @Transactional
    public PaymentOrderResponse settleReview(Long id, SettleRequest r, Authentication auth) {
        return PaymentOrderResponse.from(settleReview(id, r.paid(), r.receipt(), Access.approver(auth)));
    }

    /** Ask Safaricom what happened to a SENT / NEEDS_REVIEW order. The answer arrives by callback. */
    public PaymentOrderResponse requestStatusCheck(Long id, Authentication auth) {
        Access.preparer(auth);
        return PaymentOrderResponse.from(requestStatusCheck(id));
    }

    /** Safaricom charges paid in a month. Month looks like 2026-10. */
    @Transactional(readOnly = true)
    public FeesResponse fees(String month, Authentication auth) {
        Access.preparer(auth);
        java.time.YearMonth ym;
        try { ym = java.time.YearMonth.parse(month); } catch (Exception e) { throw MoneyException.bad("Month must look like 2026-10."); }
        Map<String, Object> m = feesBetween(ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay());
        return new FeesResponse((BigDecimal) m.get("totalFees"), ((Number) m.get("payments")).longValue());
    }

    @Transactional
    public PaymentOrderResponse retry(Long id, Authentication auth) {
        Access.preparer(auth);
        return PaymentOrderResponse.from(retry(id));
    }

    @Transactional
    public BankFileResponse exportBankFile(BankFileRequest r, Authentication auth) {
        var file = exportBankFile(r.ids() == null ? List.<Long>of() : r.ids(), Access.preparer(auth));
        return BankFileResponse.of(file.getKey(), file.getValue());
    }

    @Transactional
    public List<PaymentOrderResponse> confirmBatch(String batchId, BatchConfirmRequest r, Authentication auth) {
        return PaymentOrderResponse.list(confirmBatch(batchId, r.reference(), Access.preparer(auth)));
    }

    @Transactional
    public List<PaymentOrderResponse> releaseBatch(String batchId, Authentication auth) {
        Access.preparer(auth);
        return PaymentOrderResponse.list(releaseBatch(batchId));
    }
}