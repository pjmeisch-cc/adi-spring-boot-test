package com.adidas.hello.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class TokenAuthenticationService {

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String SECRET = "my-secret";

    void addAuthentication(HttpServletResponse response, String username) {
        String token = Jwts.builder()
            .setSubject(username)
            .signWith(SignatureAlgorithm.HS256, SECRET)
            .compact();
        response.addHeader(HEADER_NAME, TOKEN_PREFIX + token);
    }

    Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_NAME);
        if (token != null) {
            try {
                String username = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.substring(TOKEN_PREFIX.length()))
                    .getBody()
                    .getSubject();
                if (username != null) {
                    return new AuthenticatedUser(username);
                }
            } catch (SignatureException e) {
                return null;
            }
        }
        return null;
    }
}
