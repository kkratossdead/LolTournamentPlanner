module com.loltournamentplanner {
    requires javafx.base;
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.sql;
    requires com.google.gson;
    requires mysql.connector.j;
    requires jbcrypt;

    opens com.loltournamentplanner to javafx.fxml;
    opens com.loltournamentplanner.controller to javafx.fxml;
    opens com.loltournamentplanner.model to com.google.gson;
    
    exports com.loltournamentplanner;
    exports com.loltournamentplanner.model;
    exports com.loltournamentplanner.service;
}
