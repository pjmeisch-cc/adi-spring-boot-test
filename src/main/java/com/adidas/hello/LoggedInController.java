package com.adidas.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class LoggedInController {
    @GetMapping("/logged-in")
    boolean user(Principal principal) {
        return principal != null;
    }
}
