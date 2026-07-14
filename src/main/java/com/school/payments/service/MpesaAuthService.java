package com.school.payments.service;

import com.school.error.MpesaException;
import com.school.payments.MpesaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
public class MpesaAuthService {
    private final WebClient mpesaWebclient;
    private final MpesaConfig mpesaConfig;

    public MpesaAuthService(WebClient mpesaWebclient, MpesaConfig mpesaConfig) {
        this.mpesaWebclient = mpesaWebclient;
        this.mpesaConfig = mpesaConfig;
    }

    private volatile String cachedToken;
    private volatile Instant expiryTime = Instant.EPOCH;

    synchronized String generateAccessToken(){
        if(cachedToken !=null && Instant.now().isBefore(expiryTime)){
            return cachedToken;
        }
        String credentials = mpesaConfig.getConsumerKey()+ ":" +mpesaConfig
                .getConsumerSecret();

        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        Map<String,Object> body= mpesaWebclient.get()
                .uri(mpesaConfig.getAuthUrl())
                .header(HttpHeaders.AUTHORIZATION ,"Basic " +encodedCredentials)
                .retrieve()
                .onStatus(HttpStatusCode::isError,response->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody->{
                                    log.error("Access token could not be granted {}",errorBody);
                                    return Mono.error(new MpesaException("Mpesa Authentication failed"));
                                }))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(10));
        if(body == null && body.get("access_token") == null){
            throw new MpesaException("No token response from Safaricom");
        }
        cachedToken = body.get("access_token").toString();

        int expiresIn = Integer.parseInt(body.get("expires_in").toString());

        expiryTime =Instant.now().plusSeconds(expiresIn-60);

        return cachedToken;
    }
}
