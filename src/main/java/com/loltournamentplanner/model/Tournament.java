package com.loltournamentplanner.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tournament {
    private String id;
    private String name;
    private String status;
    private List<String> participantPuuids;

    public Tournament(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.status = "OPEN";
        this.participantPuuids = new ArrayList<>();
    }

    public Tournament(String id, String name, String status, List<String> participantPuuids) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.participantPuuids = participantPuuids;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getParticipantPuuids() { return participantPuuids; }
    public void setParticipantPuuids(List<String> participantPuuids) { this.participantPuuids = participantPuuids; }
    
    public void addParticipant(String puuid) {
        this.participantPuuids.add(puuid);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %d Participants", name, status, participantPuuids.size());
    }
}
