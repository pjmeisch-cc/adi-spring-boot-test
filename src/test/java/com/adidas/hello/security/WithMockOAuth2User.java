package com.adidas.hello.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOAuth2UserSecurityContextFactory.class)
public @interface WithMockOAuth2User {
    String username() default "user";

    String clientId() default "dummyClientId";

    String redirectUri() default "http://example.com";

    String detailName() default "mockDetailName";
}
