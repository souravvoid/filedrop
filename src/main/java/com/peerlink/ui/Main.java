package com.peerlink.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL resource = getClass().getResource("/com/peerlink/ui/MainView.fxml");
        if (resource == null) {
            throw new IllegalStateException("Cannot find MainView.fxml");
        }
        FXMLLoader loader = new FXMLLoader(resource);
        Scene scene = new Scene(loader.load(), 700, 500);

        // Load the new CSS
        scene.getStylesheets().add(
            getClass().getResource("/com/peerlink/ui/peerlink.css").toExternalForm());

        stage.setTitle("PeerLink");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(600);
        stage.setMinHeight(400);

        // Remove default JavaFX focus ring on startup
        scene.getRoot().requestFocus();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}