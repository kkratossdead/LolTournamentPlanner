package com.loltournamentplanner.service;

import com.loltournamentplanner.dao.JdbcTournamentDao;
import com.loltournamentplanner.dao.TournamentDao;
import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.Tournament;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentService {
    private final TournamentDao tournamentDao;

    public TournamentService() {
        this.tournamentDao = new JdbcTournamentDao();
    }

    public List<Tournament> getAllTournaments() {
        try {
            return tournamentDao.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void joinTournament(Tournament tournament, LoLAccount account) throws IOException {
        try {
            Optional<String> status = tournamentDao.findStatusById(tournament.getId());
            String statusValue = status.orElse(tournament.getStatus());
            if (statusValue != null && "CLOSED".equalsIgnoreCase(statusValue)) {
                throw new IOException("Tournament is closed");
            }
            tournamentDao.addParticipantIfAbsent(tournament.getId(), account.getPuuid());
        } catch (SQLException e) {
            throw new IOException("Database error (joinTournament): " + e.getMessage(), e);
        }
    }
}
