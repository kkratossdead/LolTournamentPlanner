package com.loltournamentplanner.service;

import com.loltournamentplanner.dao.JdbcLoLAccountDao;
import com.loltournamentplanner.dao.JdbcUserDao;
import com.loltournamentplanner.dao.LoLAccountDao;
import com.loltournamentplanner.dao.UserDao;
import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final LoLAccountDao accountDao;

    public AuthService() {
        this.userDao = new JdbcUserDao();
        this.accountDao = new JdbcLoLAccountDao();
    }

    public User register(String username, String password) throws IOException {
        try {
            if (userDao.usernameExists(username)) {
                throw new IOException("User already exists");
            }

            User newUser = new User(username, password);
            userDao.register(newUser);
            accountDao.replaceAccountsForUser(newUser.getId(), newUser.getLinkedAccounts());
            return newUser;
        } catch (SQLException e) {
            throw new IOException("Database error (register): " + e.getMessage(), e);
        }
    }

    public User login(String username, String password) throws IOException {
        try {
            Optional<User> maybeUser = userDao.login(username, password);
            if (maybeUser.isEmpty()) return null;

            User user = maybeUser.get();
            user.setLinkedAccounts(accountDao.findByUserId(user.getId()));
            return user;
        } catch (SQLException e) {
            throw new IOException("Database error (login): " + e.getMessage(), e);
        }
    }

    public void updateUser(User user) throws IOException {
        try {
            userDao.update(user);
            accountDao.replaceAccountsForUser(user.getId(), user.getLinkedAccounts());
        } catch (SQLException e) {
            throw new IOException("Database error (updateUser): " + e.getMessage(), e);
        }
    }

    public LoLAccount findAccountByPuuid(String puuid) {
        try {
            return accountDao.findByPuuid(puuid).orElse(null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
