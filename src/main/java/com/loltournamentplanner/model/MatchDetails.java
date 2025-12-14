package com.loltournamentplanner.model;

import java.util.List;

public class MatchDetails {
    private final String matchId;
    private final String gameMode;
    private final long gameStartMillis;
    private final int gameDurationSeconds;
    private final List<MatchParticipant> participants;

    public MatchDetails(
            String matchId,
            String gameMode,
            long gameStartMillis,
            int gameDurationSeconds,
            List<MatchParticipant> participants
    ) {
        this.matchId = matchId;
        this.gameMode = gameMode;
        this.gameStartMillis = gameStartMillis;
        this.gameDurationSeconds = gameDurationSeconds;
        this.participants = participants;
    }

    public String getMatchId() {
        return matchId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public long getGameStartMillis() {
        return gameStartMillis;
    }

    public int getGameDurationSeconds() {
        return gameDurationSeconds;
    }

    public List<MatchParticipant> getParticipants() {
        return participants;
    }
}
