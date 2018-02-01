package com.adidas.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentVariables {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariables.class);

    public static String getEnv(String envVar) {
        String value = System.getenv(envVar);
        if (value == null) {
            logger.warn("Environment variable was not set: " + envVar);
        }
        return value;
    }
}
