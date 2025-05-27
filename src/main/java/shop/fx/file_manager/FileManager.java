package shop.fx.file_manager;
import com.catwithawand.borderlessscenefx.scene.BorderlessScene;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.util.Objects;

public class FileManager extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize UI and controller
        FileManagerUI ui = new FileManagerUI();
        FileSystemUtils fileSystemUtils = new FileSystemUtils();
        FileManagerController controller = new FileManagerController(ui, fileSystemUtils);

        // Set up the scene
        BorderlessScene scene = new BorderlessScene(primaryStage, StageStyle.TRANSPARENT, ui.getRoot(), Color.TRANSPARENT);
        scene.setMoveControl(ui.getHeader());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("sidebar.css")).toExternalForm());

        // Configure and show the stage
        primaryStage.setTitle("File Manager");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/logo_64.png"))));
        primaryStage.setMinWidth(400);
        primaryStage.setWidth(1000); // Set initial width to 1000
        primaryStage.setHeight(600); // Set initial height to 600
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load initial data
        controller.loadDrives();
    }

    public static void main(String[] args) {
        launch(args);
    }
}