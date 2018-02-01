package com.adidas.hello;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {
    @RequestMapping("/jwt/protected")
    public String getProtected() {
        return "protected";
    }
}
