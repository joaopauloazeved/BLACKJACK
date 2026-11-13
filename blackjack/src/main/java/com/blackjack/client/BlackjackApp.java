package com.blackjack.client;

import com.blackjack.client.controllers.ClientController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BlackjackApp extends Application {
    private ClientController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(BlackjackApp.class.getResource("/com/blackjack/primary.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 820);
        scene.getStylesheets().add(BlackjackApp.class.getResource("/com/blackjack/blackjack.css").toExternalForm());

        controller = loader.getController();
        stage.setTitle("Blackjack 21 | Arcade Table");
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
