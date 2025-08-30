package com.pawtel.data.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record TransformRequest(String text) {}
record TransformResponse(String result) {}

@RestController
@RequestMapping("/api/transform")
public class TransformController {

    @Value("${internal.token:topsecret}")
    private String internalToken;

    @PostMapping
    public ResponseEntity<?> transform(
            @RequestHeader(value = "X-Internal-Token", required = false) String token,
            @RequestBody TransformRequest req
    ) {
        if (token == null || !token.equals(internalToken)) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String out = new StringBuilder(req.text()).reverse().toString().toUpperCase();
        return ResponseEntity.ok(new TransformResponse(out));
    }
}
