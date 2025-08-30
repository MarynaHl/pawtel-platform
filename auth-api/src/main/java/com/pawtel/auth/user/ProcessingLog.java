package com.pawtel.auth.user;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_log")
public class ProcessingLog {
    @Id @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 2000)
    private String inputText;

    @Column(nullable = false, length = 2000)
    private String outputText;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // getters/setters/constructors
    public ProcessingLog() {}
    public ProcessingLog(UUID userId, String inputText, String outputText) {
        this.userId = userId;
        this.inputText = inputText;
        this.outputText = outputText;
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getInputText() { return inputText; }
    public String getOutputText() { return outputText; }
    public Instant getCreatedAt() { return createdAt; }
}
