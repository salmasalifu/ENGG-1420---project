package com.example.projectfinal;

import controller.DataManager;
import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The Main class is the entry point for the Campus Event Booking System.
 * Handles JavaFX initialization and file persistence on shutdown.
 */
public class Main extends Application {

    // Reference to the controller to access data managers for saving
    private MainController controller;

    @Override
    public void start(Stage stage) throws IOException {
        // Use the absolute path from the root of the resources folder
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/projectfinal/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        // Get reference to the controller after loading the FXML
        // This allows us to access the data managers (users, events, bookings) for saving
        controller = fxmlLoader.getController();

        stage.setTitle("Campus Event Booking System");
        stage.setScene(scene);

        // File Persistance: Save all data when user clicks the window close button (X)
        stage.setOnCloseRequest(event -> {
            saveData();
        });

        stage.show();
    }

    /**
     * File persistance: Saves all application data to CSV files.
     * Called when the application is closing to preserve user changes.
     * Retrieves data managers from controller and passes them to DataManager.
     */
    private void saveData() {
        if (controller != null) {
            DataManager.saveAllData(
                    controller.getUserManager(),    // Get user management system
                    controller.getEventList(),       // Get list of all events
                    controller.getBookingManager()   // Get booking management system
            );
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
