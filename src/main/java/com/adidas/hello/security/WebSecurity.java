package com.adidas.hello.security;

import com.adidas.hello.security.jwt.JWTAuthenticationFilter;
import com.adidas.hello.security.jwt.JWTLoginFilter;
import com.adidas.hello.security.jwt.JwtValidationDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;


// For details on the OAuth2 configuration, see https://spring.io/guides/tutorials/spring-boot-oauth2/#_social_login_manual
@EnableOAuth2Client
@Configuration
public class WebSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
            .authorizeRequests()

            .antMatchers("/", "/login**", "/logged-in", "/_manage/**")
            .permitAll()

            .and()
            .authorizeRequests()
            .antMatchers("/jwt/protected").authenticated()
            .antMatchers("/user").authenticated()

            .and().logout().logoutSuccessUrl("/").permitAll()
            // disable CSRF protection for the logout route
            .and().csrf().ignoringAntMatchers("/logout")
            // Disable CSRF for /jwt/*
            .and().csrf().ignoringAntMatchers("/jwt/*")

            .and()
            .addFilterBefore(new JWTLoginFilter("/jwt/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JWTAuthenticationFilter(), JWTLoginFilter.class)
            .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint());

    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();

        filters.add(new AdidasOAuth2Filter("/login/adidas-dev",
            oAuth2ClientContext,
            adidasDevClientConfig(),
            adidasDevJwtValidationConfig(),
            restTemplateBuilder));

        // Construct and add further OAuth2 connectors here (via filters.add(yourFilter))

        filter.setFilters(filters);
        return filter;
    }

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    /**
     * Load client configuration (redirect and token URIs, clientId/clientSecret)
     */
    @Bean
    @ConfigurationProperties("oauth2.adidas-dev.client")
    AuthorizationCodeResourceDetails adidasDevClientConfig() {
        return new AuthorizationCodeResourceDetails();
    }

    /**
     * Load JWT validation configuration (certificate endpoint, expected issuer)
     */
    @Bean
    @ConfigurationProperties("oauth2.adidas-dev.jwtValidation")
    JwtValidationDetails adidasDevJwtValidationConfig() {
        return new JwtValidationDetails();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser(DefaultUser.NAME)
            .password(DefaultUser.PASSWORD)
            .roles(DefaultUser.ROLE);
    }

}
