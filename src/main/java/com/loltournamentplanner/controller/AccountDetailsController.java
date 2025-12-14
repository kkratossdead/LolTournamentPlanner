package com.loltournamentplanner.controller;

import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.MatchDetails;
import com.loltournamentplanner.model.MatchSummary;
import com.loltournamentplanner.service.RiotApiService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.concurrent.Task;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AccountDetailsController {

    @FXML
    private Label accountTitleLabel;

    @FXML
    private Label summonerLevelLabel;

    @FXML
    private Label rankLabel;

    @FXML
    private Label lpLabel;

    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private Label puuidLabel;

    @FXML
    private ListView<MatchSummary> matchesListView;

    @FXML
    private Button copyPuuidButton;

    @FXML
    private Button closeButton;

    private LoLAccount account;
    private final RiotApiService riotApiService;

    public AccountDetailsController() {
        this.riotApiService = new RiotApiService();
    }

    public void setAccount(LoLAccount account) {
        this.account = account;

        accountTitleLabel.setText(account.getGameName() + " #" + account.getTagLine());
        summonerLevelLabel.setText(String.valueOf(account.getSummonerLevel()));

        String tier = account.getTier();
        String rank = account.getRank();
        boolean unranked = tier == null || tier.isBlank() || "UNRANKED".equalsIgnoreCase(tier);
        rankLabel.setText(unranked ? "UNRANKED" : (tier + (rank == null || rank.isBlank() ? "" : (" " + rank))));

        lpLabel.setText(unranked ? "—" : String.valueOf(account.getLeaguePoints()));
        lastUpdatedLabel.setText(nullToDash(account.getLastUpdated()));
        puuidLabel.setText(nullToDash(account.getPuuid()));

        copyPuuidButton.setDisable(account.getPuuid() == null || account.getPuuid().isBlank());

        setupMatchesUI();
        loadLastMatchesAsync();
    }

    private void setupMatchesUI() {
        if (matchesListView == null) return;

        matchesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MatchSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String wl = item.isWin() ? "W" : "L";
                    String when = formatWhen(item.getGameStartMillis());
                    String duration = formatDuration(item.getGameDurationSeconds());
                    setText(String.format(
                            "%s  |  %s  |  %s  |  %s  |  %d/%d/%d  |  CS %d",
                            wl,
                            safe(item.getChampionName()),
                            safe(item.getGameMode()),
                            when + " (" + duration + ")",
                            item.getKills(),
                            item.getDeaths(),
                            item.getAssists(),
                            item.getCs()
                    ));
                }
            }
        });

        matchesListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                MatchSummary selected = matchesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openMatchDetailsAsync(selected.getMatchId());
                }
            }
        });
    }

    private void loadLastMatchesAsync() {
        if (matchesListView == null) return;
        if (account == null || account.getPuuid() == null || account.getPuuid().isBlank()) {
            matchesListView.setItems(FXCollections.observableArrayList());
            return;
        }

        matchesListView.setItems(FXCollections.observableArrayList());
        matchesListView.setPlaceholder(new Label("Loading matches..."));

        Task<List<MatchSummary>> task = new Task<>() {
            @Override
            protected List<MatchSummary> call() throws Exception {
                return riotApiService.getLastMatchSummariesForPuuid(account.getPuuid(), 3);
            }
        };

        task.setOnSucceeded(e -> matchesListView.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            matchesListView.setPlaceholder(new Label("Failed to load matches"));
            showError("Failed to load matches: " + (ex == null ? "Unknown error" : ex.getMessage()));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void openMatchDetailsAsync(String matchId) {
        Task<MatchDetails> task = new Task<>() {
            @Override
            protected MatchDetails call() throws Exception {
                return riotApiService.getMatchDetails(matchId);
            }
        };

        task.setOnSucceeded(e -> openMatchDetailsWindow(task.getValue()));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Failed to load match details: " + (ex == null ? "Unknown error" : ex.getMessage()));
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void openMatchDetailsWindow(MatchDetails details) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/loltournamentplanner/view/match-details-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);

            MatchDetailsController controller = loader.getController();
            controller.setMatchDetails(details);

            Stage stage = new Stage();
            stage.setTitle("Match Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Failed to open match details window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void onCopyPuuid() {
        if (account == null || account.getPuuid() == null) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(account.getPuuid());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private static String nullToDash(String value) {
        if (value == null) return "—";
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "—" : trimmed;
    }

    private static String safe(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }

    private static String formatDuration(int seconds) {
        int s = Math.max(0, seconds);
        int mins = s / 60;
        int secs = s % 60;
        return String.format("%d:%02d", mins, secs);
    }

    private static String formatWhen(long epochMillis) {
        if (epochMillis <= 0) return "—";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
        return fmt.format(Instant.ofEpochMilli(epochMillis));
    }
}
