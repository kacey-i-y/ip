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
     * Initialises the main window after the FXML fields have been injected.
     *
     * <p>Sets up layout behaviour (fit-to-width), applies UI styling for the input box and
     * send button, configures the background colours, and registers listeners for viewport
     * styling and responsive bubble wrapping on window resize.
     */
    @FXML
    public void initialize() {
        scrollPane.setFitToWidth(true);

        setUserStyle();

        userInput.setScrollTop(0);

        setSendButtonStyle();

        String bg = "#2B2B2B";
        scrollPane.setStyle("-fx-background: " + bg + "; -fx-background-color: " + bg + ";");
        dialogContainer.setStyle("-fx-background-color: " + bg + ";");

        setViewPortStyle(bg);

        setWindowResize();
    }

    /**
     * Applies background styling to the ScrollPane's internal viewport.
     *
     * <p>The ScrollPane viewport is a child node created by JavaFX skinning, so it is only
     * available after the UI has been rendered. This method runs later on the JavaFX
     * application thread to safely lookup and style the viewport.
     *
     * @param bg Background colour (e.g. hex string like {@code "#2B2B2B"}).
     */
    private void setViewPortStyle(String bg) {
        Platform.runLater(() -> {
            var viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                viewport.setStyle("-fx-background-color: " + bg + ";");
            }
        });
    }

    /**
     * Registers a listener that updates chat bubble wrapping when the window is resized.
     *
     * <p>This recalculates a bubble wrap width based on the ScrollPane's viewport width,
     * clamps it to a reasonable range, and updates {@link DialogBox#setWrapWidth(double)}
     * so messages reflow neatly across different window sizes.
     */
    private void setWindowResize() {
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double viewportWidth = newBounds.getWidth();

            double bubbleWidth = viewportWidth * 0.80;

            bubbleWidth = Math.max(200, Math.min(bubbleWidth, 420));

            DialogBox.setWrapWidth(bubbleWidth);
        });
    }

    /**
     * Applies visual styling to the Send button.
     *
     * <p>Uses inline CSS to set background colour, rounded corners, border colour, and
     * border thickness.
     */
    private void setSendButtonStyle() {
        sendButton.setStyle(
                "-fx-background-color: #ffb6c1;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-radius: 10;"
                        + "-fx-border-color: #f2a6b3;"
                        + "-fx-border-width: 1;"
        );
    }

    /**
     * Applies visual styling to the user input text area.
     *
     * <p>Uses inline CSS to set background colour, rounded corners, border styling,
     * padding, and font configuration for a consistent look with the rest of the UI.
     */
    private void setUserStyle() {
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
    }

    /**
     * Injects the Mochi instance into this controller and displays the startup message.
     *
     * <p>This should be called by the application launcher after loading the FXML, so the
     * GUI can delegate user commands to the core {@link Mochi} logic.
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
     * Handles sending the current user input to Mochi and displaying the response.
     *
     * <p>Reads the text currently in the input box, asks Mochi for a response, appends the
     * user and bot dialog boxes into the chat container, clears the input field, and
     * scrolls to the latest message. If the user triggers an exit command, this method
     * also schedules the app to close after a short delay.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = mochi.getResponse(input);

        boolean isError = response != null && response.startsWith("Error:");

        checkErrorMessage(input, isError, response);

        userInput.clear();

        Platform.runLater(() -> scrollPane.setVvalue(1.0));

        // Close the app if user typed "bye"
        checkShouldExit();
    }

    /**
     * Checks whether Mochi has requested the application to exit and closes the app if so.
     *
     * <p>If {@link Mochi#shouldExit()} returns true, the GUI delays shutdown briefly so
     * the goodbye message can be seen before the JavaFX platform exits.
     */
    private void checkShouldExit() {
        if (mochi.shouldExit()) {
            // Small delay so the goodbye message actually appears before closing
            PauseTransition delay = new PauseTransition(Duration.millis(1200));
            delay.setOnFinished(e -> Platform.exit()); // cleanly terminates JavaFX app
            delay.play();
        }
    }

    /**
     * Adds the user's message and Mochi's reply to the dialog container.
     *
     * <p>The reply is styled differently depending on whether it is an error response.
     *
     * @param input   The raw text typed by the user.
     * @param isError Whether Mochi's response should be displayed using error styling.
     * @param response The response string produced by Mochi.
     */
    private void checkErrorMessage(String input, boolean isError, String response) {
        dialogContainer.getChildren().add(DialogBox.getUserDialog(input, userImage));
        dialogContainer.getChildren().add(
                isError ? DialogBox.getErrorDialog(response, mochiImage)
                        : DialogBox.getMochiDialog(response, mochiImage)
        );
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
