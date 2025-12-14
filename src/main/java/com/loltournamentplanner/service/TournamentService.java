package com.loltournamentplanner.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.Tournament;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TournamentService {
    private static final String CSV_FILE = "tournaments.csv";
    private static final String DELIMITER = ";";
    private final Gson gson;

    public TournamentService() {
        this.gson = new Gson();
        ensureCsvExists();
    }

    private void ensureCsvExists() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Tournament> getAllTournaments() {
        List<Tournament> tournaments = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Path.of(CSV_FILE));
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 4) {
                    String id = parts[0];
                    String name = parts[1];
                    String status = parts[2];
                    String participantsJson = parts[3];
                    String description = parts.length >= 5 ? decode(parts[4]) : "";
                    String startDate = parts.length >= 6 ? decode(parts[5]) : "";
                    int maxParticipants = parts.length >= 7 ? safeParseInt(parts[6], 0) : 0;

                    Type listType = new TypeToken<List<String>>(){}.getType();
                    List<String> participantPuuids = gson.fromJson(participantsJson, listType);
                    
                    if (participantPuuids == null) {
                        participantPuuids = new ArrayList<>();
                    }

                    tournaments.add(new Tournament(id, name, status, participantPuuids, description, startDate, maxParticipants));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tournaments;
    }

    public void joinTournament(Tournament tournament, LoLAccount account) throws IOException {
        List<Tournament> allTournaments = getAllTournaments();
        boolean updated = false;

        for (Tournament t : allTournaments) {
            if (t.getId().equals(tournament.getId())) {
                if (t.getStatus() != null && "CLOSED".equalsIgnoreCase(t.getStatus())) {
                    throw new IOException("Tournament is closed");
                }
                if (!t.getParticipantPuuids().contains(account.getPuuid())) {
                    t.addParticipant(account.getPuuid());
                    updated = true;
                }
                break;
            }
        }

        if (updated) {
            saveAllTournaments(allTournaments);
        }
    }

    private void saveAllTournaments(List<Tournament> tournaments) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Tournament t : tournaments) {
            String participantsJson = gson.toJson(t.getParticipantPuuids());
            sb.append(t.getId()).append(DELIMITER)
              .append(t.getName()).append(DELIMITER)
              .append(t.getStatus()).append(DELIMITER)
              .append(participantsJson).append(DELIMITER)
              .append(encode(nullToEmpty(t.getDescription()))).append(DELIMITER)
              .append(encode(nullToEmpty(t.getStartDate()))).append(DELIMITER)
              .append(t.getMaxParticipants()).append(System.lineSeparator());
        }
        Files.writeString(Path.of(CSV_FILE), sb.toString(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
