package mochi.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private TextArea userInput;

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

        userInput.setStyle(
                "-fx-background-color: #ffd1dc;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-radius: 10;"
                        + "-fx-border-color: #f2a6b3;"
                        + "-fx-border-width: 1;"
                        + "-fx-padding: 8;"
                        + "-fx-font-family: 'Comic Sans MS';"
                        + "-fx-font-size: 16px;"
                        + "-fx-text-box-border: transparent;"
                        + "-fx-padding: 6 10 6 10;"
                        + "-fx-background-insets: 0;"
        );

        userInput.setScrollTop(0);

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

        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double viewportWidth = newBounds.getWidth();

            double bubbleWidth = viewportWidth * 0.80;

            bubbleWidth = Math.max(200, Math.min(bubbleWidth, 420));

            DialogBox.setWrapWidth(bubbleWidth);
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

        dialogContainer.getChildren().add(DialogBox.getUserDialog(input, userImage));
        dialogContainer.getChildren().add(
                isError ? DialogBox.getErrorDialog(response, mochiImage)
                        : DialogBox.getMochiDialog(response, mochiImage)
        );

        userInput.clear();

        Platform.runLater(() -> scrollPane.setVvalue(1.0));

        // Close the app if user typed "bye"
        if (mochi.shouldExit()) {
            // Small delay so the goodbye message actually appears before closing
            PauseTransition delay = new PauseTransition(Duration.millis(1200));
            delay.setOnFinished(e -> Platform.exit()); // cleanly terminates JavaFX app
            delay.play();
        }
    }

    /**
     * Handles key presses in the input box.
     *
     * <p>Pressing {@code Enter} sends the message (same as clicking Send).
     * {@code Shift+Enter} is left untouched so the user can insert a newline.
     *
     * @param event The key event triggered by the user's key press.
     */
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            event.consume(); // stop TextArea adding a new line
            handleUserInput();
        }
    }
}
