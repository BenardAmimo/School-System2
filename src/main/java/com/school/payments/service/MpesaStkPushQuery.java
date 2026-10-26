package com.school.payments.service;

import com.school.error.MpesaQueryException;
import com.school.payments.MpesaConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MpesaStkPushQuery {
    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");
    private static final DateTimeFormatter DARAJA_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern ERROR_CODE = Pattern.compile("\"errorCode\"\\s*:\\s*\"([^\"]+)\"");

    private final MpesaConfig config;
    private final MpesaAuthService authService;
    private final WebClient mpesaWebClient;

    public MpesaStkPushQuery(MpesaConfig config, MpesaAuthService authService, WebClient mpesaWebClient) {
        this.config = config;
        this.authService = authService;
        this.mpesaWebClient = mpesaWebClient;
    }

    /**
     * Asks Safaricom what happened to a push. Throws MpesaQueryException when Daraja answers with an
     * HTTP error; Daraja does this while a payment is still being processed (and when it has no record),
     * so callers must treat the exception as "no answer yet", never as "failed".
     */
    public Map<String, Object> queryStatus(String checkoutRequestId) {
        String token = authService.generateAccessToken();
        // Same timezone as the STK push, so the timestamp and password agree.
        String timestamp = ZonedDateTime.now(NAIROBI).format(DARAJA_TS);
        String password = Base64.getEncoder().encodeToString(
                (config.getShortCode() + config.getPasskey() + timestamp).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("BusinessShortCode", config.getShortCode());
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("CheckoutRequestID", checkoutRequestId);

        return mpesaWebClient.post()
                .uri(config.getStkQueryUrl())
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                        res.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(err -> Mono.error(
                                        new MpesaQueryException(extractErrorCode(err), "Query failed: " + err))))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));
    }

    private static String extractErrorCode(String body) {
        Matcher m = ERROR_CODE.matcher(body == null ? "" : body);
        return m.find() ? m.group(1) : null;
    }
}