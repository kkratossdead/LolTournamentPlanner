package com.loltournamentplanner.dao;

import com.loltournamentplanner.model.User;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    boolean usernameExists(String username) throws SQLException;

    boolean emailExists(String email) throws SQLException;

    void register(User user) throws SQLException;

    Optional<User> login(String username, String password) throws SQLException;

    Optional<User> findById(UUID id) throws SQLException;

    void update(User user) throws SQLException;
}
