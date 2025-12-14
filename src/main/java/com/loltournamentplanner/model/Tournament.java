package com.loltournamentplanner.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Tournament {
    private String id;
    private String name;
    private String status;
    private List<String> participantPuuids;
    private String description;
    private String startDate;
    private int maxParticipants;

    public Tournament(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.status = "OPEN";
        this.participantPuuids = new ArrayList<>();
        this.description = "";
        this.startDate = "";
        this.maxParticipants = 0;
    }

    public Tournament(String id, String name, String status, List<String> participantPuuids) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.participantPuuids = participantPuuids;
        this.description = "";
        this.startDate = "";
        this.maxParticipants = 0;
    }

    public Tournament(String id, String name, String status, List<String> participantPuuids, String description, String startDate, int maxParticipants) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.participantPuuids = participantPuuids;
        this.description = description;
        this.startDate = startDate;
        this.maxParticipants = maxParticipants;
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public void addParticipant(String puuid) {
        this.participantPuuids.add(puuid);
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %d Participants", name, status, participantPuuids.size());
    }
}
