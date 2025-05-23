package shop.fx.file_manager;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileManagerController {

    private final FileManagerUI ui;
    private final FileSystemUtils fileSystemUtils;
    private Path currentPath;
    private final List<Path> navigationHistory;
    private int historyIndex;
    private Path copiedPath; // Store the path for copy-paste operations
    private final Image maximizeImage;
    private final Image restoreImage;

    public FileManagerController(FileManagerUI ui, FileSystemUtils fileSystemUtils) {
        this.ui = ui;
        this.fileSystemUtils = fileSystemUtils;
        this.navigationHistory = new ArrayList<>();
        this.historyIndex = -1;
        this.copiedPath = null;
        maximizeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/maximize_64.png")));
        restoreImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/restore_64.png")));
        if (maximizeImage.isError() || restoreImage.isError()) {
            System.err.println("Failed to load maximize_64.png or restore_64.png");
        }
        initialize();
    }

    private void initialize() {
        // Initialize helper classes
        CellFactorySetup cellFactorySetup = new CellFactorySetup(ui, fileSystemUtils , this);
        EventHandlerSetup eventHandlerSetup = new EventHandlerSetup(ui, fileSystemUtils, this);
        cellFactorySetup.setupCellFactories();
        eventHandlerSetup.setupEventHandlers();
        loadDrives();
    }

    public void showErrorDialog(String title, String message) {
        CustomErrorDialog dialog = new CustomErrorDialog((Stage) ui.getRoot().getScene().getWindow(), title, message);
        dialog.showAndWait();
    }

    public void loadDrives() {
        ui.getDriveListView().getItems().clear();
        fileSystemUtils.loadDrives(ui.getDriveListView());
        System.out.println("Drive list items: " + ui.getDriveListView().getItems().size());
        fileSystemUtils.loadDrivesInGrid(ui.getMainContent());
        currentPath = null;
        ui.getPathField().setText("");
        ui.getSearchField().setText("");
    }

    public void loadDirectory(Path directory) throws IOException {
        if (!Files.isReadable(directory)) {
            throw new AccessDeniedException(directory.toString());
        }
        ui.getMainContent().getChildren().set(1, ui.getFileListView());
        ui.getFileListView().getItems().clear();
        fileSystemUtils.loadDirectory(directory, ui.getFileListView());
        currentPath = directory;
        ui.getPathField().setText(directory.toString());
        ui.getSearchField().setText("");
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public List<Path> getNavigationHistory() {
        return navigationHistory;
    }

    public int getHistoryIndex() {
        return historyIndex;
    }

    public void setHistoryIndex(int historyIndex) {
        this.historyIndex = historyIndex;
    }

    public Path getCopiedPath() {
        return copiedPath;
    }

    public void setCopiedPath(Path copiedPath) {
        this.copiedPath = copiedPath;
    }

    public Image getMaximizeImage() {
        return maximizeImage;
    }

    public Image getRestoreImage() {
        return restoreImage;
    }
}