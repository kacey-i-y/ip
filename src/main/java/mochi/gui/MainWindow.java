package mochi.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import mochi.Mochi;

/**
 * Controller for the main GUI.
 */
public class MainWindow {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private Mochi mochi;

    private final Image userImage =
            new Image(this.getClass().getResourceAsStream("/images/DaUser.jpg"));
    private final Image mochiImage =
            new Image(this.getClass().getResourceAsStream("/images/DaMochi.jpg"));

    /**
     * Initialises the main window.
     */
    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());

        userInput.setStyle(
                "-fx-background-color: #ffd1dc;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-radius: 10;"
                        + "-fx-border-color: #f2a6b3;"
                        + "-fx-border-width: 1;"
                        + "-fx-padding: 8;"
                        + "-fx-font-family: 'Comic Sans MS';"
                        + "-fx-font-size: 16px;"
        );

        sendButton.setStyle(
                "-fx-background-color: #ffb6c1;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-radius: 10;"
                        + "-fx-border-color: #f2a6b3;"
                        + "-fx-border-width: 1;"
        );

        String pink = "#ffe6ee";
        scrollPane.setStyle("-fx-background: " + pink + "; -fx-background-color: " + pink + ";");
        dialogContainer.setStyle("-fx-background-color: " + pink + ";");

        javafx.application.Platform.runLater(() -> {
            var viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: " + pink + ";");
            }
        });
    }

    /**
     * Injects the Mochi instance.
     *
     * @param d Mochi logic instance.
     */
    public void setMochi(Mochi d) {
        this.mochi = d;

        dialogContainer.getChildren().add(
                DialogBox.getMochiDialog(this.mochi.getStartupMessage(), mochiImage)
        );
    }

    /**
     * Creates two dialog boxes: one echoing user input and the other containing Mochi's reply.
     * Then appends them to the dialog container and clears the user input.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = mochi.getResponse(input);

        boolean isError = response != null && response.startsWith("Error:");

        dialogContainer.getChildren().add(
                DialogBox.getUserDialog(input, userImage)
        );

        if (isError) {
            dialogContainer.getChildren().add(
                    DialogBox.getErrorDialog(response, mochiImage)
            );
        } else {
            dialogContainer.getChildren().add(
                    DialogBox.getMochiDialog(response, mochiImage)
            );
        }

        userInput.clear();

        // Close the app if user typed "bye"
        if (mochi.shouldExit()) {
            // Small delay so the goodbye message actually appears before closing
            PauseTransition delay = new PauseTransition(Duration.millis(1200));
            delay.setOnFinished(e -> Platform.exit()); // cleanly terminates JavaFX app
            delay.play();
        }
    }
}
