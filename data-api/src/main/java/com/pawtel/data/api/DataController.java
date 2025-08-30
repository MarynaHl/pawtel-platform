package com.pawtel.data.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DataController {


    @GetMapping("/api/data/me")
    public Map<String, String> me(Authentication auth) {
        String email = (auth != null) ? auth.getName() : "anonymous";
        return Map.of("email", email);
    }
}
