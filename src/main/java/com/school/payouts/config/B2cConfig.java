package com.school.payouts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.b2c")
@Getter
@Setter
public class B2cConfig {
    private String url;
    private String shortcode;
    private String initiatorName;
    private String initiatorPassword;
    private String certificatePath;
    private String callbackSecret;
    private String resultUrl;
    private String timeoutUrl;

    /** Called before every send, so a missing setting gives a clear message instead of a vague failure. */
    public void assertReady() {
        List<String> missing = new ArrayList<>();
        if (blank(url)) missing.add("url");
        if (blank(shortcode)) missing.add("shortcode");
        if (blank(initiatorName)) missing.add("initiator-name");
        if (blank(initiatorPassword)) missing.add("initiator-password");
        if (blank(certificatePath)) missing.add("certificate-path");
        if (blank(callbackSecret)) missing.add("callback-secret");
        if (!missing.isEmpty())
            throw new IllegalStateException("B2C is not configured. Missing: " + String.join(", ", missing));
        if (callbackSecret.length() < 24)
            throw new IllegalStateException("B2C_CALLBACK_SECRET must be at least 24 characters.");
        if (blank(resultUrl) || !resultUrl.startsWith("https://"))
            throw new IllegalStateException("MPESA_BASE_URL must be a public https address (your ngrok URL).");
    }

    private static boolean blank(String v) { return v == null || v.isBlank(); }
}