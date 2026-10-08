package com.school.payments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @Data was replaced by @Getter/@Setter: @Data generates toString(), which would print
 * consumerSecret and passkey the first time anyone logs this object.
 */
@Configuration
@Getter
@Setter
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

    // Must be the secret URL: https://your-host/stk/callback/{mpesa.callback.secret}
    // The app refuses to start with the old unprotected "/stk/callback" URL.
    @NotBlank
    @Pattern(regexp = "^https://.+/stk/callback/[^/]{24,}$",
            message = "callbackUrl must be https and end with /stk/callback/{secret} (24+ characters)")
    private String callbackUrl;

    @NotBlank
    private String authUrl;
    @NotBlank
    private String stkPushUrl;
    @NotBlank
    private String stkQueryUrl;

    @Override
    public String toString() {
        return "MpesaConfig(shortCode=" + shortCode + ", secrets hidden)";
    }
}