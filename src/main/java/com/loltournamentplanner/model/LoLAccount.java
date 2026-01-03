package com.loltournamentplanner.model;

public class LoLAccount {
    private String puuid;
    private String gameName;
    private String tagLine;
    private int summonerLevel;
    private String tier;
    private String rank;
    private int leaguePoints;
    private String lastUpdated;

    public LoLAccount(String puuid, String gameName, String tagLine, int summonerLevel, String tier, String rank, int leaguePoints) {
        this.puuid = puuid;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.summonerLevel = summonerLevel;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.lastUpdated = "";
    }

    public LoLAccount(String puuid, String gameName, String tagLine, int summonerLevel, String tier, String rank, int leaguePoints, String lastUpdated) {
        this.puuid = puuid;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.summonerLevel = summonerLevel;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getPuuid() { return puuid; }
    public void setPuuid(String puuid) { this.puuid = puuid; }
    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }
    public String getTagLine() { return tagLine; }
    public void setTagLine(String tagLine) { this.tagLine = tagLine; }
    public int getSummonerLevel() { return summonerLevel; }
    public void setSummonerLevel(int summonerLevel) { this.summonerLevel = summonerLevel; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public int getLeaguePoints() { return leaguePoints; }
    public void setLeaguePoints(int leaguePoints) { this.leaguePoints = leaguePoints; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public String toString() {
        if ("UNRANKED".equals(tier) || tier == null) {
            return String.format("%s #%s [UNRANKED]", gameName, tagLine);
        }
        return String.format("%s #%s [%s %s - %d LP]", gameName, tagLine, tier, rank, leaguePoints);
    }
}
