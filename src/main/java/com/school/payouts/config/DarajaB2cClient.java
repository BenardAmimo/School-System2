package com.school.payouts.config;

import com.school.payments.service.MpesaAuthService;
import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutPurpose;
import com.school.payouts.exception.B2cRejectedException;
import com.school.payouts.response.B2cAck;
import com.school.payouts.service.SecurityCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class DarajaB2cClient {
    private final B2cConfig config;
    private final MpesaAuthService authService;          // the same token code your STK push uses
    private final SecurityCredentialService credentials;
    private final WebClient mpesaWebClient;

    /**
     * Throws B2cRejectedException when it is certain nothing was paid (never sent, or refused).
     * Any other exception means "we cannot tell", and the caller must send it to a person.
     */
    public B2cAck send(Payout p) {
        String token;
        String credential;
        try {
            token = authService.generateAccessToken();
            credential = credentials.build();
        } catch (Exception e) {
            throw new B2cRejectedException("Could not authenticate with Safaricom: " + e.getMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("OriginatorConversationID", p.getOriginatorConversationId());
        body.put("InitiatorName", config.getInitiatorName());
        body.put("SecurityCredential", credential);
        body.put("CommandID", commandFor(p.getPurpose()));
        body.put("Amount", p.getAmount().intValueExact());           // whole shillings
        body.put("PartyA", config.getShortcode());
        body.put("PartyB", p.getPhoneNumber());
        body.put("Remarks", remarks(p));
        body.put("QueueTimeOutURL", config.getTimeoutUrl());
        body.put("ResultURL", config.getResultUrl());
        body.put("Occassion", p.getPurpose().name());                // CHECK: Daraja's v1 spells it "Occassion"

        Map<String, Object> res = mpesaWebClient.post()
                .uri(config.getUrl())
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> r.bodyToMono(String.class).defaultIfEmpty("")
                        .flatMap(e -> Mono.error(new B2cRejectedException("Safaricom rejected the request: " + e))))
                .onStatus(HttpStatusCode::is5xxServerError, r -> r.bodyToMono(String.class).defaultIfEmpty("")
                        .flatMap(e -> Mono.error(new IllegalStateException("Safaricom server error: " + e))))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(20));

        if (res == null || !"0".equals(String.valueOf(res.get("ResponseCode"))))
            throw new B2cRejectedException("Safaricom did not accept the request: " + res);

        return new B2cAck(String.valueOf(res.get("ConversationID")),
                String.valueOf(res.get("OriginatorConversationID")),
                String.valueOf(res.get("ResponseDescription")));
    }

    // CHECK these command names against the Daraja docs.
    private static String commandFor(PayoutPurpose purpose) {
        return switch (purpose) {
            case SALARY -> "SalaryPayment";
            case TIP -> "PromotionPayment";
            default -> "BusinessPayment";                             // refunds, advances, expenses
        };
    }

    private static String remarks(Payout p) {
        String r = p.getPurpose() + " #" + p.getPayoutId();
        return r.length() > 100 ? r.substring(0, 100) : r;
    }
}