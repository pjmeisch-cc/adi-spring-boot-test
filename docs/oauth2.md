# OAuth2.0 configuration

A sample setup for Adidas SSO ([PingFederate](https://tools.adidas-group.com/confluence/display/DEG/PingFederate+Details), `DEV` environment) using
OAuth2.0 (Authorization Code flow) is included in the project. It is configured via the `oauth2` section of the [application.yml](../src/main/resources/application.yml)

The integration with Spring Boot is configured in [WebSecurity](../src/main/java/com/adidas/hello/security/WebSecurity.java) in the `ssoFilter` function.
The validation, decoding and extraction of user information from the JWT token returned by PingFederate is implemented in the class
[JwtUserInfoTokenServices](../src/main/java/com/adidas/hello/security/jwt/JwtUserInfoTokenServices.java). The fetching and decoding of the X509 certificate issued by PingFederate and
required for token validation is implemented in the class [X509SigningKeyResolver](../src/main/java/com/adidas/hello/security/jwt/X509SigningKeyResolver.java).

OAuth2.0 authorization is performed as follows:

1. User accesses the endpoint `/login/adidas-dev`
2. The [AdidasOAuth2Filter](../src/main/java/com/adidas/hello/security/AdidasOAuth2Filter.java) in `ssoFilter` in the [WebSecurity](../src/main/java/com/adidas/hello/security/WebSecurity.java)
 config redirects the user to the `oauth2.adidas-dev.client.userAuthorizationUri` specified in the [application.yml](../src/main/resources/application.yml)
3. The user confirms the authorization request and is redirected back to `/login/adidas-dev` with an authorization code as a query parameter (e.g. ?code=ssv0VMyMXWtA6WptTEDPYNMGE64oaYW8p6UXuvD3&state=UFOp7N)
4. The [AdidasOAuth2Filter](../src/main/java/com/adidas/hello/security/AdidasOAuth2Filter.java) fetches a JWT token from the `oauth2.adidas-dev.client.accessTokenUri` using the authorization code.
5. The [AdidasOAuth2Filter](../src/main/java/com/adidas/hello/security/AdidasOAuth2Filter.java) uses a [JwtUserInfoTokenServices](../src/main/java/com/adidas/hello/security/jwt/JwtUserInfoTokenServices.java) instance
configured via `oauth2.adidas-dev.jwtValidation` in the [application.yml](../src/main/resources/application.yml) to extract user information from the JWT token:

    1. An [X509SigningKeyResolver](../src/main/java/com/adidas/hello/security/jwt/X509SigningKeyResolver.java) is created with a REST client (a Spring Boot `RestTemplate`) set
    to point at the certificate resource specified in `oauth2.adidas-dev.jwtValidation.certificateEndpoint`.
    2. The JWT is decoded and validated using the `jjwt` library with the `X509SigningKeyResolver` as the signing key resolver:

        - `jjwt` invokes the `resolveSigningKey` of the `X509SigningKeyResolver` to obtain a public key to check the JWT's validity.
        - `X509SigningKeyResolver` uses its REST client to access the PingFederate certificate endpoint and fetches a certificate for the thumbprint specified in the JWT header.
        - `jjwt` uses the public key extracted from the certificate to validate the token.
    3. `JwtUserInfoTokenServices` extracts a set of `Authorities` and a `Principal` from the JWT payload to construct an `OAuthAuthentication`
    4. The `OAuthAuthentication` is returned to Spring Security.
6. The user is forwarded to the resource he wished to access.
