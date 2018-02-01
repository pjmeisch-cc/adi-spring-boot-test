package com.adidas.hello.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;

import static java.util.Collections.*;

public class WithMockOAuth2UserSecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2User> {

    @Override
    public SecurityContext createSecurityContext(WithMockOAuth2User withMockOAuth2User) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        OAuth2Request dummyRequest = new OAuth2Request(
            emptyMap(),
            withMockOAuth2User.clientId(),
            emptyList(),
            true,
            emptySet(),
            emptySet(),
            withMockOAuth2User.redirectUri(),
            emptySet(),
            emptyMap()
        );
        UserDetails principal = new User(withMockOAuth2User.username(), "password", emptyList());

        UsernamePasswordAuthenticationToken userAuthentication =
            new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),
                principal.getAuthorities());

        userAuthentication.setDetails(Collections.singletonMap("name", withMockOAuth2User.detailName()));
        Authentication authentication = new OAuth2Authentication(dummyRequest, userAuthentication);

        context.setAuthentication(authentication);
        return context;
    }
}
