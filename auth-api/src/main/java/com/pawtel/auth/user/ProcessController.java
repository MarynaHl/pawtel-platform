package com.pawtel.auth.user;

import com.pawtel.auth.user.dto.ProcessRequest;
import com.pawtel.auth.user.dto.ProcessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProcessController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProcessingLogRepository processingLogRepository;

    @Value("${data.api.url:http://localhost:8081}")
    private String dataApiUrl;

    @Value("${internal.token:dev-internal-secret}")
    private String internalToken;

    public ProcessController(RestTemplate restTemplate,
                             UserRepository userRepository,
                             ProcessingLogRepository processingLogRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.processingLogRepository = processingLogRepository;
    }

    @PostMapping("/process")
    public ResponseEntity<Map<String,String>> process(@RequestBody ProcessRequest body,
                                                      Authentication auth) {

        String email = auth.getName();
        var user = userRepository.findByEmail(email).orElseThrow();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Token", internalToken);
        HttpEntity<ProcessRequest> entity = new HttpEntity<>(body, headers);

        ResponseEntity<ProcessResponse> resp = restTemplate
                .postForEntity(dataApiUrl + "/api/transform", entity, ProcessResponse.class);

        String result = resp.getBody() != null ? resp.getBody().result() : "";


        var log = new ProcessingLog(user.getId(), body.text(), result, Instant.now());
        processingLogRepository.save(log);

        return ResponseEntity.ok(Map.of("result", result));
    }
}
