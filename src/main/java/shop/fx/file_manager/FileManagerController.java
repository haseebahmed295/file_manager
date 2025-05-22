package shop.fx.file_manager;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileManagerController {

    private final FileManagerUI ui;
    private final FileSystemUtils fileSystemUtils;
    private Path currentPath;
    private final List<Path> navigationHistory;
    private int historyIndex;
    private final Image maximizeImage;
    private final Image restoreImage;

    public FileManagerController(FileManagerUI ui, FileSystemUtils fileSystemUtils) {
        this.ui = ui;
        this.fileSystemUtils = fileSystemUtils;
        this.navigationHistory = new ArrayList<>();
        this.historyIndex = -1;
        // Load maximize and restore icons
        maximizeImage = new Image(getClass().getResourceAsStream("icons/maximize_64.png"));
        restoreImage = new Image(getClass().getResourceAsStream("icons/restore_64.png"));
        if (maximizeImage.isError() || restoreImage.isError()) {
            System.err.println("Failed to load maximize_64.png or restore_64.png");
        }
        setupEventHandlers();
        setupCellFactories();
        loadDrives();
    }

    private void setupEventHandlers() {
        // Minimize button
        ui.getMinimizeButton().setOnAction(e -> {
            Stage stage = (Stage) ui.getRoot().getScene().getWindow();
            stage.setIconified(true);
        });

        // Maximize button
        ui.getMaximizeButton().setOnAction(e -> {
            Stage stage = (Stage) ui.getRoot().getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
            ImageView icon = (ImageView) ui.getMaximizeButton().getGraphic();
            icon.setImage(stage.isMaximized() ? restoreImage : maximizeImage);
        });

        // Close button
        ui.getCloseButton().setOnAction(e -> Platform.exit());

        // Home button
        ui.getHomeButton().setOnAction(e -> {
            loadDrives();
            navigationHistory.clear();
            historyIndex = -1;
        });

        // Back button
        ui.getBackButton().setOnAction(event -> {
            if (historyIndex > 0) {
                historyIndex--;
                try {
                    loadDirectory(navigationHistory.get(historyIndex));
                } catch (IOException e) {
                    System.err.println("Error navigating back: " + e.getMessage());
                }
            } else {
                loadDrives();
            }
        });

        // Forward button
        ui.getForwardButton().setOnAction(event -> {
            if (historyIndex < navigationHistory.size() - 1) {
                historyIndex++;
                try {
                    loadDirectory(navigationHistory.get(historyIndex));
                } catch (IOException e) {
                    System.err.println("Error navigating forward: " + e.getMessage());
                }
            }
        });

        // File list double-click to navigate folders
        ui.getFileListView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
                if (selectedPath != null) {
                    try {
                        if (Files.isDirectory(selectedPath)) {
                            while (navigationHistory.size() > historyIndex + 1) {
                                navigationHistory.remove(navigationHistory.size() - 1);
                            }
                            navigationHistory.add(selectedPath);
                            historyIndex++;
                            loadDirectory(selectedPath);
                        }
                    } catch (AccessDeniedException e) {
                        System.err.println("Caught AccessDeniedException for: " + selectedPath);
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Access Denied");
                        alert.setHeaderText(null);
                        alert.setContentText("Access Denied: Cannot open folder " + selectedPath.toString());
                        alert.showAndWait();
                    } catch (IOException e) {
                        System.err.println("IOException accessing directory: " + selectedPath + ", Message: " + e.getMessage());
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Error accessing folder: " + e.getMessage());
                        alert.showAndWait();
                    }
                }
            }
        });

        // Drive selection from sidebar (single-click)
        ui.getDriveListView().setOnMouseClicked(event -> {
            DriveInfo selectedDrive = ui.getDriveListView().getSelectionModel().getSelectedItem();
            if (selectedDrive != null) {
                try {
                    while (navigationHistory.size() > historyIndex + 1) {
                        navigationHistory.remove(navigationHistory.size() - 1);
                    }
                    navigationHistory.add(selectedDrive.getPath());
                    historyIndex++;
                    loadDirectory(selectedDrive.getPath());
                } catch (AccessDeniedException e) {
                    System.err.println("Caught AccessDeniedException for drive: " + selectedDrive.getPath());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Access Denied");
                    alert.setHeaderText(null);
                    alert.setContentText("Access Denied: Cannot open drive " + selectedDrive.getPath().toString());
                    alert.showAndWait();
                } catch (IOException e) {
                    System.err.println("IOException accessing drive: " + selectedDrive.getPath() + ", Message: " + e.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error accessing drive: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });

        // Drive selection from home view grid (single-click)
        ui.getMainContent().setOnMouseClicked(event -> {
            if (ui.getMainContent().getChildren().get(1) instanceof GridPane) {
                Node target = event.getPickResult().getIntersectedNode();
                System.out.println("Clicked node: " + target);
                while (target != null && !(target instanceof GridPane && target.getStyleClass().contains("drive-cell-content"))) {
                    target = target.getParent();
                }
                if (target != null) {
                    DriveInfo selectedDrive = (DriveInfo) target.getUserData();
                    System.out.println("Selected drive: " + (selectedDrive != null ? selectedDrive.getPath() : "null"));
                    if (selectedDrive != null) {
                        try {
                            while (navigationHistory.size() > historyIndex + 1) {
                                navigationHistory.remove(navigationHistory.size() - 1);
                            }
                            navigationHistory.add(selectedDrive.getPath());
                            historyIndex++;
                            loadDirectory(selectedDrive.getPath());
                        } catch (AccessDeniedException e) {
                            System.err.println("Caught AccessDeniedException for drive: " + selectedDrive.getPath());
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Access Denied");
                            alert.setHeaderText(null);
                            alert.setContentText("Access Denied: Cannot open drive " + selectedDrive.getPath().toString());
                            alert.showAndWait();
                        } catch (IOException e) {
                            System.err.println("IOException accessing drive: " + selectedDrive.getPath() + ", Message: " + e.getMessage());
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Error accessing drive: " + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                }
            }
        });

        // Search field
        ui.getSearchField().textProperty().addListener((obs, oldValue, newValue) -> {
            if (currentPath != null) {
                try {
                    ui.getFileListView().getItems().clear();
                    fileSystemUtils.searchDirectory(currentPath, newValue, ui.getFileListView());
                } catch (IOException e) {
                    System.err.println("Error searching directory: " + e.getMessage());
                }
            }
        });
    }

    private void setupCellFactories() {
        // File list cell factory (with icon, name, size, and extension columns)
        ui.getFileListView().setCellFactory(param -> new ListCell<>() {
            private final ImageView icon = new ImageView();
            private final Label name = new Label();
            private final Label sizeInfo = new Label();
            private final Label extensionInfo = new Label();
            private final GridPane content = new GridPane();

            {
                // Define column constraints
                ColumnConstraints iconCol = new ColumnConstraints(24); // Fixed for icon
                iconCol.setHalignment(HPos.LEFT);
                ColumnConstraints nameCol = new ColumnConstraints();
                nameCol.setHgrow(Priority.ALWAYS); // Flexible width
                nameCol.setHalignment(HPos.LEFT);
                ColumnConstraints sizeCol = new ColumnConstraints(100); // Fixed for size
                sizeCol.setHalignment(HPos.RIGHT);
                ColumnConstraints extCol = new ColumnConstraints(60); // Fixed for extension
                extCol.setHalignment(HPos.CENTER);
                content.getColumnConstraints().addAll(iconCol, nameCol, sizeCol, extCol);
                content.setHgap(10);
                content.setAlignment(Pos.CENTER_LEFT);

                // Add nodes to grid
                content.add(icon, 0, 0);
                content.add(name, 1, 0);
                content.add(sizeInfo, 2, 0);
                content.add(extensionInfo, 3, 0);

                // Style nodes
                icon.getStyleClass().add("file-icon");
                icon.setFitHeight(16);
                icon.setPreserveRatio(true);
                name.getStyleClass().add("file-name");
                sizeInfo.getStyleClass().add("file-size");
                sizeInfo.setMinWidth(100);
                sizeInfo.setMaxWidth(100);
                extensionInfo.getStyleClass().add("file-extension");
                extensionInfo.setMinWidth(60);
                extensionInfo.setMaxWidth(60);
            }

            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    String fileName = item.getFileName() != null ? item.getFileName().toString() : item.toString();
                    try {
                        String extension = "";
                        if (Files.isDirectory(item)) {
                            Image folderImage = new Image(getClass().getResourceAsStream("icons/folder_64.png"));
                            if (folderImage.isError()) {
                                System.err.println("Failed to load folder_64.png");
                            }
                            icon.setImage(folderImage);
                            name.setText(fileName);
                            sizeInfo.setText("-");
                            extension = "-";
                        } else {
                            Image fileImage = new Image(getClass().getResourceAsStream("icons/file_64.png"));
                            if (fileImage.isError()) {
                                System.err.println("Failed to load file_64.png");
                            }
                            icon.setImage(fileImage);
                            name.setText(fileName);
                            long size = Files.size(item);
                            sizeInfo.setText(fileSystemUtils.formatSize(size));
                            int dotIndex = fileName.lastIndexOf('.');
                            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                                extension = fileName.substring(dotIndex + 1).toLowerCase();
                            } else {
                                extension = "-";
                            }
                        }
                        extensionInfo.setText(extension);
                    } catch (Exception e) {
                        Image lockedImage = new Image(getClass().getResourceAsStream("icons/lock_64.png"));
                        if (lockedImage.isError()) {
                            System.err.println("Failed to load lock_64.png");
                        }
                        icon.setImage(lockedImage);
                        name.setText(fileName);
                        sizeInfo.setText("(Access Denied)");
                        extensionInfo.setText("-");
                    }

                    setGraphic(content);
                    setText(null);

                    int index = getIndex();
                    int size = param.getItems().size();
                    if (index == 0) {
                        setStyle("-fx-background-radius: 8 8 0 0;");
                    } else if (index == size - 1) {
                        setStyle("-fx-background-radius: 0 0 8 8;");
                    } else {
                        setStyle("-fx-background-radius: 0;");
                    }
                }
            }
        });

        // Drive list cell factory (with custom icon on left, vertical title and progress bar)
        ui.getDriveListView().setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DriveInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    GridPane cellContent = new GridPane();
                    cellContent.getStyleClass().add("drive-cell-content");

                    // Drive icon
                    ImageView driveIcon = new ImageView();
                    driveIcon.getStyleClass().add("drive-icon");
                    driveIcon.setFitHeight(16);
                    driveIcon.setPreserveRatio(true);
                    Image driveImage = new Image(getClass().getResourceAsStream("icons/hdd_64.png"));
                    if (driveImage.isError()) {
                        System.err.println("Failed to load hdd_64.png");
                    }
                    driveIcon.setImage(driveImage);

                    // Vertical arrangement of drive name and progress bar
                    VBox textAndProgress = new VBox(8);
                    Label driveName = new Label(item.getDisplayName());
                    driveName.getStyleClass().add("drive-name");
                    ProgressBar progressBar = new ProgressBar();
                    progressBar.getStyleClass().add("drive-progress");
                    progressBar.setMaxWidth(160);
                    textAndProgress.getChildren().addAll(driveName, progressBar);

                    try {
                        FileStore store = Files.getFileStore(item.getPath());
                        long totalSpace = store.getTotalSpace();
                        long freeSpace = store.getUsableSpace();
                        long usedSpace = totalSpace - freeSpace;
                        double progress = totalSpace > 0 ? (double) usedSpace / totalSpace : 0;
                        progressBar.setProgress(progress);
                    } catch (IOException e) {
                        System.err.println("Error calculating progress for drive: " + item.getDisplayName());
                        progressBar.setProgress(0);
                    }

                    // Define column constraints
                    ColumnConstraints iconCol = new ColumnConstraints(24);
                    iconCol.setHalignment(HPos.LEFT);
                    ColumnConstraints textCol = new ColumnConstraints();
                    textCol.setHgrow(Priority.ALWAYS);
                    textCol.setHalignment(HPos.LEFT);
                    cellContent.getColumnConstraints().addAll(iconCol, textCol);
                    cellContent.setHgap(8);
                    cellContent.add(driveIcon, 0, 0);
                    cellContent.add(textAndProgress, 1, 0);

                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });
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
        try {
            ui.getMainContent().getChildren().set(1, ui.getFileListView());
            ui.getFileListView().getItems().clear();
            fileSystemUtils.loadDirectory(directory, ui.getFileListView());
            currentPath = directory;
            ui.getPathField().setText(directory.toString());
            ui.getSearchField().setText("");
        } catch (AccessDeniedException e) {
            throw e; // Propagate AccessDeniedException
        }
    }
}