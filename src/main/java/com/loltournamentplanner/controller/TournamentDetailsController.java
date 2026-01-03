package com.loltournamentplanner.controller;

import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.Tournament;
import com.loltournamentplanner.service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TournamentDetailsController {

    @FXML
    private Label tournamentNameLabel;

    @FXML
    private Label tournamentStatusLabel;

    @FXML
    private Label tournamentStartDateLabel;

    @FXML
    private Label tournamentMaxParticipantsLabel;

    @FXML
    private TextArea tournamentDescriptionArea;

    @FXML
    private ListView<String> participantsListView;

    @FXML
    private Button closeButton;

    private final AuthService authService;
    private final Map<String, LoLAccount> accountByPuuid;

    public TournamentDetailsController() {
        this.authService = new AuthService();
        this.accountByPuuid = new HashMap<>();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void setTournament(Tournament tournament) {
        accountByPuuid.clear();

        tournamentNameLabel.setText(tournament.getName());
        tournamentStatusLabel.setText(nullToDash(tournament.getStatus()));
        tournamentStartDateLabel.setText(nullToDash(tournament.getStartDate()));

        int max = tournament.getMaxParticipants();
        tournamentMaxParticipantsLabel.setText(max > 0 ? String.valueOf(max) : "—");

        tournamentDescriptionArea.setText(nullToDash(tournament.getDescription()));

        participantsListView.setItems(FXCollections.observableArrayList(tournament.getParticipantPuuids()));

        participantsListView.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String puuid, boolean empty) {
                super.updateItem(puuid, empty);
                if (empty || puuid == null) {
                    setText(null);
                    return;
                }

                LoLAccount account = accountByPuuid.get(puuid);
                if (account == null) {
                    account = authService.findAccountByPuuid(puuid);
                    if (account != null) {
                        accountByPuuid.put(puuid, account);
                    }
                }

                setText(account != null ? (account.getGameName() + " #" + account.getTagLine()) : puuid);
            }
        });

        participantsListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                String selectedPuuid = participantsListView.getSelectionModel().getSelectedItem();
                if (selectedPuuid == null) return;

                LoLAccount account = accountByPuuid.get(selectedPuuid);
                if (account == null) {
                    account = authService.findAccountByPuuid(selectedPuuid);
                    if (account != null) {
                        accountByPuuid.put(selectedPuuid, account);
                    }
                }

                if (account == null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Account Details");
                    alert.setHeaderText("Unknown account");
                    alert.setContentText("No linked account found for this participant (PUUID).");
                    alert.showAndWait();
                    return;
                }

                openAccountDetailsWindow(account);
            }
        });
    }

    private void openAccountDetailsWindow(LoLAccount account) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/loltournamentplanner/view/account-details-view.fxml"));
            Scene scene = new Scene(loader.load(), 720, 420);

            AccountDetailsController controller = loader.getController();
            controller.setAccount(account);

            Stage stage = new Stage();
            stage.setTitle("Account Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open account details: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private static String nullToDash(String value) {
        if (value == null) return "—";
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "—" : trimmed;
    }
}
