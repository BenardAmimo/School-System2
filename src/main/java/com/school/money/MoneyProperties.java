package com.school.money;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * All Money-Out rules in one place. Change them in application.properties, never in code.
 */
@ConfigurationProperties(prefix = "app.money")
public class MoneyProperties {

    /** Max total sent to M-Pesa / bank per day. */
    private BigDecimal dailyLimit = new BigDecimal("500000");

    private final Advance advance = new Advance();
    private final Tender tender = new Tender();
    private final Refund refund = new Refund();
    private final B2b b2b = new B2b();
    private final Notify notify = new Notify();
    private final Fees fees = new Fees();

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal v) { this.dailyLimit = v; }
    public Advance getAdvance() { return advance; }
    public Tender getTender() { return tender; }
    public Refund getRefund() { return refund; }
    public B2b getB2b() { return b2b; }
    public Notify getNotify() { return notify; }
    public Fees getFees() { return fees; }

    public static class Advance {
        /** Fraction of one month's net salary a staff member may borrow (3/4). */
        private BigDecimal maxSalaryFraction = new BigDecimal("0.75");
        /** Longest repayment period in months. */
        private int maxInstalments = 4;

        public BigDecimal getMaxSalaryFraction() { return maxSalaryFraction; }
        public void setMaxSalaryFraction(BigDecimal v) { this.maxSalaryFraction = v; }
        public int getMaxInstalments() { return maxInstalments; }
        public void setMaxInstalments(int v) { this.maxInstalments = v; }
    }

    public static class Tender {
        /** Below this: one approval, no quotes. */
        private BigDecimal quotesFrom = new BigDecimal("20000");
        /** From this: formal tender (open bidding period, two-level approval). */
        private BigDecimal tenderFrom = new BigDecimal("200000");
        private int minQuotes = 3;
        private int tenderOpenDays = 7;
        /** Allowed gap between the delivered invoice and the awarded amount. */
        private BigDecimal invoiceTolerance = new BigDecimal("0.00");

        public BigDecimal getQuotesFrom() { return quotesFrom; }
        public void setQuotesFrom(BigDecimal v) { this.quotesFrom = v; }
        public BigDecimal getTenderFrom() { return tenderFrom; }
        public void setTenderFrom(BigDecimal v) { this.tenderFrom = v; }
        public int getMinQuotes() { return minQuotes; }
        public void setMinQuotes(int v) { this.minQuotes = v; }
        public int getTenderOpenDays() { return tenderOpenDays; }
        public void setTenderOpenDays(int v) { this.tenderOpenDays = v; }
        public BigDecimal getInvoiceTolerance() { return invoiceTolerance; }
        public void setInvoiceTolerance(BigDecimal v) { this.invoiceTolerance = v; }
    }

    public static class Refund {
        /** Days the parent has to choose before the credit is carried forward automatically. */
        private int decisionDays = 14;
        /** Scan the latest term for overpayments every night. */
        private boolean autoScan = true;

        public int getDecisionDays() { return decisionDays; }
        public void setDecisionDays(int v) { this.decisionDays = v; }
        public boolean isAutoScan() { return autoScan; }
        public void setAutoScan(boolean v) { this.autoScan = v; }
    }

    /** Settings for paybill / till payments (Daraja B2B). Reuses the B2C initiator. */
    public static class B2b {
        private boolean enabled = false;
        private String shortcode = "";
        private String initiator = "";
        private String initiatorPassword = "";
        private String certPath = "";
        private String baseUrl = "https://sandbox.safaricom.co.ke";
        private String consumerKey = "";
        private String consumerSecret = "";
        /** Public https base URL of this server, e.g. https://abc.ngrok.app */
        private String callbackBase = "";
        /** Long random string (24+ chars) that forms part of the callback path. */
        private String callbackSecret = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getShortcode() { return shortcode; }
        public void setShortcode(String v) { this.shortcode = v; }
        public String getInitiator() { return initiator; }
        public void setInitiator(String v) { this.initiator = v; }
        public String getInitiatorPassword() { return initiatorPassword; }
        public void setInitiatorPassword(String v) { this.initiatorPassword = v; }
        public String getCertPath() { return certPath; }
        public void setCertPath(String v) { this.certPath = v; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String v) { this.baseUrl = v; }
        public String getConsumerKey() { return consumerKey; }
        public void setConsumerKey(String v) { this.consumerKey = v; }
        public String getConsumerSecret() { return consumerSecret; }
        public void setConsumerSecret(String v) { this.consumerSecret = v; }
        public String getCallbackBase() { return callbackBase; }
        public void setCallbackBase(String v) { this.callbackBase = v; }
        public String getCallbackSecret() { return callbackSecret; }
        public void setCallbackSecret(String v) { this.callbackSecret = v; }
    }

    /** Email alerts. Sent only when a mail server is configured (spring.mail.*). */
    public static class Notify {
        private boolean enabled = true;
        /** From address; defaults to spring.mail.username when empty. */
        private String from = "";
        private String schoolName = "The school";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
        public String getFrom() { return from; }
        public void setFrom(String v) { this.from = v; }
        public String getSchoolName() { return schoolName; }
        public void setSchoolName(String v) { this.schoolName = v; }
    }

    /**
     * Safaricom's charge per payment, so it can be recorded as an expense.
     * Empty by default: copy the rates from Safaricom's current tariff for your account, e.g.
     *   app.money.fees.tiers[0].up-to=100    app.money.fees.tiers[0].fee=0
     *   app.money.fees.tiers[1].up-to=1500   app.money.fees.tiers[1].fee=5
     * The first tier whose up-to is not below the amount applies.
     */
    public static class Fees {
        private java.util.List<Tier> tiers = new java.util.ArrayList<>();

        public java.util.List<Tier> getTiers() { return tiers; }
        public void setTiers(java.util.List<Tier> v) { this.tiers = v; }

        public static class Tier {
            private BigDecimal upTo;
            private BigDecimal fee;
            public BigDecimal getUpTo() { return upTo; }
            public void setUpTo(BigDecimal v) { this.upTo = v; }
            public BigDecimal getFee() { return fee; }
            public void setFee(BigDecimal v) { this.fee = v; }
        }

        /** Fee for one M-Pesa payment of this amount; zero when no tiers are configured. */
        public BigDecimal feeFor(BigDecimal amount) {
            BigDecimal best = null;
            BigDecimal bestLimit = null;
            for (Tier t : tiers) {
                if (t.getUpTo() == null || t.getFee() == null) continue;
                if (amount.compareTo(t.getUpTo()) <= 0 && (bestLimit == null || t.getUpTo().compareTo(bestLimit) < 0)) {
                    best = t.getFee();
                    bestLimit = t.getUpTo();
                }
            }
            return best == null ? BigDecimal.ZERO : best;
        }
    }
}