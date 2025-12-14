package com.loltournamentplanner.dao;

import com.loltournamentplanner.db.Db;
import com.loltournamentplanner.model.Tournament;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcTournamentDao implements TournamentDao {

    @Override
    public List<Tournament> findAll() throws SQLException {
        Map<String, Tournament> byId = new HashMap<>();

        String tournamentsSql = "SELECT id, name, status, description, start_date, max_participants FROM tournaments";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(tournamentsSql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("id");
                byId.put(id, new Tournament(
                        id,
                        rs.getString("name"),
                        rs.getString("status"),
                        new ArrayList<>(),
                        rs.getString("description"),
                        rs.getString("start_date"),
                        rs.getInt("max_participants")
                ));
            }
        }

        String participantsSql = "SELECT tournament_id, puuid FROM tournament_participants";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(participantsSql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String tournamentId = rs.getString("tournament_id");
                String puuid = rs.getString("puuid");
                Tournament tournament = byId.get(tournamentId);
                if (tournament != null) {
                    tournament.getParticipantPuuids().add(puuid);
                }
            }
        }

        return new ArrayList<>(byId.values());
    }

    @Override
    public Optional<Tournament> findById(String tournamentId) throws SQLException {
        String tournamentSql = "SELECT id, name, status, description, start_date, max_participants FROM tournaments WHERE id = ? LIMIT 1";
        Tournament tournament;
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(tournamentSql)) {
            ps.setString(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                tournament = new Tournament(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("status"),
                        new ArrayList<>(),
                        rs.getString("description"),
                        rs.getString("start_date"),
                        rs.getInt("max_participants")
                );
            }
        }

        String participantsSql = "SELECT puuid FROM tournament_participants WHERE tournament_id = ?";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(participantsSql)) {
            ps.setString(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tournament.getParticipantPuuids().add(rs.getString("puuid"));
                }
            }
        }

        return Optional.of(tournament);
    }

    @Override
    public void create(Tournament tournament) throws SQLException {
        String sql = "INSERT INTO tournaments (id, name, status, description, start_date, max_participants) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tournament.getId());
            ps.setString(2, tournament.getName());
            ps.setString(3, tournament.getStatus());
            ps.setString(4, nullToEmpty(tournament.getDescription()));
            ps.setString(5, nullToEmpty(tournament.getStartDate()));
            ps.setInt(6, tournament.getMaxParticipants());
            ps.executeUpdate();
        }
    }

    @Override
    public boolean updateStatus(String tournamentId, String status) throws SQLException {
        String sql = "UPDATE tournaments SET status = ? WHERE id = ?";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, tournamentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<String> findStatusById(String tournamentId) throws SQLException {
        String sql = "SELECT status FROM tournaments WHERE id = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.ofNullable(rs.getString("status"));
            }
        }
    }

    @Override
    public boolean addParticipantIfAbsent(String tournamentId, String puuid) throws SQLException {
        String sql = "INSERT IGNORE INTO tournament_participants (tournament_id, puuid) VALUES (?, ?)";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, puuid);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    @Override
    public boolean removeParticipant(String tournamentId, String puuid) throws SQLException {
        String sql = "DELETE FROM tournament_participants WHERE tournament_id = ? AND puuid = ?";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            ps.setString(2, puuid);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countParticipants(String tournamentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tournament_participants WHERE tournament_id = ?";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
