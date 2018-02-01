package com.adidas.hello.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

class X509SigningKeyResolver implements SigningKeyResolver {

    /**
     * Specified here:
     * https://tools.adidas-group.com/confluence/display/DEG/Validating+an+Access+Token#ValidatinganAccessToken-RetrievingtheVerificationCertificate
     *  "x5t" stands for "x509 certificate thumbprint"
     */
    private final String CERTIFICATE_THUMBPRINT_KEY = "x5t";
    private final RestTemplate certificateEndpoint;

    public X509SigningKeyResolver(RestTemplate certificateEndpoint) {
        this.certificateEndpoint = certificateEndpoint;
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, Claims claims) {
        String certificateThumbprint = String.valueOf(header.get(CERTIFICATE_THUMBPRINT_KEY));

        // URL specified here:
        // https://tools.adidas-group.com/confluence/display/DEG/Validating+an+Access+Token#ValidatinganAccessToken-RetrievingtheVerificationCertificate
        String certificatePEMString = certificateEndpoint
            .getForObject("/x5t?v={certificateThumbprint}", String.class, certificateThumbprint);
        try (InputStream inputStream = new ByteArrayInputStream(certificatePEMString.getBytes())) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
            return x509Certificate.getPublicKey();
        } catch (IOException e) {
            throw new IllegalStateException("Reading from byte array input stream failed.", e);
        } catch (CertificateException e) {
            throw new IllegalStateException("Reading certificate failed.", e);
        }
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, String plaintext) {
        throw new UnsupportedOperationException("Not supported: plaintext JWS. Only JSON payloads are accepted.");
    }
}
