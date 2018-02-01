package com.adidas.hello;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {
    @GetMapping("/user")
    Object user(OAuth2Authentication authentication) {
        return extractNameFromOAuth2Authentication(authentication);
    }

    private String getStringFromObjectMap(Map map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value.getClass() == String.class) {
            return (String) value;
        }
        return null;
    }

    private String extractNameFromOAuth2Authentication(OAuth2Authentication authentication) {
        // Returns the details obtained from the OAuth2.0 endpoint
        Object details = authentication.getUserAuthentication().getDetails();
        if (details instanceof Map) {
            Map detailsMap = (Map) details;
            String name = getStringFromObjectMap(detailsMap, "name");
            String firstName = getStringFromObjectMap(detailsMap, "first_name");
            String lastName = getStringFromObjectMap(detailsMap, "last_name");
            String socialFirstName = getStringFromObjectMap(detailsMap, "social.first_name");
            String socialLastName = getStringFromObjectMap(detailsMap, "social.last_name");
            if (name != null) {
                return name;
            } else if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            } else if (socialFirstName != null && socialLastName != null) {
                return socialFirstName + " " + socialLastName;
            } else {
                return "Unknown";
            }
        }
        return "";
    }
}
