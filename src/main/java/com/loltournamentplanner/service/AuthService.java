package com.loltournamentplanner.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuthService {
    private static final String CSV_FILE = "users.csv";
    private static final String DELIMITER = ";";
    private final Gson gson;

    public AuthService() {
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

    public User register(String username, String password) throws IOException {
        if (userExists(username)) {
            throw new IOException("User already exists");
        }

        User newUser = new User(username, password);
        saveUserToCsv(newUser);
        return newUser;
    }

    public User login(String username, String password) throws IOException {
        List<User> users = loadUsersFromCsv();
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private boolean userExists(String username) throws IOException {
        List<User> users = loadUsersFromCsv();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void saveUserToCsv(User user) throws IOException {
        String accountsJson = gson.toJson(user.getLinkedAccounts());
        String line = user.getId() + DELIMITER + user.getUsername() + DELIMITER + user.getPassword() + DELIMITER + accountsJson + System.lineSeparator();
        Files.writeString(Path.of(CSV_FILE), line, StandardOpenOption.APPEND);
    }
    
    public void updateUser(User user) throws IOException {
        List<User> users = loadUsersFromCsv();
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                found = true;
                break;
            }
        }
        
        if (found) {
            writeAllUsersToCsv(users);
        }
    }

    private void writeAllUsersToCsv(List<User> users) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            String accountsJson = gson.toJson(user.getLinkedAccounts());
            sb.append(user.getId()).append(DELIMITER)
              .append(user.getUsername()).append(DELIMITER)
              .append(user.getPassword()).append(DELIMITER)
              .append(accountsJson).append(System.lineSeparator());
        }
        Files.writeString(Path.of(CSV_FILE), sb.toString(), StandardOpenOption.TRUNCATE_EXISTING);
    }

    private List<User> loadUsersFromCsv() throws IOException {
        List<User> users = new ArrayList<>();
        List<String> lines = Files.readAllLines(Path.of(CSV_FILE));
        
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(DELIMITER);
            if (parts.length >= 4) {
                UUID id = UUID.fromString(parts[0]);
                String username = parts[1];
                String password = parts[2];
                String accountsJson = parts[3];
                
                User user = new User(id, username, password);
                Type listType = new TypeToken<List<LoLAccount>>(){}.getType();
                List<LoLAccount> accounts = gson.fromJson(accountsJson, listType);
                if (accounts != null) {
                    user.setLinkedAccounts(accounts);
                }
                users.add(user);
            }
        }
        return users;
    }
}
