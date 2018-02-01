package com.adidas.hello;

import com.adidas.hello.security.jwt.AccountCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginTestCommon {
    // Returns the JSON object {"username":"<username>,"password":"<password>"} as a string.
    public static String jsonCredentials(String username, String password) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        AccountCredentials testCredentials = new AccountCredentials();
        testCredentials.setUsername(username);
        testCredentials.setPassword(password);
        return objectMapper.writeValueAsString(testCredentials);
    }


}
