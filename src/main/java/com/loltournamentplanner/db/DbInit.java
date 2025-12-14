package com.loltournamentplanner.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class DbInit {
    private DbInit() {}

    public static void ensureSchema() throws SQLException {
        try (Connection connection = Db.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "  id CHAR(36) NOT NULL," +
                    "  username VARCHAR(64) NOT NULL," +
                    "  password VARCHAR(255) NOT NULL," +
                    "  PRIMARY KEY (id)," +
                    "  UNIQUE KEY uq_users_username (username)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS lol_accounts (" +
                    "  puuid VARCHAR(128) NOT NULL," +
                    "  user_id CHAR(36) NOT NULL," +
                    "  game_name VARCHAR(64) NOT NULL," +
                    "  tag_line VARCHAR(32) NOT NULL," +
                    "  summoner_level INT NOT NULL," +
                    "  tier VARCHAR(32) NOT NULL," +
                    "  `rank` VARCHAR(16) NOT NULL," +
                    "  league_points INT NOT NULL," +
                    "  last_updated VARCHAR(64) NOT NULL," +
                    "  PRIMARY KEY (puuid)," +
                    "  KEY idx_lol_accounts_user_id (user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tournaments (" +
                    "  id VARCHAR(64) NOT NULL," +
                    "  name VARCHAR(128) NOT NULL," +
                    "  status VARCHAR(32) NOT NULL," +
                    "  description TEXT NOT NULL," +
                    "  start_date VARCHAR(64) NOT NULL," +
                    "  max_participants INT NOT NULL," +
                    "  PRIMARY KEY (id)," +
                    "  KEY idx_tournaments_status (status)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS tournament_participants (" +
                    "  tournament_id VARCHAR(64) NOT NULL," +
                    "  puuid VARCHAR(128) NOT NULL," +
                    "  PRIMARY KEY (tournament_id, puuid)," +
                    "  KEY idx_tp_puuid (puuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            statement.executeUpdate("ALTER TABLE users ENGINE=InnoDB");
            statement.executeUpdate("ALTER TABLE lol_accounts ENGINE=InnoDB");
            statement.executeUpdate("ALTER TABLE tournaments ENGINE=InnoDB");
            statement.executeUpdate("ALTER TABLE tournament_participants ENGINE=InnoDB");

            ensureForeignKeyExists(connection,
                    "lol_accounts",
                    "fk_lol_accounts_user",
                    "ALTER TABLE lol_accounts ADD CONSTRAINT fk_lol_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            );

            ensureForeignKeyExists(connection,
                    "tournament_participants",
                    "fk_tp_tournament",
                    "ALTER TABLE tournament_participants ADD CONSTRAINT fk_tp_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE"
            );

            ensureForeignKeyExists(connection,
                    "tournament_participants",
                    "fk_tp_puuid",
                    "ALTER TABLE tournament_participants ADD CONSTRAINT fk_tp_puuid FOREIGN KEY (puuid) REFERENCES lol_accounts(puuid) ON DELETE CASCADE"
            );

            ensureIndexExists(connection,
                    "lol_accounts",
                    "idx_lol_accounts_user_id",
                    "CREATE INDEX idx_lol_accounts_user_id ON lol_accounts(user_id)"
            );

            ensureIndexExists(connection,
                    "tournaments",
                    "idx_tournaments_status",
                    "CREATE INDEX idx_tournaments_status ON tournaments(status)"
            );

            ensureIndexExists(connection,
                    "tournament_participants",
                    "idx_tp_puuid",
                    "CREATE INDEX idx_tp_puuid ON tournament_participants(puuid)"
            );
        }
    }

    private static void ensureForeignKeyExists(Connection connection, String tableName, String constraintName, String alterSql) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = ? AND CONSTRAINT_NAME = ? AND CONSTRAINT_TYPE = 'FOREIGN KEY' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, constraintName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(alterSql);
        }
    }

    private static void ensureIndexExists(Connection connection, String tableName, String indexName, String createIndexSql) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createIndexSql);
        }
    }
}
