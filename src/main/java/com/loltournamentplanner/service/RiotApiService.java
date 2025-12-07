package com.loltournamentplanner.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.loltournamentplanner.model.LoLAccount;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class RiotApiService {
    private static final String API_KEY = "RGAPI-e7f565bd-0da1-4ca1-ace7-615ac9875042";
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

        String[] platforms = {"euw1", "eun1"};
        JsonObject summonerJson = null;
        String activePlatformUrl = null;

        for (String platform : platforms) {
            String platformUrl = "https://" + platform + ".api.riotgames.com";
            String summonerUrl = String.format("%s/lol/summoner/v4/summoners/by-puuid/%s", 
                    platformUrl, puuid);
            
            try {
                JsonObject response = fetchJson(summonerUrl);
                // If we get a 200 OK, the user exists on this platform
                summonerJson = response;
                activePlatformUrl = platformUrl;
                break; 
            } catch (IOException e) {
                // Ignore 404 or other errors and try next platform
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

        // Step 3: Get League Data (Rank/Tier) using PUUID
        // "/lol/league/v4/entries/by-puuid/{encryptedPUUID}"
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

        return new LoLAccount(puuid, returnedGameName, returnedTagLine, summonerLevel, tier, rank, leaguePoints);
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
