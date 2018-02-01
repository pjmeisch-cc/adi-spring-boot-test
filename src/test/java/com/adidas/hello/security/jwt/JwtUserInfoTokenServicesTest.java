package com.adidas.hello.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@TestConfiguration
public class JwtUserInfoTokenServicesTest {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    @MockBean
    private RestTemplate mockRestTemplate;
    private String clientId = getClass().getName();
    private String expectedIssuer = getClass().getName();
    private int allowedClockSkewSeconds = 30;
    private boolean requireExpirationDate = false;
    private JwtUserInfoTokenServices jwtUserInfoTokenServices;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PrivateKey otherPrivateKey;
    private X509Certificate x509Certificate;
    private String certificatePEMString;
    private String certificateThumbprint = "my-certificate-thumbprint";
    private String testUser = "test user";
    private Claims claims;
    private Map<String, Object> headerMap;
    private final Provider BC = new BouncyCastleProvider();

    @Before
    public void setUp() throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, IOException, CertificateException, SignatureException, ParseException, OperatorCreationException {
        Security.addProvider(BC);
        jwtUserInfoTokenServices = new JwtUserInfoTokenServices(
            clientId,
            expectedIssuer,
            allowedClockSkewSeconds,
            requireExpirationDate,
            mockRestTemplate
        );
        claims = new DefaultClaims();
        claims.put("name", testUser);

        // Generate keys
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        // Use small keysize since we don't actually care about the security here
        keyPairGenerator.initialize(512);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
        KeyPair otherKeyPair = keyPairGenerator.generateKeyPair();
        otherPrivateKey = otherKeyPair.getPrivate();

        // See http://www.bouncycastle.org/wiki/display/JA1/BC+Version+2+APIs#BCVersion2APIs-Certificate/CRLGeneration
        // and https://github.com/bcgit/bc-java/blob/master/pkix/src/test/java/org/bouncycastle/cert/test/CertTest.java#L2266
        X500Name x500Name = new X500NameBuilder(BCStyle.INSTANCE)
            .addRDN(BCStyle.CN, "Test CA Certificate")
            .build();
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(BC).build(privateKey);
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, 365);
        Date nowPlusOneYear = calendar.getTime();
        X509v1CertificateBuilder x509v1CertificateBuilder = new JcaX509v1CertificateBuilder(x500Name, BigInteger.ONE, now, nowPlusOneYear, x500Name, publicKey);
        x509Certificate = new JcaX509CertificateConverter().setProvider(BC).getCertificate(x509v1CertificateBuilder.build(contentSigner));
        certificatePEMString = getX509CertificateAsPEM(x509Certificate);

        headerMap = new HashMap<>();
        headerMap.put("x5t", certificateThumbprint);
    }

    @After
    public void tearDown() {
        Security.removeProvider(BC.getName());
    }

    private String getX509CertificateAsPEM(X509Certificate x509Certificate) throws CertificateEncodingException {
        byte[] certificateBytes = x509Certificate.getEncoded();
        Base64.Encoder encoder = Base64.getEncoder();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BEGIN_CERT);
        stringBuilder.append('\n');
        stringBuilder.append(encoder.encodeToString(certificateBytes));
        stringBuilder.append(END_CERT);
        return stringBuilder.toString();
    }

    private void givenApiCallRestTemplateWillReturnCertificatePEM() {
        given(mockRestTemplate.getForObject("/x5t?v={certificateThumbprint}", String.class, certificateThumbprint))
            .willReturn(certificatePEMString);
    }

    @Test
    public void shouldReturnCredentialsFromAValidJws() throws CertificateEncodingException {
        String token = Jwts.builder()
            .setClaims(claims)
            .setHeader(headerMap)
            .setIssuer(expectedIssuer)
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .compact();

        givenApiCallRestTemplateWillReturnCertificatePEM();

        OAuth2Authentication oAuth2Authentication = jwtUserInfoTokenServices.loadAuthentication(token);

        assertThat(oAuth2Authentication.getName(), is(equalTo(testUser)));
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldRejectAJwsSignedWithADifferentKey() throws CertificateEncodingException {
        String token = Jwts.builder()
            .setClaims(claims)
            .setHeader(headerMap)
            .setIssuer(expectedIssuer)
            .signWith(SignatureAlgorithm.RS256, otherPrivateKey)
            .compact();

        givenApiCallRestTemplateWillReturnCertificatePEM();

        jwtUserInfoTokenServices.loadAuthentication(token);
    }

    @Test(expected = InvalidTokenException.class)
    public void shouldRejectAJwsIssuedByADifferentIssuer() throws CertificateEncodingException {
        String token = Jwts.builder()
            .setClaims(claims)
            .setHeader(headerMap)
            .setIssuer(expectedIssuer + " NOT")
            .signWith(SignatureAlgorithm.RS256, privateKey)
            .compact();

        givenApiCallRestTemplateWillReturnCertificatePEM();

        jwtUserInfoTokenServices.loadAuthentication(token);
    }

}
