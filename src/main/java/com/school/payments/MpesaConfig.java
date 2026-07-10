package com.school.payments;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Data
@ConfigurationProperties(prefix = "mpesa")
@Validated
public class MpesaConfig {
    @NotBlank
    private String consumerKey;
    @NotBlank
    private String consumerSecret;
    @NotBlank
    private String shortCode;
    @NotBlank
    private String passkey;
    @NotBlank
    private String callbackUrl;
    @NotBlank
    private String authUrl;
    @NotBlank
    private String stkPushUrl;
    @NotBlank
    private String stkQueryUrl;
}
