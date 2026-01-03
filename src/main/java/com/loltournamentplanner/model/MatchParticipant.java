package com.loltournamentplanner.model;

public class MatchParticipant {
    private final String summonerName;
    private final String championName;
    private final int kills;
    private final int deaths;
    private final int assists;
    private final int cs;
    private final int gold;
    private final boolean win;

    public MatchParticipant(
            String summonerName,
            String championName,
            int kills,
            int deaths,
            int assists,
            int cs,
            int gold,
            boolean win
    ) {
        this.summonerName = summonerName;
        this.championName = championName;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.cs = cs;
        this.gold = gold;
        this.win = win;
    }

    public String getSummonerName() {
        return summonerName;
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
