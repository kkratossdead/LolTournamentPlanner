package com.loltournamentplanner.service;

import com.loltournamentplanner.dao.JdbcLoLAccountDao;
import com.loltournamentplanner.dao.JdbcUserDao;
import com.loltournamentplanner.dao.LoLAccountDao;
import com.loltournamentplanner.dao.UserDao;
import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthService {
    private final UserDao userDao;
    private final LoLAccountDao accountDao;
    
    // Validation patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    public AuthService() {
        this.userDao = new JdbcUserDao();
        this.accountDao = new JdbcLoLAccountDao();
    }

    /**
     * Validates username format.
     * @return list of validation errors (empty if valid)
     */
    public List<String> validateUsername(String username) {
        List<String> errors = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            errors.add("Username is required");
        } else if (username.length() < 3) {
            errors.add("Username must be at least 3 characters");
        } else if (username.length() > 20) {
            errors.add("Username must be at most 20 characters");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.add("Username can only contain letters, numbers and underscores");
        }
        return errors;
    }

    /**
     * Validates email format.
     * @return list of validation errors (empty if valid)
     */
    public List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        if (email == null || email.isEmpty()) {
            errors.add("Email is required");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format");
        }
        return errors;
    }

    /**
     * Validates password and returns errors.
     * @return list of validation errors (empty if valid)
     */
    public List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
        } else {
            if (password.length() < MIN_PASSWORD_LENGTH) {
                errors.add("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            }
            if (!password.matches(".*[A-Z].*")) {
                errors.add("Password must contain at least one uppercase letter");
            }
            if (!password.matches(".*[0-9].*")) {
                errors.add("Password must contain at least one digit");
            }
        }
        return errors;
    }

    /**
     * Calculates password strength score (0-4).
     * 0 = very weak, 1 = weak, 2 = fair, 3 = strong, 4 = very strong
     */
    public int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        int score = 0;
        
        // Length checks
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        
        // Cap at 4
        return Math.min(score, 4);
    }

    /**
     * Returns a label for the password strength.
     */
    public String getPasswordStrengthLabel(int strength) {
        return switch (strength) {
            case 0 -> "Very Weak";
            case 1 -> "Weak";
            case 2 -> "Fair";
            case 3 -> "Strong";
            case 4 -> "Very Strong";
            default -> "";
        };
    }

    /**
     * Checks if username is already taken.
     */
    public boolean isUsernameTaken(String username) throws IOException {
        try {
            return userDao.usernameExists(username);
        } catch (SQLException e) {
            throw new IOException("Database error: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if email is already registered.
     */
    public boolean isEmailTaken(String email) throws IOException {
        try {
            return userDao.emailExists(email);
        } catch (SQLException e) {
            throw new IOException("Database error: " + e.getMessage(), e);
        }
    }

    public User register(String username, String email, String password) throws IOException {
        try {
            if (userDao.usernameExists(username)) {
                throw new IOException("Username already exists");
            }
            if (userDao.emailExists(email)) {
                throw new IOException("Email already registered");
            }

            User newUser = new User(username, email, password);
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
