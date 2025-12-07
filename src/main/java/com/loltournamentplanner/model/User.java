package com.loltournamentplanner.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String password;
    private List<LoLAccount> linkedAccounts;

    public User(UUID id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.linkedAccounts = new ArrayList<>();
    }

    public User(String username, String password) {
        this(UUID.randomUUID(), username, password);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<LoLAccount> getLinkedAccounts() { return linkedAccounts; }
    public void setLinkedAccounts(List<LoLAccount> linkedAccounts) { this.linkedAccounts = linkedAccounts; }
    
    public void addAccount(LoLAccount account) {
        this.linkedAccounts.add(account);
    }
}
