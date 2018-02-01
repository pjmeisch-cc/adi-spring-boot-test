package com.adidas.hello.catalog;

import com.adidas.hello.EnvironmentVariables;

import java.net.URI;
import java.net.URISyntaxException;

import static java.lang.Integer.parseInt;

public class CatalogURI {

    static String scheme = EnvironmentVariables.getEnv("K8S_CATALOG_SERVICE_PROTOCOL");
    static String host = EnvironmentVariables.getEnv("K8S_CATALOG_SERVICE_HOST");
    static String servicePort = EnvironmentVariables.getEnv("K8S_CATALOG_SERVICE_PORT");

    public static URI getProductsURI() throws URISyntaxException {
        int port = 0;
        if (servicePort != null) {
            port = parseInt(servicePort);
        }

        String emptyUserInfo = null;
        String path = "/products";
        String emptyQuery = null;
        String emptyFragment = null;
        return new URI(
            scheme,
            emptyUserInfo,
            host,
            port,
            path,
            emptyQuery,
            emptyFragment
        );
    }

}
