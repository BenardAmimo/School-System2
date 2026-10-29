package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.MoneyProperties;
import com.school.money.entity.ExpenseStatus;
import com.school.money.entity.PayeeType;
import com.school.money.entity.Purpose;
import com.school.money.entity.TenderTier;
import com.school.money.entity.moneyEntities.Expense;
import com.school.money.entity.moneyEntities.PaymentOrder;
import com.school.money.entity.moneyEntities.Quote;
import com.school.money.entity.moneyEntities.Supplier;
import com.school.money.repository.ExpenseRepository;
import com.school.money.repository.QuoteRepository;
import com.school.money.repository.SupplierRepository;
import com.school.money.request.*;
import com.school.money.response.*;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Purchases, with tender thresholds (configurable, app.money.tender.*):
 *   estimate below quotes-from  -> DIRECT: one approval
 *   from quotes-from            -> QUOTES: at least 3 written quotes, one approval
 *   from tender-from            -> TENDER: bidding stays open N days, at least 3 bids, two approvals (Admin, then Super Admin)
 * The person who picks the supplier cannot approve; nobody approves twice; the invoice must match the award.
 */
@Service
public class ExpenseService {

    public record Create(String title, String description, String category, BigDecimal estimatedAmount) {}
    public record Award(Long quoteId, Long supplierId, BigDecimal amount, String justification) {}
    public record Receipt(String invoiceNumber, BigDecimal invoiceAmount) {}

    private final ExpenseRepository expenses;
    private final QuoteRepository quotes;
    private final SupplierRepository suppliers;
    private final PaymentOrderService orders;
    private final MoneyProperties props;

    public ExpenseService(ExpenseRepository expenses, QuoteRepository quotes, SupplierRepository suppliers,
                          PaymentOrderService orders, MoneyProperties props) {
        this.expenses = expenses; this.quotes = quotes; this.suppliers = suppliers; this.orders = orders; this.props = props;
    }

