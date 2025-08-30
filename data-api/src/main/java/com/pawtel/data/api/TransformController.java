package com.pawtel.data.api;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record TransformRequest(@NotBlank String text) {}
record TransformResponse(String result) {}

@RestController
@RequestMapping("/api/transform")
public class TransformController {

    @PostMapping
    public ResponseEntity<TransformResponse> transform(@RequestBody TransformRequest req) {

        String out = new StringBuilder(req.text()).reverse().toString().toUpperCase();
        return ResponseEntity.ok(new TransformResponse(out));
    }
}
