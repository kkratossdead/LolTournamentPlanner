package com.loltournamentplanner.dao;

import com.loltournamentplanner.db.Db;
import com.loltournamentplanner.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserDao implements UserDao {

    @Override
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void register(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<User> login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password FROM users WHERE username = ? AND password = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                UUID id = UUID.fromString(rs.getString("id"));
                User user = new User(id, rs.getString("username"), rs.getString("password"));
                return Optional.of(user);
            }
        }
    }

    @Override
    public Optional<User> findById(UUID id) throws SQLException {
        String sql = "SELECT id, username, password FROM users WHERE id = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                User user = new User(UUID.fromString(rs.getString("id")), rs.getString("username"), rs.getString("password"));
                return Optional.of(user);
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getId().toString());
            ps.executeUpdate();
        }
    }
}
