module com.example.projectfinal {
    // Required JavaFX Modules
    requires javafx.controls;
    requires javafx.fxml;

    // Optional: Include this if you are using the BootstrapFX library
    requires org.kordamp.bootstrapfx.core;

    // 1. Allow JavaFX to load your FXML files and Main class
    opens com.example.projectfinal to javafx.fxml;
    exports com.example.projectfinal;

    // 2. Allow JavaFX to access your Controllers for button actions and logic
    opens controller to javafx.fxml;
    exports controller;

    // 3. PERMISSION FOR EVENT TABLE: Allows access to Event, Workshop, Concert, etc.
    opens model to javafx.base, javafx.fxml;
    exports model;

    // 4. PERMISSION FOR USER TABLE: Allows access to User, Student, Staff, etc.
    opens user to javafx.base, javafx.fxml;
    exports user;
}