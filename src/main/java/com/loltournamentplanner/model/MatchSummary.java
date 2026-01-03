package com.loltournamentplanner.model;

public class MatchSummary {
    private final String matchId;
    private final String gameMode;
    private final long gameStartMillis;
    private final int gameDurationSeconds;

    private final String championName;
    private final int kills;
    private final int deaths;
    private final int assists;
    private final int cs;
    private final int gold;
    private final boolean win;

    public MatchSummary(
            String matchId,
            String gameMode,
            long gameStartMillis,
            int gameDurationSeconds,
            String championName,
            int kills,
            int deaths,
            int assists,
            int cs,
            int gold,
            boolean win
    ) {
        this.matchId = matchId;
        this.gameMode = gameMode;
        this.gameStartMillis = gameStartMillis;
        this.gameDurationSeconds = gameDurationSeconds;
        this.championName = championName;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.cs = cs;
        this.gold = gold;
        this.win = win;
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

    public String getChampionName() {
        return championName;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public int getCs() {
        return cs;
    }

    public int getGold() {
        return gold;
    }

    public boolean isWin() {
        return win;
    }
}
