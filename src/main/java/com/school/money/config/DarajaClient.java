package com.school.money.config;

import com.school.money.MoneyException;
import com.school.money.MoneyProperties;
import com.school.money.entity.Channel;
import com.school.money.entity.moneyEntities.PaymentOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sends M-Pesa B2C (to a phone) and B2B (to a paybill or till).
 * CHECK against Safaricom's current Daraja docs before going live: endpoint paths, CommandType values
 * and field spellings (the B2B API really does spell "RecieverIdentifierType" that way).
 * The result URL carries the order id, so a result is always matched to the right order.
 */
@Component
public class DarajaClient {

    private static final Logger log = LoggerFactory.getLogger(DarajaClient.class);

    /** accepted = Safaricom queued it (ResponseCode 0). A false value is a definite refusal. */
    public record Sent(boolean accepted, String originatorConversationId, String code, String description) {}

    private final MoneyProperties props;
    private final WebClient http = WebClient.builder().build();

    private volatile String token;
    private volatile Instant tokenExpiry = Instant.EPOCH;
    private volatile String credential;

    public DarajaClient(MoneyProperties props) {
        this.props = props;
    }

    public Sent send(PaymentOrder o) {
        MoneyProperties.B2b c = props.getB2b();
        if (!c.isEnabled()) {
            throw MoneyException.conflict("M-Pesa sending is switched off (app.money.b2b.enabled=false).");
        }
        require(c.getShortcode(), "app.money.b2b.shortcode");
        require(c.getInitiator(), "app.money.b2b.initiator");
        require(c.getCallbackBase(), "app.money.b2b.callback-base");
        require(c.getCallbackSecret(), "app.money.b2b.callback-secret");
        if (c.getCallbackSecret().length() < 24) {
            throw MoneyException.conflict("app.money.b2b.callback-secret must be at least 24 characters.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        String path;
        String resultUrl = c.getCallbackBase().replaceAll("/+$", "")
                + "/money/mpesa/result/" + c.getCallbackSecret() + "/" + o.getId();
        String timeoutUrl = c.getCallbackBase().replaceAll("/+$", "")
                + "/money/mpesa/timeout/" + c.getCallbackSecret() + "/" + o.getId();
        String remarks = trim("Order " + o.getId() + " " + (o.getDescription() == null ? "" : o.getDescription()), 100);

        body.put("Initiator", c.getInitiator());
        body.put("SecurityCredential", securityCredential());
        body.put("Amount", wholeShillings(o.getAmount()));
        body.put("PartyA", c.getShortcode());
        body.put("Remarks", remarks);
        body.put("QueueTimeOutURL", timeoutUrl);
        body.put("ResultURL", resultUrl);

        if (o.getChannel() == Channel.MPESA_B2C) {
            path = "/mpesa/b2c/v1/paymentrequest";
            body.remove("Initiator");
            body.put("OriginatorConversationID", UUID.randomUUID().toString());
            body.put("InitiatorName", c.getInitiator());
            body.put("CommandType", "BusinessPayment");
            body.put("PartyB", o.getPhone());
            body.put("Occasion", trim(o.getPurpose().name(), 100));
        } else {
            path = "/mpesa/b2b/v1/paymentrequest";
            body.put("CommandType", o.getChannel() == Channel.MPESA_TILL ? "BusinessBuyGoods" : "BusinessPayBill");
            body.put("SenderIdentifierType", "4");
            body.put("RecieverIdentifierType", "4");
            body.put("PartyB", o.getBusinessNumber());
            if (o.getChannel() == Channel.MPESA_PAYBILL) {
                body.put("AccountReference", trim(o.getAccountReference(), 20));
            } else {
                body.put("AccountReference", "ORDER" + o.getId());
            }
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = http.post().uri(c.getBaseUrl() + path)
                    .header("Authorization", "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(40));
            String code = res == null ? null : str(res.get("ResponseCode"));
            String desc = res == null ? "empty response" : str(res.get("ResponseDescription"));
            String oid = res == null ? null : str(res.get("OriginatorConversationID"));
            return new Sent("0".equals(code), oid, code, desc);
        } catch (WebClientResponseException e) {
            // Safaricom answered with an error status: the request was refused, nothing was queued.
            log.warn("Daraja refused order {}: {} {}", o.getId(), e.getStatusCode(), e.getResponseBodyAsString());
            return new Sent(false, null, String.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
        // Any other exception (timeout, connection reset) propagates: the outcome is UNKNOWN.
    }

    /**
     * Asks Safaricom what happened to an order whose result never arrived. The answer comes later on
     * /money/mpesa/status-result/... CHECK against the current Daraja "Transaction Status" documentation:
     * it accepts the OriginatorConversationID we stored when the request was queued.
     */
    public Sent queryStatus(PaymentOrder o) {
        MoneyProperties.B2b c = props.getB2b();
        if (!c.isEnabled()) throw MoneyException.conflict("M-Pesa sending is switched off (app.money.b2b.enabled=false).");
        require(c.getShortcode(), "app.money.b2b.shortcode");
        require(c.getInitiator(), "app.money.b2b.initiator");
        require(c.getCallbackBase(), "app.money.b2b.callback-base");
        require(c.getCallbackSecret(), "app.money.b2b.callback-secret");
        if (o.getOriginatorConversationId() == null) {
            throw MoneyException.conflict("This order has no Safaricom reference, so Safaricom cannot look it up. Check the M-Pesa portal.");
        }
        String base = c.getCallbackBase().replaceAll("/+$", "");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("Initiator", c.getInitiator());
        body.put("SecurityCredential", securityCredential());
        body.put("CommandID", "TransactionStatusQuery");
        body.put("TransactionID", "");
        body.put("OriginatorConversationID", o.getOriginatorConversationId());
        body.put("PartyA", c.getShortcode());
        body.put("IdentifierType", "4");
        body.put("ResultURL", base + "/money/mpesa/status-result/" + c.getCallbackSecret() + "/" + o.getId());
        body.put("QueueTimeOutURL", base + "/money/mpesa/timeout-status/" + c.getCallbackSecret() + "/" + o.getId());
        body.put("Remarks", trim("Status of order " + o.getId(), 100));
        body.put("Occasion", "StatusCheck");
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = http.post().uri(c.getBaseUrl() + "/mpesa/transactionstatus/v1/query")
                    .header("Authorization", "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body).retrieve().bodyToMono(Map.class).block(Duration.ofSeconds(40));
            String code = res == null ? null : str(res.get("ResponseCode"));
            String desc = res == null ? "empty response" : str(res.get("ResponseDescription"));
            return new Sent("0".equals(code), o.getOriginatorConversationId(), code, desc);
        } catch (WebClientResponseException e) {
            return new Sent(false, null, String.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    private static String wholeShillings(BigDecimal a) {
        if (a.stripTrailingZeros().scale() > 0) {
            throw MoneyException.bad("M-Pesa pays whole shillings only. Amount " + a + " has cents.");
        }
        return a.setScale(0).toPlainString();
    }

    private static void require(String v, String name) {
        if (v == null || v.isBlank()) throw MoneyException.conflict("Missing setting " + name);
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        s = s.replaceAll("[^A-Za-z0-9 .,_-]", "");
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String str(Object o) { return o == null ? null : o.toString(); }

    private synchronized String accessToken() {
        if (token != null && Instant.now().isBefore(tokenExpiry)) return token;
        MoneyProperties.B2b c = props.getB2b();
        require(c.getConsumerKey(), "app.money.b2b.consumer-key");
        require(c.getConsumerSecret(), "app.money.b2b.consumer-secret");
        String basic = Base64.getEncoder().encodeToString(
                (c.getConsumerKey() + ":" + c.getConsumerSecret()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        @SuppressWarnings("unchecked")
        Map<String, Object> res = http.get()
                .uri(c.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + basic)
                .retrieve().bodyToMono(Map.class).block(Duration.ofSeconds(20));
        if (res == null || res.get("access_token") == null) {
            throw MoneyException.conflict("Could not get an M-Pesa access token.");
        }
        token = res.get("access_token").toString();
        long secs = 3000;
        try { secs = Math.max(60, Long.parseLong(String.valueOf(res.get("expires_in"))) - 120); } catch (Exception ignored) { }
        tokenExpiry = Instant.now().plusSeconds(secs);
        return token;
    }

    private synchronized String securityCredential() {
        if (credential != null) return credential;
        MoneyProperties.B2b c = props.getB2b();
        require(c.getCertPath(), "app.money.b2b.cert-path");
        require(c.getInitiatorPassword(), "app.money.b2b.initiator-password");
        try (FileInputStream in = new FileInputStream(c.getCertPath())) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
            PublicKey key = cert.getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] enc = cipher.doFinal(c.getInitiatorPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            credential = Base64.getEncoder().encodeToString(enc);
            return credential;
        } catch (Exception e) {
            throw MoneyException.conflict("Could not build the M-Pesa security credential: " + e.getMessage());
        }
    }
}
