package com.loltournamentplanner.controller;

import com.loltournamentplanner.model.MatchDetails;
import com.loltournamentplanner.model.MatchParticipant;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class MatchDetailsController {

    @FXML
    private Label matchTitleLabel;

    @FXML
    private Label matchMetaLabel;

    @FXML
    private ListView<MatchParticipant> participantsListView;

    @FXML
    private Button closeButton;

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void setMatchDetails(MatchDetails details) {
        matchTitleLabel.setText(details.getMatchId());
        matchMetaLabel.setText(formatMeta(details.getGameMode(), details.getGameDurationSeconds()));

        participantsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MatchParticipant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String wl = item.isWin() ? "W" : "L";
                    setText(String.format(
                            "%s  |  %s  |  %d/%d/%d  |  CS %d  |  Gold %d  |  %s",
                            safe(item.getSummonerName()),
                            safe(item.getChampionName()),
                            item.getKills(),
                            item.getDeaths(),
                            item.getAssists(),
                            item.getCs(),
                            item.getGold(),
                            wl
                    ));
                }
            }
        });

        participantsListView.setItems(FXCollections.observableArrayList(details.getParticipants()));
    }

    private static String formatMeta(String mode, int durationSeconds) {
        int mins = Math.max(0, durationSeconds) / 60;
        int secs = Math.max(0, durationSeconds) % 60;
        String d = String.format("%d:%02d", mins, secs);
        String m = (mode == null || mode.isBlank()) ? "" : mode;
        return (m.isEmpty() ? "" : (m + "  |  ")) + d;
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
