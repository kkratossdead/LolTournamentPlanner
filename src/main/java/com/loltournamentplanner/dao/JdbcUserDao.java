package com.loltournamentplanner.dao;

import com.loltournamentplanner.db.Db;
import com.loltournamentplanner.model.User;
import org.mindrot.jbcrypt.BCrypt;

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
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public void register(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, email, password) VALUES (?, ?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, hashedPassword);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<User> login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, email, password FROM users WHERE username = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                
                String storedHash = rs.getString("password");
                // Support both legacy plain passwords and new BCrypt hashes
                boolean passwordValid;
                if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
                    passwordValid = BCrypt.checkpw(password, storedHash);
                } else {
                    // Legacy plain text password - validate and upgrade
                    passwordValid = storedHash.equals(password);
                    if (passwordValid) {
                        // Upgrade to BCrypt hash
                        upgradePassword(rs.getString("id"), password);
                    }
                }
                
                if (!passwordValid) return Optional.empty();
                
                UUID id = UUID.fromString(rs.getString("id"));
                User user = new User(id, rs.getString("username"), rs.getString("email"), storedHash);
                return Optional.of(user);
            }
        }
    }
    
    private void upgradePassword(String id, String plainPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Silent fail - not critical
            e.printStackTrace();
        }
    }

    @Override
    public Optional<User> findById(UUID id) throws SQLException {
        String sql = "SELECT id, username, email, password FROM users WHERE id = ? LIMIT 1";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                User user = new User(UUID.fromString(rs.getString("id")), rs.getString("username"), rs.getString("email"), rs.getString("password"));
                return Optional.of(user);
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?";
        try (Connection connection = Db.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getId().toString());
            ps.executeUpdate();
        }
    }
}
