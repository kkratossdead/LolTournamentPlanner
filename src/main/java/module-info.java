module com.loltournamentplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.loltournamentplanner to javafx.fxml;
    opens com.loltournamentplanner.controller to javafx.fxml;
    opens com.loltournamentplanner.model to com.google.gson;
    
    exports com.loltournamentplanner;
    exports com.loltournamentplanner.model;
    exports com.loltournamentplanner.service;
}