    // ---- Entry points for the controller: who may do it, the work, and the mapping to the response ----

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Authentication auth) {
        Access.preparer(auth);
        return ExpenseResponse.list(list());
    }

    /** The rules the screen shows next to the amount field. */
    public ExpenseRulesResponse rules(Authentication auth) {
        Access.preparer(auth);
        var t = props.getTender();
        return new ExpenseRulesResponse(t.getQuotesFrom(), t.getTenderFrom(), t.getMinQuotes(), t.getTenderOpenDays());
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest r, Authentication auth) {
        return ExpenseResponse.from(create(new Create(r.title(), r.description(), r.category(), r.estimatedAmount()), Access.preparer(auth)));
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> quotesOf(Long id, Authentication auth) {
        Access.preparer(auth);
        return QuoteResponse.list(quotesOf(id));
    }

    @Transactional
    public QuoteResponse addQuote(Long id, QuoteRequest r, Authentication auth) {
        return QuoteResponse.from(addQuote(id, r.supplierId(), r.amount(), r.notes(), Access.preparer(auth)));
    }

    @Transactional
    public ExpenseResponse award(Long id, AwardRequest r, Authentication auth) {
        return ExpenseResponse.from(award(id, new Award(r.quoteId(), r.supplierId(), r.amount(), r.justification()), Access.preparer(auth)));
    }

    @Transactional
    public ExpenseResponse approve(Long id, Authentication auth) {
        return ExpenseResponse.from(approve(id, Access.approver(auth), Access.has(auth, Access.SUPER_ADMIN)));
    }

    @Transactional
    public ExpenseResponse reject(Long id, ReasonRequest r, Authentication auth) {
        return ExpenseResponse.from(reject(id, r.reason(), Access.approver(auth)));
    }

    @Transactional
    public ExpenseResponse receive(Long id, ReceiptRequest r, Authentication auth) {
        return ExpenseResponse.from(receive(id, new Receipt(r.invoiceNumber(), r.invoiceAmount()), Access.preparer(auth)));
    }

    @Transactional
    public ExpenseResponse order(Long id, Authentication auth) {
        Access.preparer(auth);
        return ExpenseResponse.from(order(id));
    }

    // ---- The work itself ----

    public static TenderTier tierFor(BigDecimal amount, MoneyProperties.Tender t) {
        if (amount.compareTo(t.getTenderFrom()) >= 0) return TenderTier.TENDER;
        if (amount.compareTo(t.getQuotesFrom()) >= 0) return TenderTier.QUOTES;
        return TenderTier.DIRECT;
    }

    @Transactional
    public Expense create(Create c, String actor) {
        if (c.title() == null || c.title().isBlank()) throw MoneyException.bad("Title is required.");
        if (c.estimatedAmount() == null || c.estimatedAmount().signum() <= 0) throw MoneyException.bad("Estimated amount must be above zero.");
        Expense e = new Expense();
        e.setTitle(c.title().trim());
        e.setDescription(c.description());
        e.setCategory(c.category());
        e.setEstimatedAmount(c.estimatedAmount());
        e.setTier(tierFor(c.estimatedAmount(), props.getTender()));
        if (e.getTier() == TenderTier.TENDER) {
            e.setBiddingClosesAt(LocalDateTime.now().plusDays(props.getTender().getTenderOpenDays()));
        }
        e.setRequestedBy(actor);
        e.setCreatedAt(LocalDateTime.now());
        return expenses.save(e);
    }

    @Transactional
    public Quote addQuote(Long expenseId, Long supplierId, BigDecimal amount, String notes, String actor) {
        Expense e = get(expenseId);
        if (e.getStatus() != ExpenseStatus.REQUESTED) throw MoneyException.conflict("Quotes can only be added while the requisition is open.");
        if (e.getTier() == TenderTier.DIRECT) throw MoneyException.bad("Below the quote threshold no quotes are needed. Choose the supplier directly.");
        if (e.getTier() == TenderTier.TENDER && LocalDateTime.now().isAfter(e.getBiddingClosesAt())) {
            throw MoneyException.conflict("Bidding closed on " + e.getBiddingClosesAt() + ".");
        }
        Supplier s = supplier(supplierId);
        if (amount == null || amount.signum() <= 0) throw MoneyException.bad("Quote amount must be above zero.");
        try {
            Quote q = new Quote();
            q.setExpenseId(expenseId);
            q.setSupplierId(s.getId());
            q.setSupplierName(s.getName());
            q.setAmount(amount);
            q.setNotes(notes);
            q.setRecordedBy(actor);
            q.setRecordedAt(LocalDateTime.now());
            return quotes.saveAndFlush(q);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw MoneyException.conflict(s.getName() + " already has a quote on this requisition.");
        }
    }

    /** Choose the supplier. DIRECT: name supplier and price. QUOTES/TENDER: pick a quote. */
    @Transactional
    public Expense award(Long id, Award a, String actor) {
        Expense e = get(id);
        if (e.getStatus() != ExpenseStatus.REQUESTED) throw MoneyException.conflict("Requisition is " + e.getStatus() + ".");
        BigDecimal amount;
        Supplier s;
        String justification = a.justification() == null ? null : a.justification().trim();

        if (e.getTier() == TenderTier.DIRECT) {
            s = supplier(a.supplierId());
            amount = a.amount();
            if (amount == null || amount.signum() <= 0) throw MoneyException.bad("Enter the agreed price.");
            e.setChosenQuoteId(null);
        } else {
            if (e.getTier() == TenderTier.TENDER && LocalDateTime.now().isBefore(e.getBiddingClosesAt())) {
                throw MoneyException.conflict("Bidding is still open until " + e.getBiddingClosesAt() + ". A tender cannot be awarded early.");
            }
            List<Quote> qs = quotes.findByExpenseIdOrderByAmountAsc(id);
            int need = props.getTender().getMinQuotes();
            if (qs.size() < need) throw MoneyException.conflict("Need at least " + need + " "
                    + (e.getTier() == TenderTier.TENDER ? "bids" : "quotes") + ", have " + qs.size() + ".");
            Quote chosen = qs.stream().filter(q -> q.getId().equals(a.quoteId())).findFirst()
                    .orElseThrow(() -> MoneyException.bad("Choose one of the recorded quotes."));
            if (!chosen.getId().equals(qs.get(0).getId()) && (justification == null || justification.length() < 10)) {
                throw MoneyException.bad("This is not the cheapest quote. Explain why it was chosen (at least 10 characters).");
            }
            s = supplier(chosen.getSupplierId());
            amount = chosen.getAmount();
            e.setChosenQuoteId(chosen.getId());
        }
        // The award may not slip into a stricter tier than the one the requisition was handled under.
        if (tierFor(amount, props.getTender()).ordinal() > e.getTier().ordinal()) {
            throw MoneyException.conflict("The price " + amount + " needs a stricter procedure than this requisition followed. "
                    + "Raise a new requisition with the right estimate.");
        }
        e.setSupplierId(s.getId());
        e.setSupplierName(s.getName());
        e.setAwardedAmount(amount);
        e.setSelectionJustification(justification);
        e.setSelectedBy(actor);
        e.setStatus(ExpenseStatus.AWAITING_APPROVAL);
        return expenses.save(e);
    }

    /**
     * DIRECT / QUOTES: one approval by an Admin or Super Admin.
     * TENDER: first an Admin (or Super Admin), then a DIFFERENT Super Admin.
     */
    @Transactional
    public Expense approve(Long id, String actor, boolean isSuperAdmin) {
        Expense e = get(id);
        if (e.getStatus() != ExpenseStatus.AWAITING_APPROVAL) throw MoneyException.conflict("Requisition is " + e.getStatus() + ".");
        if (actor.equalsIgnoreCase(e.getSelectedBy()) || actor.equalsIgnoreCase(e.getRequestedBy())) {
            throw MoneyException.forbidden("You raised or chose this purchase, so someone else must approve it.");
        }
        if (e.getTier() != TenderTier.TENDER) {
            e.setFirstApprover(actor);
            e.setFinalApprover(actor);
            e.setStatus(ExpenseStatus.APPROVED);
        } else if (e.getFirstApprover() == null) {
            e.setFirstApprover(actor);
        } else {
            if (!isSuperAdmin) throw MoneyException.forbidden("The final approval of a tender must be given by a Super Admin.");
            if (actor.equalsIgnoreCase(e.getFirstApprover())) throw MoneyException.forbidden("You gave the first approval. A different person must give the final one.");
            e.setFinalApprover(actor);
            e.setStatus(ExpenseStatus.APPROVED);
        }
        return expenses.save(e);
    }

    @Transactional
    public Expense reject(Long id, String reason, String actor) {
        Expense e = get(id);
        if (e.getStatus() != ExpenseStatus.REQUESTED && e.getStatus() != ExpenseStatus.AWAITING_APPROVAL) {
            throw MoneyException.conflict("Requisition is " + e.getStatus() + ".");
        }
        if (reason == null || reason.isBlank()) throw MoneyException.bad("Give a reason.");
        e.setStatus(ExpenseStatus.REJECTED);
        e.setRejectReason(reason.trim());
        return expenses.save(e);
    }

    /** Goods or service received, invoice checked against what was awarded. */
    @Transactional
    public Expense receive(Long id, Receipt r, String actor) {
        Expense e = get(id);
        if (e.getStatus() != ExpenseStatus.APPROVED) throw MoneyException.conflict("Requisition is " + e.getStatus() + ", it must be APPROVED first.");
        if (r.invoiceNumber() == null || r.invoiceNumber().isBlank()) throw MoneyException.bad("Enter the invoice number.");
        if (r.invoiceAmount() == null || r.invoiceAmount().signum() <= 0) throw MoneyException.bad("Enter the invoice amount.");
        BigDecimal diff = r.invoiceAmount().subtract(e.getAwardedAmount()).abs();
        if (diff.compareTo(props.getTender().getInvoiceTolerance()) > 0) {
            throw MoneyException.conflict("The invoice (" + r.invoiceAmount() + ") does not match the approved amount ("
                    + e.getAwardedAmount() + "). Ask the supplier to correct it or raise the difference for approval.");
        }
        e.setInvoiceNumber(r.invoiceNumber().trim());
        e.setInvoiceAmount(r.invoiceAmount());
        e.setReceivedBy(actor);
        e.setReceivedAt(LocalDateTime.now());
        e.setStatus(ExpenseStatus.RECEIVED);
        return expenses.save(e);
    }

    /** Creates the payment order to the supplier (their verified payment details are used). */
    @Transactional
    public Expense order(Long id) {
        Expense e = get(id);
        if (e.getStatus() != ExpenseStatus.RECEIVED) throw MoneyException.conflict("Goods must be received and the invoice matched first.");
        orders.create(Purpose.EXPENSE, "EXPENSE", e.getId(), e.getInvoiceAmount(), PayeeType.SUPPLIER,
                String.valueOf(e.getSupplierId()), e.getTitle() + " / inv " + e.getInvoiceNumber(), e.getFinalApprover());
        e.setStatus(ExpenseStatus.PAYMENT_ORDERED);
        return expenses.save(e);
    }

    @EventListener
    @Transactional
    public void onSettled(OrderSettledEvent ev) {
        PaymentOrder o = ev.order();
        if (o.getPurpose() != Purpose.EXPENSE) return;
        expenses.findById(o.getSourceId()).ifPresent(e -> { e.setStatus(ExpenseStatus.PAID); expenses.save(e); });
    }

    @Transactional(readOnly = true)
    public List<Expense> list() { return expenses.findAllByOrderByIdDesc(); }

    @Transactional(readOnly = true)
    public List<Quote> quotesOf(Long id) { return quotes.findByExpenseIdOrderByAmountAsc(id); }

    private Supplier supplier(Long id) {
        if (id == null) throw MoneyException.bad("Choose a supplier.");
        Supplier s = suppliers.findById(id).orElseThrow(() -> MoneyException.notFound("Supplier not found."));
        if (!s.isActive()) throw MoneyException.conflict(s.getName() + " is not active.");
        return s;
    }

    private Expense get(Long id) { return expenses.findById(id).orElseThrow(() -> MoneyException.notFound("Requisition not found.")); }
}