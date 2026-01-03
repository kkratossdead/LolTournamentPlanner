package com.loltournamentplanner.dao;

import com.loltournamentplanner.model.LoLAccount;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoLAccountDao {
    List<LoLAccount> findByUserId(UUID userId) throws SQLException;

    Optional<LoLAccount> findByPuuid(String puuid) throws SQLException;

    void upsertForUser(UUID userId, LoLAccount account) throws SQLException;

    void replaceAccountsForUser(UUID userId, List<LoLAccount> accounts) throws SQLException;
}
