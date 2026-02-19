package mochi.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

/**
 * Represents a dialog box consisting of an ImageView and a label containing text.
 */
public class DialogBox extends HBox {

    @FXML
    private Label dialog;

    @FXML
    private ImageView displayPicture;

    private static final DoubleProperty WRAP_WIDTH = new SimpleDoubleProperty(250);

    /**
     * Updates the max width used for wrapping text in all dialog bubbles.
     *
     * @param width New wrap width in pixels.
     */
    public static void setWrapWidth(double width) {
        WRAP_WIDTH.set(width);
    }

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
        centerCropToSquare(img);
        makeDisplayPictureCircular();

        dialog.setWrapText(true);
        dialog.setMinHeight(Region.USE_PREF_SIZE);
        dialog.maxWidthProperty().bind(WRAP_WIDTH);
        dialog.setPrefWidth(Region.USE_COMPUTED_SIZE);
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
        DialogBox db = new DialogBox(text, img);
        db.setUserStyle();
        return db;
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
        db.setMochiStyle();
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
                        + "-fx-font-family: 'Comic Sans MS'; -fx-font-size: 14px;"
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

    /**
     * Clips the {@code displayPicture} ImageView into a circular shape.
     *
     * <p>This creates a {@link Circle} clip based on the current {@code fitWidth} and
     * {@code fitHeight}, so the visible portion of the image appears as a round avatar.
     *
     * <p>The clip is also updated when the ImageView's layout bounds change, keeping the
     * circle centered and correctly sized if the ImageView is resized.
     */
    private void makeDisplayPictureCircular() {
        displayPicture.setPreserveRatio(true);

        double size = Math.min(displayPicture.getFitWidth(), displayPicture.getFitHeight());
        if (size <= 0) {
            size = 70;
            displayPicture.setFitWidth(size);
            displayPicture.setFitHeight(size);
        }

        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        displayPicture.setClip(clip);

        displayPicture.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            double s = Math.min(bounds.getWidth(), bounds.getHeight());
            clip.setRadius(s / 2.0);
            clip.setCenterX(bounds.getWidth() / 2.0);
            clip.setCenterY(bounds.getHeight() / 2.0);
        });
    }

    /**
     * Crops the given image to a centered square region and applies it to {@code displayPicture}.
     *
     * <p>This helps keep the subject centered when we later clip the ImageView into a circle,
     * especially if the original image is not 1:1 (e.g., wider or taller than it is wide).
     *
     * @param img The image currently shown by {@code displayPicture}.
     */
    private void centerCropToSquare(Image img) {
        double imgW = img.getWidth();
        double imgH = img.getHeight();
        double side = Math.min(imgW, imgH);

        double x = (imgW - side) / 2.0;
        double y = (imgH - side) / 2.0;

        displayPicture.setViewport(new Rectangle2D(x, y, side, side));
    }

    /**
     * Applies inline styling for user messages (right side).
     *
     * <p>Uses a brighter pink bubble with readable dark text.</p>
     */
    private void setUserStyle() {
        dialog.setStyle(
                "-fx-background-color: #ffb6c1;"
                        + "-fx-text-fill: #4a1f2a;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-background-radius: 10;"
                        + "-fx-font-family: 'Comic Sans MS'; -fx-font-size: 14px;"
                        + "-fx-font-size: 16px;"
        );
    }

    /**
     * Applies inline styling for Mochi messages (left side).
     *
     * <p>Uses a softer neutral bubble to contrast user messages.</p>
     */
    private void setMochiStyle() {
        dialog.setStyle(
                "-fx-background-color: #f2f2f2;"
                        + "-fx-text-fill: #222222;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-background-radius: 10;"
                        + "-fx-font-family: 'Comic Sans MS'; -fx-font-size: 14px;"
        );
    }
}
