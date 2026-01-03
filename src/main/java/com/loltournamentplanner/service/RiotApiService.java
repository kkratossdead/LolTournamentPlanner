package com.loltournamentplanner.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.MatchDetails;
import com.loltournamentplanner.model.MatchParticipant;
import com.loltournamentplanner.model.MatchSummary;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class RiotApiService {
    private static final String API_KEY = "RGAPI-129845ba-e91c-44fb-b1ca-898a50383e19";
    private static final String REGION_URL = "https://europe.api.riotgames.com";
    private final HttpClient httpClient;
    private final Gson gson;

    public RiotApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public LoLAccount verifyAndLinkAccount(String gameName, String tagLine) throws IOException, InterruptedException {
        String encodedGameName = URLEncoder.encode(gameName, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedTagLine = URLEncoder.encode(tagLine, StandardCharsets.UTF_8).replace("+", "%20");
        
        String accountUrl = String.format("%s/riot/account/v1/accounts/by-riot-id/%s/%s", 
                REGION_URL, encodedGameName, encodedTagLine);

        JsonObject accountJson = fetchJson(accountUrl);
        if (!accountJson.has("puuid")) {
            throw new IOException("Account JSON missing PUUID: " + accountJson);
        }
        String puuid = accountJson.get("puuid").getAsString();
        String returnedGameName = accountJson.get("gameName").getAsString();
        String returnedTagLine = accountJson.get("tagLine").getAsString();

        String[] platforms = {"euw1"};
        JsonObject summonerJson = null;
        String activePlatformUrl = null;

        for (String platform : platforms) {
            String platformUrl = "https://" + platform + ".api.riotgames.com";
            String summonerUrl = String.format("%s/lol/summoner/v4/summoners/by-puuid/%s", 
                    platformUrl, puuid);
            
            try {
                JsonObject response = fetchJson(summonerUrl);
                summonerJson = response;
                activePlatformUrl = platformUrl;
                break; 
            } catch (IOException e) {
                System.out.println("Failed to find summoner on " + platform + ": " + e.getMessage());
            }
        }

        if (summonerJson == null) {
             throw new IOException("Summoner not found in EUW1 or EUN1 regions.");
        }

        int summonerLevel = summonerJson.has("summonerLevel") ? summonerJson.get("summonerLevel").getAsInt() : 0;
        
        String tier = "UNRANKED";
        String rank = "";
        int leaguePoints = 0;

        String leagueUrl = String.format("%s/lol/league/v4/entries/by-puuid/%s", 
                activePlatformUrl, puuid);
        
        try {
            JsonArray leagueEntries = fetchJsonArray(leagueUrl);
            if (leagueEntries != null) {
                for (JsonElement element : leagueEntries) {
                    JsonObject entry = element.getAsJsonObject();
                    if ("RANKED_SOLO_5x5".equals(entry.get("queueType").getAsString())) {
                        tier = entry.get("tier").getAsString();
                        rank = entry.get("rank").getAsString();
                        leaguePoints = entry.get("leaguePoints").getAsInt();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to fetch league entries by PUUID: " + e.getMessage());
        }

        String lastUpdated = OffsetDateTime.now().toString();
        return new LoLAccount(puuid, returnedGameName, returnedTagLine, summonerLevel, tier, rank, leaguePoints, lastUpdated);
    }

    public List<String> getLastMatchIds(String puuid, int count) throws IOException, InterruptedException {
        int safeCount = Math.max(1, Math.min(count, 20));
        String url = String.format("%s/lol/match/v5/matches/by-puuid/%s/ids?start=0&count=%d", REGION_URL, puuid, safeCount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Riot-Token", API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("API Request failed. Status: " + response.statusCode() + " URL: " + url + " Body: " + response.body());
        }

        JsonArray ids = gson.fromJson(response.body(), JsonArray.class);
        List<String> matchIds = new ArrayList<>();
        if (ids != null) {
            for (JsonElement el : ids) {
                matchIds.add(el.getAsString());
            }
        }
        return matchIds;
    }

    public MatchDetails getMatchDetails(String matchId) throws IOException, InterruptedException {
        String url = String.format("%s/lol/match/v5/matches/%s", REGION_URL, matchId);
        JsonObject root = fetchJson(url);

        JsonObject info = root.getAsJsonObject("info");
        String gameMode = info != null && info.has("gameMode") ? info.get("gameMode").getAsString() : "";
        long gameStartMillis = info != null && info.has("gameStartTimestamp") ? info.get("gameStartTimestamp").getAsLong() : 0L;
        int gameDurationSeconds = info != null && info.has("gameDuration") ? info.get("gameDuration").getAsInt() : 0;

        List<MatchParticipant> participants = new ArrayList<>();
        if (info != null && info.has("participants") && info.get("participants").isJsonArray()) {
            JsonArray arr = info.getAsJsonArray("participants");
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject p = el.getAsJsonObject();
                participants.add(parseParticipant(p));
            }
        }

        return new MatchDetails(matchId, gameMode, gameStartMillis, gameDurationSeconds, participants);
    }

    public List<MatchSummary> getLastMatchSummariesForPuuid(String puuid, int count) throws IOException, InterruptedException {
        List<String> ids = getLastMatchIds(puuid, count);
        List<MatchSummary> summaries = new ArrayList<>();
        for (String matchId : ids) {
            summaries.add(getMatchSummaryForPuuid(matchId, puuid));
        }
        return summaries;
    }

    private MatchSummary getMatchSummaryForPuuid(String matchId, String puuid) throws IOException, InterruptedException {
        String url = String.format("%s/lol/match/v5/matches/%s", REGION_URL, matchId);
        JsonObject root = fetchJson(url);
        JsonObject info = root.getAsJsonObject("info");

        String gameMode = info != null && info.has("gameMode") ? info.get("gameMode").getAsString() : "";
        long gameStartMillis = info != null && info.has("gameStartTimestamp") ? info.get("gameStartTimestamp").getAsLong() : 0L;
        int gameDurationSeconds = info != null && info.has("gameDuration") ? info.get("gameDuration").getAsInt() : 0;

        if (info == null || !info.has("participants") || !info.get("participants").isJsonArray()) {
            return new MatchSummary(matchId, gameMode, gameStartMillis, gameDurationSeconds, "", 0, 0, 0, 0, 0, false);
        }

        JsonArray participants = info.getAsJsonArray("participants");
        JsonObject me = null;
        for (JsonElement el : participants) {
            if (!el.isJsonObject()) continue;
            JsonObject p = el.getAsJsonObject();
            if (p.has("puuid") && puuid.equals(p.get("puuid").getAsString())) {
                me = p;
                break;
            }
        }

        if (me == null) {
            return new MatchSummary(matchId, gameMode, gameStartMillis, gameDurationSeconds, "", 0, 0, 0, 0, 0, false);
        }

        String championName = me.has("championName") ? me.get("championName").getAsString() : "";
        int kills = me.has("kills") ? me.get("kills").getAsInt() : 0;
        int deaths = me.has("deaths") ? me.get("deaths").getAsInt() : 0;
        int assists = me.has("assists") ? me.get("assists").getAsInt() : 0;
        int cs = (me.has("totalMinionsKilled") ? me.get("totalMinionsKilled").getAsInt() : 0)
                + (me.has("neutralMinionsKilled") ? me.get("neutralMinionsKilled").getAsInt() : 0);
        int gold = me.has("goldEarned") ? me.get("goldEarned").getAsInt() : 0;
        boolean win = me.has("win") && me.get("win").getAsBoolean();

        return new MatchSummary(matchId, gameMode, gameStartMillis, gameDurationSeconds, championName, kills, deaths, assists, cs, gold, win);
    }

    private MatchParticipant parseParticipant(JsonObject p) {
        String summonerName = p.has("summonerName") ? p.get("summonerName").getAsString() : "";
        String championName = p.has("championName") ? p.get("championName").getAsString() : "";
        int kills = p.has("kills") ? p.get("kills").getAsInt() : 0;
        int deaths = p.has("deaths") ? p.get("deaths").getAsInt() : 0;
        int assists = p.has("assists") ? p.get("assists").getAsInt() : 0;
        int cs = (p.has("totalMinionsKilled") ? p.get("totalMinionsKilled").getAsInt() : 0)
                + (p.has("neutralMinionsKilled") ? p.get("neutralMinionsKilled").getAsInt() : 0);
        int gold = p.has("goldEarned") ? p.get("goldEarned").getAsInt() : 0;
        boolean win = p.has("win") && p.get("win").getAsBoolean();

        return new MatchParticipant(summonerName, championName, kills, deaths, assists, cs, gold, win);
    }

    private JsonObject fetchJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Riot-Token", API_KEY)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response for " + url + ": " + response.body());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonObject.class);
        } else {
            throw new IOException("API Request failed. Status: " + response.statusCode() + " URL: " + url + " Body: " + response.body());
        }
    }
    
    private JsonArray fetchJsonArray(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Riot-Token", API_KEY)
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonArray.class);
        } else {
            throw new IOException("API Request failed. Status: " + response.statusCode() + " URL: " + url + " Body: " + response.body());
        }
    }
}
