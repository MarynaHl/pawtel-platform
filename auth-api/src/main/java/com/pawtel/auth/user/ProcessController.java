package com.pawtel.auth.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.pawtel.auth.user.ProcessDtos.*;

@RestController
@RequestMapping("/api")
public class ProcessController {

    private final UserRepository userRepo;
    private final ProcessingLogRepository logRepo;
    private final RestTemplate http;

    @Value("${app.data-api-url}")
    private String dataApiUrl;

    @Value("${app.internal-token}")
    private String internalToken;

    public ProcessController(UserRepository userRepo, ProcessingLogRepository logRepo, RestTemplate http) {
        this.userRepo = userRepo;
        this.logRepo = logRepo;
        this.http = http;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessResponse> process(@Valid @RequestBody ProcessRequest req, Authentication auth) {

        String email = (String) auth.getPrincipal();
        var user = userRepo.findByEmail(email).orElseThrow();


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Token", internalToken);

        var body = Map.of("text", req.text());
        var entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = http.postForEntity(dataApiUrl + "/api/transform", entity, Map.class);
        String result = (String) resp.getBody().get("result");


        logRepo.save(new ProcessingLog(user.getId(), req.text(), result));


        return ResponseEntity.ok(new ProcessResponse(result));
    }
}
