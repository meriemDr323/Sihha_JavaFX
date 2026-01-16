module com.example.hopitalapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.hopitalapp to javafx.fxml;
    opens com.example.hopitalapp.controller.admin to javafx.fxml;
    opens com.example.hopitalapp.controller.medecin to javafx.fxml;
    opens com.example.hopitalapp.controller.secretaire to javafx.fxml;
    opens com.example.hopitalapp.controller.shared to javafx.fxml;
    opens com.example.hopitalapp.model to javafx.base;
    opens com.example.hopitalapp.util to javafx.base;

    exports com.example.hopitalapp;
    exports com.example.hopitalapp.controller.admin;
    exports com.example.hopitalapp.controller.medecin;
    exports com.example.hopitalapp.controller.secretaire;
    exports com.example.hopitalapp.controller.shared;
    exports com.example.hopitalapp.model;
    exports com.example.hopitalapp.util;
}
