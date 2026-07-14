package com.school.payments.service;

import com.school.error.MpesaException;
import com.school.payments.MpesaConfig;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
@Service
public class MpesaStkPushQuery {
    private final MpesaConfig config;
    private final MpesaAuthService authService;
    private final WebClient mpesaWebClient;

    public MpesaStkPushQuery(MpesaConfig config, MpesaAuthService authService, WebClient mpesaWebClient) {
        this.config = config;
        this.authService = authService;
        this.mpesaWebClient = mpesaWebClient;
    }

    public Map<String, Object> queryStatus(String checkoutRequestId) {
        String token = authService.generateAccessToken();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String password = Base64.getEncoder().encodeToString(
                (config.getShortCode() + config.getPasskey() + timestamp).getBytes(StandardCharsets.UTF_8)
        );

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
                                .flatMap(err -> Mono.error(new MpesaException("Query failed: " + err))))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));
    }
}
