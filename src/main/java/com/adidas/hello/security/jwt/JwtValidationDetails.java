package com.adidas.hello.security.jwt;

/**
 * Configuration class for JWT validation.
 */
public class JwtValidationDetails {
    private String expectedIssuer;
    private int allowedClockSkewSeconds;
    private boolean requireExpirationDate;
    private String certificateEndpoint;

    public String getExpectedIssuer() {
        return expectedIssuer;
    }

    public void setExpectedIssuer(String expectedIssuer) {
        this.expectedIssuer = expectedIssuer;
    }

    public int getAllowedClockSkewSeconds() {
        return allowedClockSkewSeconds;
    }

    public void setAllowedClockSkewSeconds(int allowedClockSkewSeconds) {
        this.allowedClockSkewSeconds = allowedClockSkewSeconds;
    }

    public boolean isRequireExpirationDate() {
        return requireExpirationDate;
    }

    public void setRequireExpirationDate(boolean requireExpirationDate) {
        this.requireExpirationDate = requireExpirationDate;
    }

    public String getCertificateEndpoint() {
        return certificateEndpoint;
    }

    public void setCertificateEndpoint(String certificateEndpoint) {
        this.certificateEndpoint = certificateEndpoint;
    }
}
