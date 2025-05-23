package shop.fx.file_manager;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;

public class EventHandlerSetup {

    private final FileManagerUI ui;
    private final FileSystemUtils fileSystemUtils;
    private final FileManagerController controller;
    private final FileOperations fileOperations;

    public EventHandlerSetup(FileManagerUI ui, FileSystemUtils fileSystemUtils, FileManagerController controller) {
        this.ui = ui;
        this.fileSystemUtils = fileSystemUtils;
        this.controller = controller;
        this.fileOperations = new FileOperations(ui, fileSystemUtils, controller);
    }

    public void setupEventHandlers() {
        ui.getMinimizeButton().setOnAction(e -> {
            Stage stage = (Stage) ui.getRoot().getScene().getWindow();
            stage.setIconified(true);
        });

        ui.getMaximizeButton().setOnAction(e -> {
            Stage stage = (Stage) ui.getRoot().getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
            ImageView icon = (ImageView) ui.getMaximizeButton().getGraphic();
            icon.setImage(stage.isMaximized() ? controller.getRestoreImage() : controller.getMaximizeImage());
        });

        ui.getCloseButton().setOnAction(e -> Platform.exit());

        ui.getHomeButton().setOnAction(e -> {
            controller.loadDrives();
            controller.getNavigationHistory().clear();
            controller.setHistoryIndex(-1);
        });

        ui.getBackButton().setOnAction(_ -> {
            if (controller.getHistoryIndex() > 0) {
                controller.setHistoryIndex(controller.getHistoryIndex() - 1);
                try {
                    controller.loadDirectory(controller.getNavigationHistory().get(controller.getHistoryIndex()));
                } catch (IOException e) {
                    System.err.println("Error navigating back: " + e.getMessage());
                    controller.showErrorDialog("Error", "Error navigating back: " + e.getMessage());
                }
            } else {
                controller.loadDrives();
            }
        });

        ui.getForwardButton().setOnAction(_ -> {
            if (controller.getHistoryIndex() < controller.getNavigationHistory().size() - 1) {
                controller.setHistoryIndex(controller.getHistoryIndex() + 1);
                try {
                    controller.loadDirectory(controller.getNavigationHistory().get(controller.getHistoryIndex()));
                } catch (IOException e) {
                    System.err.println("Error navigating forward: " + e.getMessage());
                    controller.showErrorDialog("Error", "Error navigating forward: " + e.getMessage());
                }
            }
        });

        // Context menu for fileListView
        ContextMenu contextMenu = new ContextMenu();
        MenuItem openItem = new MenuItem("Open");
        MenuItem renameItem = new MenuItem("Rename");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem copyItem = new MenuItem("Copy");
        MenuItem pasteItem = new MenuItem("Paste");

        openItem.getStyleClass().add("menu-item");
        renameItem.getStyleClass().add("menu-item");
        deleteItem.getStyleClass().add("menu-item");
        copyItem.getStyleClass().add("menu-item");
        pasteItem.getStyleClass().add("menu-item");

        openItem.setOnAction(_ -> {
            Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
            if (selectedPath != null) {
                try {
                    fileOperations.openItem(selectedPath);
                } catch (IOException e) {
                    System.err.println("Error opening item: " + e.getMessage());
                    controller.showErrorDialog("Error", "Error opening item: " + e.getMessage());
                }
            } else {
                System.out.println("Open failed: No item selected");
                controller.showErrorDialog("Error", "No file or folder selected.");
            }
        });

        renameItem.setOnAction(_ -> {
            Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
            int selectedIndex = ui.getFileListView().getSelectionModel().getSelectedIndex();
            if (selectedPath != null && selectedIndex >= 0) {
                System.out.println("Initiating rename for: " + selectedPath + " at index: " + selectedIndex);
                fileOperations.handleRename(selectedPath, selectedIndex);
            } else {
                System.out.println("Rename failed: No item selected");
                controller.showErrorDialog("Error", "No file or folder selected.");
            }
        });

        deleteItem.setOnAction(e -> fileOperations.handleDelete());
        copyItem.setOnAction(e -> fileOperations.handleCopy());
        pasteItem.setOnAction(e -> fileOperations.handlePaste());

        contextMenu.getItems().addAll(openItem, renameItem, deleteItem, copyItem, pasteItem);
        ui.getFileListView().setContextMenu(contextMenu);

        ui.getFileListView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
                if (selectedPath != null) {
                    try {
                        if (Files.isDirectory(selectedPath)) {
                            while (controller.getNavigationHistory().size() > controller.getHistoryIndex() + 1) {
                                controller.getNavigationHistory().remove(controller.getNavigationHistory().size() - 1);
                            }
                            controller.getNavigationHistory().add(selectedPath);
                            controller.setHistoryIndex(controller.getHistoryIndex() + 1);
                            controller.loadDirectory(selectedPath);
                        } else {
                            fileOperations.openItem(selectedPath);
                        }
                    } catch (AccessDeniedException e) {
                        System.err.println("Caught AccessDeniedException for: " + selectedPath);
                        controller.showErrorDialog("Access Denied", "Access Denied: Cannot open " + selectedPath.toString());
                    } catch (IOException e) {
                        System.err.println("IOException accessing: " + selectedPath + ", Message: " + e.getMessage());
                        controller.showErrorDialog("Error", "Error accessing: " + e.getMessage());
                    }
                }
            }
        });

        ui.getDriveListView().setOnMouseClicked(event -> {
            DriveInfo selectedDrive = ui.getDriveListView().getSelectionModel().getSelectedItem();
            if (selectedDrive != null) {
                try {
                    while (controller.getNavigationHistory().size() > controller.getHistoryIndex() + 1) {
                        controller.getNavigationHistory().remove(controller.getNavigationHistory().size() - 1);
                    }
                    controller.getNavigationHistory().add(selectedDrive.getPath());
                    controller.setHistoryIndex(controller.getHistoryIndex() + 1);
                    controller.loadDirectory(selectedDrive.getPath());
                } catch (AccessDeniedException e) {
                    System.err.println("Caught AccessDeniedException for drive: " + selectedDrive.getPath());
                    controller.showErrorDialog("Access Denied", "Access Denied: Cannot open drive " + selectedDrive.getPath().toString());
                } catch (IOException e) {
                    System.err.println("IOException accessing drive: " + selectedDrive.getPath() + ", Message: " + e.getMessage());
                    controller.showErrorDialog("Error", "Error accessing drive: " + e.getMessage());
                }
            }
        });

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
                            while (controller.getNavigationHistory().size() > controller.getHistoryIndex() + 1) {
                                controller.getNavigationHistory().remove(controller.getNavigationHistory().size() - 1);
                            }
                            controller.getNavigationHistory().add(selectedDrive.getPath());
                            controller.setHistoryIndex(controller.getHistoryIndex() + 1);
                            controller.loadDirectory(selectedDrive.getPath());
                        } catch (AccessDeniedException e) {
                            System.err.println("Caught AccessDeniedException for drive: " + selectedDrive.getPath());
                            controller.showErrorDialog("Access Denied", "Access Denied: Cannot open drive " + selectedDrive.getPath().toString());
                        } catch (IOException e) {
                            System.err.println("IOException accessing drive: " + selectedDrive.getPath() + ", Message: " + e.getMessage());
                            controller.showErrorDialog("Error", "Error accessing drive: " + e.getMessage());
                        }
                    }
                }
            }
        });

        ui.getSearchField().textProperty().addListener((obs, oldValue, newValue) -> {
            if (controller.getCurrentPath() != null) {
                try {
                    ui.getFileListView().getItems().clear();
                    fileSystemUtils.searchDirectory(controller.getCurrentPath(), newValue, ui.getFileListView());
                } catch (IOException e) {
                    System.err.println("Error searching directory: " + e.getMessage());
                    controller.showErrorDialog("Error", "Error searching directory: " + e.getMessage());
                }
            }
        });
    }
}