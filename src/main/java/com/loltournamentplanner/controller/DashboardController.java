package com.loltournamentplanner.controller;

import com.loltournamentplanner.model.LoLAccount;
import com.loltournamentplanner.model.Tournament;
import com.loltournamentplanner.model.User;
import com.loltournamentplanner.service.AuthService;
import com.loltournamentplanner.service.RiotApiService;
import com.loltournamentplanner.service.TournamentService;
import com.loltournamentplanner.service.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML
    private TextField gameNameField;

    @FXML
    private TextField tagLineField;

    @FXML
    private ListView<Tournament> tournamentListView;
    
    @FXML
    private Button joinTournamentButton;
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private ListView<LoLAccount> linkedAccountsListView;


    private final RiotApiService riotApiService;
    private final AuthService authService;
    private final TournamentService tournamentService;

    public DashboardController() {
        this.riotApiService = new RiotApiService();
        this.authService = new AuthService();
        this.tournamentService = new TournamentService();
    }

    @FXML
    public void initialize() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + currentUser.getUsername());
            }
            updateLinkedAccountsList();
        }
        
        refreshTournamentList();
        
        tournamentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (joinTournamentButton != null) {
                boolean disabled = (newVal == null) || (newVal.getStatus() != null && "CLOSED".equalsIgnoreCase(newVal.getStatus()));
                joinTournamentButton.setDisable(disabled);
            }
        });

        if (linkedAccountsListView != null) {
            linkedAccountsListView.setCellFactory(param -> new ListCell<LoLAccount>() {
                private final Label label = new Label();
                private final Button deleteButton = new Button("🗑");
                private final HBox pane = new HBox(label, new Region(), deleteButton);

                {
                    pane.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(pane.getChildren().get(1), Priority.ALWAYS); 
                    deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
                    deleteButton.setOnAction(event -> {
                        LoLAccount account = getItem();
                        if (account != null) {
                            deleteAccount(account);
                        }
                    });
                }

                @Override
                protected void updateItem(LoLAccount item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        String rankInfo = "UNRANKED";
                        if (item.getTier() != null && !"UNRANKED".equals(item.getTier())) {
                            rankInfo = item.getTier() + " " + item.getRank();
                        }
                        label.setText(item.getGameName() + " #" + item.getTagLine() + " [" + rankInfo + "]");
                        setGraphic(pane);
                    }
                }
            });

            linkedAccountsListView.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    LoLAccount selectedAccount = linkedAccountsListView.getSelectionModel().getSelectedItem();
                    if (selectedAccount != null) {
                        showAccountDetails(selectedAccount);
                    }
                }
            });




            tournamentListView.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Tournament selectedTournament = tournamentListView.getSelectionModel().getSelectedItem();
                    if (selectedTournament != null) {
                        openTournamentDetailsWindow(selectedTournament);
                    }
                }
            });

        }
    }

    private void deleteAccount(LoLAccount account) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Delete " + account.getGameName() + " #" + account.getTagLine() + "?");
        alert.setContentText("Are you sure you want to remove this account?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentUser.getLinkedAccounts().removeIf(a -> a.getPuuid().equals(account.getPuuid()));
                try {
                    authService.updateUser(currentUser);
                    updateLinkedAccountsList();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete account: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    protected void onLinkAccount() {
        String gameName = gameNameField.getText();
        String tagLine = tagLineField.getText();

        if (gameName.isEmpty() || tagLine.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Game Name and Tag Line cannot be empty.");
            return;
        }

        try {
            LoLAccount account = riotApiService.verifyAndLinkAccount(gameName, tagLine);
            User currentUser = UserSession.getInstance().getCurrentUser();
            
            if (currentUser != null) {
                currentUser.addAccount(account);
                authService.updateUser(currentUser);
                updateLinkedAccountsList();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account linked successfully!");
                gameNameField.clear();
                tagLineField.clear();
            }
        } catch (IOException | InterruptedException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to link account: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    protected void onJoinTournament() {
        Tournament selectedTournament = tournamentListView.getSelectionModel().getSelectedItem();
        User currentUser = UserSession.getInstance().getCurrentUser();
        
        if (selectedTournament == null || currentUser == null) return;

        if (selectedTournament.getStatus() != null && "CLOSED".equalsIgnoreCase(selectedTournament.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Tournament Closed", "This tournament is closed and cannot be joined.");
            return;
        }
        
        List<LoLAccount> accounts = currentUser.getLinkedAccounts();
        if (accounts.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Accounts", "You must link a LoL account before joining a tournament.");
            return;
        }

        ChoiceDialog<LoLAccount> dialog = new ChoiceDialog<>(accounts.get(0), accounts);
        dialog.setTitle("Join Tournament");
        dialog.setHeaderText("Select an account to join " + selectedTournament.getName());
        dialog.setContentText("Choose account:");

        Optional<LoLAccount> result = dialog.showAndWait();
        result.ifPresent(account -> {
            if (selectedTournament.getParticipantPuuids().contains(account.getPuuid())) {
                showAlert(Alert.AlertType.WARNING, "Already Joined", "This account is already participating in this tournament.");
                return;
            }

            try {
                tournamentService.joinTournament(selectedTournament, account);
                refreshTournamentList();
                showAlert(Alert.AlertType.INFORMATION, "Joined!", "You have successfully joined " + selectedTournament.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to join tournament: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private void refreshTournamentList() {
        List<Tournament> tournaments = tournamentService.getAllTournaments();
        tournamentListView.setItems(FXCollections.observableArrayList(tournaments));
    }
    
    private void updateLinkedAccountsList() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && linkedAccountsListView != null) {
            linkedAccountsListView.setItems(FXCollections.observableArrayList(currentUser.getLinkedAccounts()));
        }
    }
    
    private void showAccountDetails(LoLAccount account) {
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open account details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openTournamentDetailsWindow(Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/loltournamentplanner/view/tournament-details-view.fxml"));
            Scene scene = new Scene(loader.load(), 720, 560);

            TournamentDetailsController controller = loader.getController();
            controller.setTournament(tournament);

            Stage stage = new Stage();
            stage.setTitle("Tournament Details");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open tournament details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
