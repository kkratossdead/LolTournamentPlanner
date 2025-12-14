package com.loltournamentplanner.dao;

import com.loltournamentplanner.model.Tournament;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TournamentDao {
    List<Tournament> findAll() throws SQLException;

    Optional<Tournament> findById(String tournamentId) throws SQLException;

    void create(Tournament tournament) throws SQLException;

    boolean updateStatus(String tournamentId, String status) throws SQLException;

    Optional<String> findStatusById(String tournamentId) throws SQLException;

    boolean addParticipantIfAbsent(String tournamentId, String puuid) throws SQLException;

    boolean removeParticipant(String tournamentId, String puuid) throws SQLException;

    int countParticipants(String tournamentId) throws SQLException;
}
