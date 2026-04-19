package com.spotiapp.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "user_reputations")
public class UserReputation {

    @Id
    private Long id; // Same as User ID

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private int tasteScore = 0;   // Based on verified musical commonality
    private int curatorScore = 0; // Based on shares-from-posts
    private int signalScore = 0;  // Based on high-value interactions

    // ── Constructors ───────────────────────────────────────────────────────────

    public UserReputation() {}

    public UserReputation(User user) {
        this.user = user;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public int getTasteScore() { return tasteScore; }
    public void setTasteScore(int tasteScore) { this.tasteScore = tasteScore; }

    public int getCuratorScore() { return curatorScore; }
    public void setCuratorScore(int curatorScore) { this.curatorScore = curatorScore; }

    public int getSignalScore() { return signalScore; }
    public void setSignalScore(int signalScore) { this.signalScore = signalScore; }
}
