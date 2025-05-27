package shop.fx.file_manager;

import com.catwithawand.borderlessscenefx.scene.BorderlessScene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Objects;

public class CustomErrorDialog {
    private final Stage stage;

    public CustomErrorDialog(Stage owner, String title, String message) {
        // Initialize undecorated stage
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle(title);

        // Root layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dialog-root");

        // Custom header (mimics FileManagerUI header)
        HBox header = createHeader(title);
        header.setAlignment(Pos.CENTER);
        root.setTop(header);
        header.getStyleClass().add("dialog-header");

        // Content area
        VBox content = new VBox();
        content.getStyleClass().add("dialog-content");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("dialog-message");
        messageLabel.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().add(messageLabel);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10, 20, 10, 20));
        Image DataLogix = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/error_64.png")));
        if (DataLogix.isError()) {
            System.err.println("Failed to load close_64.png");
        }
        ImageView dialogIcon = new ImageView(DataLogix);
        dialogIcon.setFitHeight(32);
        dialogIcon.setPreserveRatio(true);
        messageLabel.setGraphic(dialogIcon);
        root.setCenter(content);

        // Create scene with BorderlessScene for drag functionality
        BorderlessScene scene = new BorderlessScene(stage, StageStyle.TRANSPARENT, root, Color.TRANSPARENT);
        scene.setMoveControl(header); // Make header draggable
        stage.setWidth(400);
        stage.setHeight(100);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        stage.setScene(scene);
    }

    private HBox createHeader(String title) {
        HBox header = new HBox();
        header.getStyleClass().add("header");


        // Title label
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");

        // Close button
        Button closeButton = new Button();
        Image closeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/close_64.png")));
        if (closeImage.isError()) {
            System.err.println("Failed to load close_64.png");
        }
        ImageView closeIcon = new ImageView(closeImage);
        closeIcon.setFitHeight(16);
        closeIcon.setPreserveRatio(true);
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().addAll("window-button", "close-button");
        closeButton.setOnAction(_ -> stage.close());

        // Spacer to push close button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        return header;
    }

    public void showAndWait() {
        stage.showAndWait();
    }
}