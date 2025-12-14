package com.loltournamentplanner.dao;

import com.loltournamentplanner.db.Db;
import com.loltournamentplanner.model.LoLAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcLoLAccountDao implements LoLAccountDao {

    @Override
    public List<LoLAccount> findByUserId(UUID userId) throws SQLException {
        String sql = "SELECT puuid, game_name, tag_line, summoner_level, tier, `rank`, league_points, last_updated FROM lol_accounts WHERE user_id = ?";
        List<LoLAccount> accounts = new ArrayList<>();
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs));
                }
            }
        }
        return accounts;
    }

    @Override
    public Optional<LoLAccount> findByPuuid(String puuid) throws SQLException {
        String sql = "SELECT puuid, game_name, tag_line, summoner_level, tier, `rank`, league_points, last_updated FROM lol_accounts WHERE puuid = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, puuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        }
    }

    @Override
    public void upsertForUser(UUID userId, LoLAccount account) throws SQLException {
        String sql =
                "INSERT INTO lol_accounts (puuid, user_id, game_name, tag_line, summoner_level, tier, `rank`, league_points, last_updated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "user_id = VALUES(user_id), " +
                "game_name = VALUES(game_name), " +
                "tag_line = VALUES(tag_line), " +
                "summoner_level = VALUES(summoner_level), " +
                "tier = VALUES(tier), " +
                "`rank` = VALUES(`rank`), " +
                "league_points = VALUES(league_points), " +
                "last_updated = VALUES(last_updated)";

        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, account.getPuuid());
            ps.setString(2, userId.toString());
            ps.setString(3, nullToEmpty(account.getGameName()));
            ps.setString(4, nullToEmpty(account.getTagLine()));
            ps.setInt(5, account.getSummonerLevel());
            ps.setString(6, nullToEmpty(account.getTier()));
            ps.setString(7, nullToEmpty(account.getRank()));
            ps.setInt(8, account.getLeaguePoints());
            ps.setString(9, nullToEmpty(account.getLastUpdated()));
            ps.executeUpdate();
        }
    }

    @Override
    public void replaceAccountsForUser(UUID userId, List<LoLAccount> accounts) throws SQLException {
        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deletePs = connection.prepareStatement("DELETE FROM lol_accounts WHERE user_id = ?")) {
                    deletePs.setString(1, userId.toString());
                    deletePs.executeUpdate();
                }

                String insertSql = "INSERT INTO lol_accounts (puuid, user_id, game_name, tag_line, summoner_level, tier, `rank`, league_points, last_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertPs = connection.prepareStatement(insertSql)) {
                    for (LoLAccount account : accounts) {
                        insertPs.setString(1, account.getPuuid());
                        insertPs.setString(2, userId.toString());
                        insertPs.setString(3, nullToEmpty(account.getGameName()));
                        insertPs.setString(4, nullToEmpty(account.getTagLine()));
                        insertPs.setInt(5, account.getSummonerLevel());
                        insertPs.setString(6, nullToEmpty(account.getTier()));
                        insertPs.setString(7, nullToEmpty(account.getRank()));
                        insertPs.setInt(8, account.getLeaguePoints());
                        insertPs.setString(9, nullToEmpty(account.getLastUpdated()));
                        insertPs.addBatch();
                    }
                    insertPs.executeBatch();
                }

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static LoLAccount mapRow(ResultSet rs) throws SQLException {
        return new LoLAccount(
                rs.getString("puuid"),
                rs.getString("game_name"),
                rs.getString("tag_line"),
                rs.getInt("summoner_level"),
                rs.getString("tier"),
                rs.getString("rank"),
                rs.getInt("league_points"),
                rs.getString("last_updated")
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
