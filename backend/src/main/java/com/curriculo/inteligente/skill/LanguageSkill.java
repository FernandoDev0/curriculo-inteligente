package com.curriculo.inteligente.skill;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "language_skill")
public class LanguageSkill {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID userId;
    @Column(nullable = false, length = 40) private String name;
    private String comprehension;
    private String reading;
    private String writing;
    private String conversation;
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getComprehension() { return comprehension; }
    public void setComprehension(String comprehension) { this.comprehension = comprehension; }
    public String getReading() { return reading; }
    public void setReading(String reading) { this.reading = reading; }
    public String getWriting() { return writing; }
    public void setWriting(String writing) { this.writing = writing; }
    public String getConversation() { return conversation; }
    public void setConversation(String conversation) { this.conversation = conversation; }
}
