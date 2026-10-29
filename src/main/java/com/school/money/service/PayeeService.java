package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.entity.Channel;
import com.school.money.entity.PayeeType;
import com.school.money.entity.moneyEntities.PayeeAccount;
import com.school.money.repository.PayeeAccountRepository;
import com.school.money.request.PayeeRequest;
import com.school.money.response.PayeeResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** How each payee wants to be paid. A change must be verified by a different person. */
@Service
public class PayeeService {

    public record Save(PayeeType payeeType, String payeeRef, String displayName, Channel channel,
                       String phone, String businessNumber, String accountReference,
                       String bankName, String bankAccountNumber, String bankAccountName) {}

    private final PayeeAccountRepository repo;

    public PayeeService(PayeeAccountRepository repo) { this.repo = repo; }

    // ---------- Entry points for the controller ----------

    @Transactional(readOnly = true)
    public List<PayeeResponse> list(PayeeType type, Authentication auth) {
        Access.preparer(auth);
        return PayeeResponse.list(list(type));
    }

    @Transactional
    public PayeeResponse save(PayeeRequest r, Authentication auth) {
        Save s = new Save(r.payeeType(), r.payeeRef(), r.displayName(), r.channel(), r.phone(), r.businessNumber(),
                r.accountReference(), r.bankName(), r.bankAccountNumber(), r.bankAccountName());
        return PayeeResponse.from(save(s, Access.preparer(auth)));
    }

    @Transactional
    public PayeeResponse verify(Long id, Authentication auth) {
        return PayeeResponse.from(verify(id, Access.approver(auth)));
    }

    // ---------- The work itself ----------

    @Transactional(readOnly = true)
    public List<PayeeAccount> list(PayeeType type) {
        return type == null ? repo.findAll() : repo.findAll().stream().filter(p -> p.getPayeeType() == type).toList();
    }

    @Transactional
    public PayeeAccount save(Save s, String actor) {
        if (s.payeeType() == null || blank(s.payeeRef()) || blank(s.displayName()) || s.channel() == null)
            throw MoneyException.bad("Payee type, reference, name and payment method are required.");
        PayeeAccount a = repo.findByPayeeTypeAndPayeeRef(s.payeeType(), s.payeeRef().trim()).orElseGet(PayeeAccount::new);
        a.setPayeeType(s.payeeType());
        a.setPayeeRef(s.payeeRef().trim());
        a.setDisplayName(s.displayName().trim());
        a.setChannel(s.channel());
        a.setPhone(null); a.setBusinessNumber(null); a.setAccountReference(null);
        a.setBankName(null); a.setBankAccountNumber(null); a.setBankAccountName(null);
        switch (s.channel()) {
            case MPESA_B2C -> {
                String p = PaymentOrderService.normalizePhone(s.phone());
                if (p == null || !p.matches("2547\\d{8}|2541\\d{8}")) throw MoneyException.bad("Phone must look like 0712345678 or 254712345678.");
                a.setPhone(p);
            }
            case MPESA_PAYBILL -> {
                if (blank(s.businessNumber()) || blank(s.accountReference())) throw MoneyException.bad("Paybill number and account number are required.");
                a.setBusinessNumber(digits(s.businessNumber()));
                a.setAccountReference(s.accountReference().trim());
            }
            case MPESA_TILL -> {
                if (blank(s.businessNumber())) throw MoneyException.bad("Till number is required.");
                a.setBusinessNumber(digits(s.businessNumber()));
            }
            case BANK -> {
                if (blank(s.bankName()) || blank(s.bankAccountNumber()) || blank(s.bankAccountName()))
                    throw MoneyException.bad("Bank name, account number and account name are required.");
                a.setBankName(s.bankName().trim());
                a.setBankAccountNumber(s.bankAccountNumber().trim());
                a.setBankAccountName(s.bankAccountName().trim());
            }
            default -> { }
        }
        a.setVerified(false);
        a.setVerifiedBy(null);
        a.setVerifiedAt(null);
        a.setUpdatedBy(actor);
        a.setUpdatedAt(LocalDateTime.now());
        return repo.save(a);
    }

    /** Second person confirms the details (by phone call, cheque book, bank letter...). */
    @Transactional
    public PayeeAccount verify(Long id, String actor) {
        PayeeAccount a = repo.findById(id).orElseThrow(() -> MoneyException.notFound("Payee not found."));
        if (actor.equalsIgnoreCase(a.getUpdatedBy())) {
            throw MoneyException.forbidden("You entered these details, so someone else must verify them.");
        }
        a.setVerified(true);
        a.setVerifiedBy(actor);
        a.setVerifiedAt(LocalDateTime.now());
        return repo.save(a);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String digits(String s) { return s.replaceAll("[^0-9]", ""); }
}