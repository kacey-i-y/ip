package mochi.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
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
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());

        dialogContainer.getChildren().add(
                DialogBox.getMochiDialog("Hello! I'm Mochi.\nWhat can I do for you?",
                        mochiImage));
    }

    /**
     * Injects the Mochi instance.
     *
     * @param mochi Mochi logic instance.
     */
    public void setMochi(Mochi mochi) {
        this.mochi = mochi;
    }

    /**
     * Creates two dialog boxes: one echoing user input and the other containing Mochi's reply.
     * Then appends them to the dialog container and clears the user input.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input == null) {
            return;
        }

        String response = mochi.getResponse(input);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getMochiDialog(response, mochiImage)
        );

        userInput.clear();

        if (mochi.shouldExit()) {
            Platform.exit();
        }
    }
}
