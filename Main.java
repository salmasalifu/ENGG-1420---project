package com.example.projectfinal;

import controller.DataManager;
import controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private MainController controller;


    @Override
    public void start(Stage stage) throws IOException {
        //Use the absolute path from the root of the resources folder
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/com/example/projectfinal/MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);

        // Get reference to the controller after loading the FXML
        controller = fxmlLoader.getController();

        stage.setTitle("Campus Event Booking System");
        stage.setScene(scene);

        // Save data when window close button (X) is clicked
        stage.setOnCloseRequest(event -> {
            saveData();
        });

        stage.show();

    }

    private void saveData() {
        if (controller != null) {
            DataManager.saveAllData(
                    controller.getUserManager(),
                    controller.getEventList(),
                    controller.getBookingManager()
            );
        }
    }

    public static void main(String[] args) {
        launch();
    }
}