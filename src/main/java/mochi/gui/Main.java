package mochi.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import mochi.Mochi;

/**
 * A GUI for Mochi using FXML.
 */
public class Main extends Application {

    private final Mochi mochi = new Mochi();

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader =
                    new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane ap = fxmlLoader.load();

            Scene scene = new Scene(ap);
            stage.setScene(scene);
            stage.setTitle("Mochi");
            stage.setResizable(false);

            MainWindow controller = fxmlLoader.getController();
            controller.setMochi(mochi);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
