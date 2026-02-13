package mochi.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Represents a dialog box consisting of an ImageView and a label containing text.
 */
public class DialogBox extends HBox {

    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader =
                    new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        getChildren().setAll(tmp);
        setAlignment(Pos.TOP_LEFT);
    }

    /**
     * Creates a dialog box representing the user.
     *
     * @param text Dialog text.
     * @param img User image.
     * @return DialogBox instance.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Creates a dialog box representing Mochi.
     *
     * @param text Dialog text.
     * @param img Mochi image.
     * @return DialogBox instance.
     */
    public static DialogBox getMochiDialog(String text, Image img) {
        DialogBox db = new DialogBox(text, img);
        db.flip();
        return db;
    }

    /**
     * Applies inline styling to visually highlight this dialog as an error message.
     *
     * <p>The style uses a light red background and darker red text to make errors
     * stand out from normal replies. This method is intended to be called after
     * the FXML has been loaded and {@code dialog} has been injected.
     */
    private void setErrorStyle() {
        dialog.setStyle(
                "-fx-background-color: #ffdddd;"
                        + "-fx-text-fill: #8a0000;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-background-radius: 10;"
        );
    }

    /**
     * Creates an error dialog box for displaying an error message in the chat UI.
     *
     * <p>The returned dialog box:
     * <ul>
     *   <li>uses the error styling applied by {@link #setErrorStyle()}</li>
     *   <li>is flipped so that it appears on the left, consistent with Mochi replies</li>
     * </ul>
     *
     * @param text Error message text to display.
     * @param img  Display picture to show alongside the message.
     * @return A {@code DialogBox} styled and positioned as an error dialog.
     */
    public static DialogBox getErrorDialog(String text, Image img) {
        var db = new DialogBox(text, img);
        db.setErrorStyle();
        db.flip(); // keep Mochi/error on the left like Mochi dialog
        return db;
    }
}
