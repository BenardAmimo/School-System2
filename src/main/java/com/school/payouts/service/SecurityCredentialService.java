package com.school.payouts.service;

import com.school.error.MpesaException;
import com.school.payouts.config.B2cConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class SecurityCredentialService {
    private final B2cConfig config;

    /** The initiator password, RSA-encrypted with Safaricom's certificate, base64. */
    public String build() {
        try (InputStream in = Files.newInputStream(Path.of(config.getCertificatePath()))) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
            return Base64.getEncoder().encodeToString(
                    cipher.doFinal(config.getInitiatorPassword().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            // Deliberately no detail: nothing about the password may reach a log.
            throw new MpesaException("Could not build the B2C security credential. Check the certificate path and file.");
        }
    }
}