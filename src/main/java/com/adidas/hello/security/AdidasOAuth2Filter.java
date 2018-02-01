package com.adidas.hello.security;

import com.adidas.hello.security.jwt.JwtUserInfoTokenServices;
import com.adidas.hello.security.jwt.JwtValidationDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.web.client.RestTemplate;

class AdidasOAuth2Filter extends OAuth2ClientAuthenticationProcessingFilter {

    AdidasOAuth2Filter(String handlerUrl,
                       OAuth2ClientContext clientContext,
                       AuthorizationCodeResourceDetails clientConfig,
                       JwtValidationDetails jwtValidationConfig,
                       RestTemplateBuilder restTemplateBuilder) {
        super(handlerUrl);

        setRestTemplate(new OAuth2RestTemplate(clientConfig, clientContext));

        RestTemplate certificateEndpoint = restTemplateBuilder
            .rootUri(jwtValidationConfig.getCertificateEndpoint())
            .build();

        JwtUserInfoTokenServices tokenServices = new JwtUserInfoTokenServices(
            clientConfig.getClientId(),
            jwtValidationConfig.getExpectedIssuer(),
            jwtValidationConfig.getAllowedClockSkewSeconds(),
            jwtValidationConfig.isRequireExpirationDate(),
            certificateEndpoint);

        setTokenServices(tokenServices);
    }
}
