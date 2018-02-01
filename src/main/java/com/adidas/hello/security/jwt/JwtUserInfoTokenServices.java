package com.adidas.hello.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SigningKeyResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedPrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Extracts user info from the OAuth2.0 token when it is in JWT format. <br/>
 *
 * Adapted from org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices <br/>
 *
 * See also: https://tools.adidas-group.com/confluence/display/DEG/Validating+an+Access+Token#ValidatinganAccessToken-ValidatingaJWTusingJose4J
 */
public class JwtUserInfoTokenServices implements ResourceServerTokenServices {
    protected final Log logger = LogFactory.getLog(getClass());

    private final String clientId;
    private final String expectedIssuer;
    private final int allowedClockSkewSeconds;
    private final boolean requireExpirationDate;
    private final RestTemplate certificateEndpointRestTemplate;

    private AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();

    private PrincipalExtractor principalExtractor = new FixedPrincipalExtractor();

    public JwtUserInfoTokenServices(String clientId, String expectedIssuer, int allowedClockSkewSeconds, boolean requireExpirationDate, RestTemplate certificateEndpointRestTemplate) {
        this.clientId = clientId;
        this.expectedIssuer = expectedIssuer;
        this.allowedClockSkewSeconds = allowedClockSkewSeconds;
        this.requireExpirationDate = requireExpirationDate;
        this.certificateEndpointRestTemplate = certificateEndpointRestTemplate;
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        Map<String, Object> map = validateAndDecodeJwt(accessToken);
        if (map.containsKey("error")) {
            this.logger.debug("userinfo returned error: " + map.get("error"));
            throw new InvalidTokenException(accessToken);
        }
        return extractAuthentication(map);
    }


    private Map<String, Object> validateAndDecodeJwt(String accessToken) {
        this.logger.info("Getting user info from access token.");
        try {
            SigningKeyResolver x509SigningKeyResolver = new X509SigningKeyResolver(certificateEndpointRestTemplate);
            Claims jwtClaims = Jwts.parser()
                .setAllowedClockSkewSeconds(allowedClockSkewSeconds)
                .setSigningKeyResolver(x509SigningKeyResolver)
                .parseClaimsJws(accessToken)
                .getBody();
            if (!jwtClaims.getIssuer().equals(expectedIssuer)) {
                throw new JwtException("Invalid JWT issuer");
            }
            if (requireExpirationDate && jwtClaims.getExpiration() == null) {
                throw new JwtException("No JWT expiration date set!");
            }
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> jwtClaim : jwtClaims.entrySet()) {
                map.put(jwtClaim.getKey(), jwtClaim.getValue());
            }
            return map;
        } catch (JwtException e) {
            return Collections.singletonMap("error", "Could not fetch user details: " + e.getMessage());
        }
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");
    }

    protected Object getPrincipal(Map<String, Object> map) {
        Object principal = this.principalExtractor.extractPrincipal(map);
        return (principal == null ? "unknown" : principal);
    }

    private OAuth2Authentication extractAuthentication(Map<String, Object> map) {
        Object principal = getPrincipal(map);
        List<GrantedAuthority> authorities = this.authoritiesExtractor
            .extractAuthorities(map);
        OAuth2Request request = new OAuth2Request(null, this.clientId, null, true, null,
            null, null, null, null);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            principal, "N/A", authorities);
        token.setDetails(map);
        return new OAuth2Authentication(request, token);
    }
}
