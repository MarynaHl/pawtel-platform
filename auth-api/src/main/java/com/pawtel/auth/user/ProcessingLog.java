package com.pawtel.auth.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processing_log")
public class ProcessingLog {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "input_text", nullable = false)
    private String inputText;

    @Column(name = "output_text", nullable = false)
    private String outputText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ProcessingLog() {}

    public ProcessingLog(UUID userId, String inputText, String outputText, Instant createdAt) {
        this.userId = userId;
        this.inputText = inputText;
        this.outputText = outputText;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getInputText() { return inputText; }
    public String getOutputText() { return outputText; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUserId(UUID userId) { this.userId = userId; }
    public void setInputText(String inputText) { this.inputText = inputText; }
    public void setOutputText(String outputText) { this.outputText = outputText; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
