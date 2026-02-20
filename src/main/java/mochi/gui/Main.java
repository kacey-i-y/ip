package mochi.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mochi.Mochi;

/**
 * Launches Mochi's JavaFX GUI application.
 *
 * <p>This class is the JavaFX entry point. It loads {@code MainWindow.fxml}, creates the
 * main scene, configures the primary stage, and injects a {@link Mochi} instance into
 * the {@link MainWindow} controller so the GUI can delegate command handling to the
 * core application logic.
 */
public class Main extends Application {

    private final Mochi mochi = new Mochi();

    /**
     * Starts the JavaFX application and displays the main window.
     *
     * <p>Loads the UI layout from {@code /view/MainWindow.fxml}, applies window styling,
     * sets up the {@link Stage}, and wires the GUI controller to the core {@link Mochi}
     * instance.
     *
     * @param stage The primary stage provided by the JavaFX runtime.
     */
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader =
                    new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();

            ap.setStyle("-fx-background-color: #ffe6ee;");

            Scene scene = new Scene(ap);
            stage.setScene(scene);
            stage.setTitle("Mochi");
            stage.setResizable(true);

            MainWindow controller = fxmlLoader.getController();
            controller.setMochi(mochi);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
