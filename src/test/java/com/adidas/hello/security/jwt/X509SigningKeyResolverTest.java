package com.adidas.hello.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@TestConfiguration
public class X509SigningKeyResolverTest {

    private final String CERTIFICATE_THUMBPRINT_FIELD = "x5t";

    @MockBean
    private RestTemplate mockRestTemplate;

    private X509SigningKeyResolver x509SigningKeyResolver;

    @Before
    public void setUp() {
        this.x509SigningKeyResolver = new X509SigningKeyResolver(mockRestTemplate);
    }

    @Test
    public void shouldResolveTheKeySpecifiedInTheJwsThumbprintHeaderEntry() throws CertificateException {
        // Given a Jws comprised of header and claims
        Map<String, Object> jwsHeaderMap = new HashMap<>();
        String certificateThumbprint = "myCertificateThumbprint";
        jwsHeaderMap.put(CERTIFICATE_THUMBPRINT_FIELD, certificateThumbprint);
        JwsHeader jwsHeader = new DefaultJwsHeader(jwsHeaderMap);
        Claims claims = new DefaultClaims();

        // And a x509 certificate
        String certificatePEMString = "-----BEGIN CERTIFICATE-----\n" +
            "MIIE8DCCAtigAwIBAgIGAVGrYouHMA0GCSqGSIb3DQEBDQUAMDkxCzAJBgNVBAYTAkRFMQ8wDQYD\n" +
            "VQQKEwZhZGlkYXMxGTAXBgNVBAMTEEpXVCBTaWduaW5nIENlcnQwHhcNMTUxMjE2MTUyMjIwWhcN\n" +
            "MTgxMjE1MTUyMjIwWjA5MQswCQYDVQQGEwJERTEPMA0GA1UEChMGYWRpZGFzMRkwFwYDVQQDExBK\n" +
            "V1QgU2lnbmluZyBDZXJ0MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAysFrzHoZ6IFy\n" +
            "+1FuNqv8Y2AQyxeoyEmv3BtscrX4tdT9bK1uylsfULTjbN+WsGV0ZKTRxo37Ll+LQpGfYkmgvMqW\n" +
            "G5yZY1dreCfoRvTuCahuPpDXRkZ10Kodn9MELVfblDafQWf5LNYue/icgdQwUDIpEwBMuJdFbRh8\n" +
            "hBY4pRZjKt6EJernMPhGWxFZ4YZd2ORZlA9JdCXCbHiFGL+oyR1E+mk4JRcd12NUQjYQoeA2+ruN\n" +
            "QQiYou6YPRoucXpQyRzje53J39XMKzFIaZukvNc2LHuRUjltAGtvPpjb51KOJqzb91nJwyiMxSB+\n" +
            "gQCBZQ26sAVmrSO6Fa7vwNgyne9y936nH6AtMGr0ZCvmUk4kXkBc1sWV0eVBI+iPxwd2vHnmIkbt\n" +
            "7qGyv5oxmTPABjHc2dEh/H66NOROL+JxhQq3yHh7ybyfvAWogpSkZE9Gh9+/2rIKWhjx3LX/xR3c\n" +
            "SVTofsrlKYV7N/IJSObFQYM31YDJ5AU4BfjIKolB8XRU1hhK7J+PYd57HkqadWGqgUXLftctpMML\n" +
            "GwvRAH/jeQwxCdYG9YORxse0S4v1jDj0BQCybb53NU1kn6MryNuYvW2b4bRcrFTlxlToVtrMJpjL\n" +
            "84U72ZT/8h3L0kjEgwUjJtB03tgPQGbubaIatrLdhJomtfrPvpe+Ik6QPfCx6eECAwEAATANBgkq\n" +
            "hkiG9w0BAQ0FAAOCAgEAa/b3wbatR+wt64GxXNvBvhByraxOz2+TTaSGSSJ2mTvAyZnra+PU7/ew\n" +
            "kG4S6CGhnb1beBfwWe60wLZ58fHHiL1PC6gU469t7fo9kHsP86Z3eyRApMHrPO5AqcC7Pj+Ad6on\n" +
            "NWAfdoVktzGjwqfoDUriZEyacyThybWbM2A1HCQLrp4yY39RWuwAiT0vIb3NbsAxY6TxCs7myGlU\n" +
            "aQbtXetIzbWT3SmoSoRb2Wh9ywzN5YWnudxAXdzmgu2mcT8LSgBGvZ/YbVrPAgpMLtaLQ6s9XZS8\n" +
            "JQVyEWbkJEzZzk2dqYeWce/yvKLrK0qCkiaU/OuHaZjorXXKU/2YHW/OLZt56N29y59RXXwsK4hl\n" +
            "TFyYuapvIQdOUc9gEZwPmvOsV+ULtjjzf6RPGdAL9VyLrfH6gIn0NdHRZOw8mpz9I1YyQd1KsGFB\n" +
            "mWfZFRlrfhybB2H2qG2Ce1bGDT14pYznCOYoIaTFlkh4q/AXYWeT9H8seWeNpkX2rkx5kIy8u70t\n" +
            "mybU34XX7mZbxpmTawj9SgY5voQTXOzTwwbRO74oK5JoTa3xDWcxTyr6XfEIqHxyyFabzgOEcZLX\n" +
            "S/rrwkCZFXYu0FG85cZgwZzApd8oEuSPipHB8wMYn9uRaWFc0LqZEb6ZMpUO4FRQJRTZ3kDiwgXO\n" +
            "fQpM15PeeH8Qw+7wA+k=\n" +
            "-----END CERTIFICATE-----";
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(certificatePEMString.getBytes());
        X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);

        given(mockRestTemplate.getForObject("/x5t?v={certificateThumbprint}", String.class, certificateThumbprint))
            .willReturn(certificatePEMString);

        // When the resolver resolves the key
        Key key = x509SigningKeyResolver.resolveSigningKey(jwsHeader, claims);

        // The correct key is returned
        assertThat(key, is(equalTo(x509Certificate.getPublicKey())));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailWhenAnInvalidCertificateIsReturnedFromTheEndpoint() throws CertificateException {
        // Given a Jws comprised of header and claims
        Map<String, Object> jwsHeaderMap = new HashMap<>();
        String certificateThumbprint = "myCertificateThumbprint";
        jwsHeaderMap.put(CERTIFICATE_THUMBPRINT_FIELD, certificateThumbprint);
        JwsHeader jwsHeader = new DefaultJwsHeader(jwsHeaderMap);
        Claims claims = new DefaultClaims();

        String emptyCertificate = "-----BEGIN CERTIFICATE-----\n" + "-----END CERTIFICATE-----";
        given(mockRestTemplate.getForObject("/x5t?v={certificateThumbprint}", String.class, certificateThumbprint))
            .willReturn(emptyCertificate);

        x509SigningKeyResolver.resolveSigningKey(jwsHeader, claims);
    }

}
