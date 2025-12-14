package com.loltournamentplanner.db;

public final class DbConfig {
    private DbConfig() {}

    public static String url() {
        return getenvOrDefault("DB_URL", "jdbc:mysql://localhost:3306/loltournamentplanner?useSSL=false&serverTimezone=UTC");
    }

    public static String user() {
        return getenvOrDefault("DB_USER", "root");
    }

    public static String password() {
        return getenvOrDefault("DB_PASSWORD", "");
    }

    private static String getenvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) return defaultValue;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }
}
