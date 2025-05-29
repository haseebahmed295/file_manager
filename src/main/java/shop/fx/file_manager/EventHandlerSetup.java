package shop.fx.file_manager;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

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
        ui.getMinimizeButton().setOnAction(_ -> {
            Stage stage = (Stage) ui.getRoot().getScene().getWindow();
            stage.setIconified(true);
        });

        ui.getMaximizeButton().setOnAction(_ -> {
            Stage stage = (Stage) ui.getRoot().getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
            ImageView icon = (ImageView) ui.getMaximizeButton().getGraphic();
            icon.setImage(stage.isMaximized() ? controller.getRestoreImage():controller.getMaximizeImage());
        });

        ui.getCloseButton().setOnAction(_ -> Platform.exit());

        ui.getHomeButton().setOnAction(_ -> {
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
        MenuItem cutItem = new MenuItem("Cut");
        MenuItem pasteItem = new MenuItem("Paste");
        MenuItem newFolderItem = new MenuItem("New Folder");
        MenuItem pinFolderItem = new MenuItem("Pin Folder");

        openItem.getStyleClass().add("menu-item");
        renameItem.getStyleClass().add("menu-item");
        deleteItem.getStyleClass().add("menu-item");
        copyItem.getStyleClass().add("menu-item");
        cutItem.getStyleClass().add("menu-item");
        pasteItem.getStyleClass().add("menu-item");
        newFolderItem.getStyleClass().add("menu-item");
        pinFolderItem.getStyleClass().add("menu-item");

        // Set icons for context menu items
        Image openImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/open_64.png")));
        if (openImage.isError()) {
            System.err.println("Failed to load open_64.png");
        }
        ImageView openIcon = new ImageView(openImage);
        openIcon.setFitHeight(24);
        openIcon.setPreserveRatio(true);
        openItem.setGraphic(openIcon);

        Image renameImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/rename_64.png")));
        if (renameImage.isError()) {
            System.err.println("Failed to load rename_64.png");
        }
        ImageView renameIcon = new ImageView(renameImage);
        renameIcon.setFitHeight(20);
        renameIcon.setPreserveRatio(true);
        renameItem.setGraphic(renameIcon);

        Image deleteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/delete_64.png")));
        if (deleteImage.isError()) {
            System.err.println("Failed to load delete_64.png");
        }
        ImageView deleteIcon = new ImageView(deleteImage);
        deleteIcon.setFitHeight(20);
        deleteIcon.setPreserveRatio(true);
        deleteItem.setGraphic(deleteIcon);

        Image copyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/copy_64.png")));
        if (copyImage.isError()) {
            System.err.println("Failed to load copy_64.png");
        }
        ImageView copyIcon = new ImageView(copyImage);
        copyIcon.setFitHeight(20);
        copyIcon.setPreserveRatio(true);
        copyItem.setGraphic(copyIcon);

        Image cutImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/cut_64.png")));
        if (cutImage.isError()) {
            System.err.println("Failed to load cut_64.png");
        }
        ImageView cutIcon = new ImageView(cutImage);
        cutIcon.setFitHeight(20);
        cutIcon.setPreserveRatio(true);
        cutItem.setGraphic(cutIcon);

        Image pasteImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/paste_64.png")));
        if (pasteImage.isError()) {
            System.err.println("Failed to load paste_64.png");
        }
        ImageView pasteIcon = new ImageView(pasteImage);
        pasteIcon.setFitHeight(20);
        pasteIcon.setPreserveRatio(true);
        pasteItem.setGraphic(pasteIcon);

        Image newFolderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/new_folder_64.png")));
        if (newFolderImage.isError()) {
            System.err.println("Failed to load new_folder_64.png");
        }
        ImageView newFolderIcon = new ImageView(newFolderImage);
        newFolderIcon.setFitHeight(20);
        newFolderIcon.setPreserveRatio(true);
        newFolderItem.setGraphic(newFolderIcon);

        Image pinImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/pin_64.png")));
        if (pinImage.isError()) {
            System.err.println("Failed to load pin_64.png");
        }
        ImageView pinIcon = new ImageView(pinImage);
        pinIcon.setFitHeight(20);
        pinIcon.setPreserveRatio(true);
        pinFolderItem.setGraphic(pinIcon);

        Image unpinImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/unpin_64.png")));
        if (unpinImage.isError()) {
            System.err.println("Failed to load unpin_64.png");
        }
        ImageView unpinIcon = new ImageView(unpinImage);
        unpinIcon.setFitHeight(20);
        unpinIcon.setPreserveRatio(true);

        // Bind Paste item state to clipboard content
        pasteItem.disableProperty().bind(Bindings.isNull(controller.copiedPathProperty()));
        pasteIcon.opacityProperty().bind(Bindings.when(pasteItem.disableProperty())
                .then(0.5)
                .otherwise(1.0));

        // Bind Pin/Unpin item visibility based on selection
        pinFolderItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
            try {
                return selectedPath != null && Files.exists(selectedPath) && Files.isDirectory(selectedPath);
            } catch (Exception e) {
                System.err.println("Error checking if path is directory: " + selectedPath + ", Message: " + e.getMessage());
                return false;
            }
        }, ui.getFileListView().getSelectionModel().selectedItemProperty()));

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

        deleteItem.setOnAction(_ -> fileOperations.handleDelete());
        copyItem.setOnAction(_ -> fileOperations.handleCopy());
        cutItem.setOnAction(_ -> fileOperations.handleCut());
        pasteItem.setOnAction(_ -> fileOperations.handlePaste());
        newFolderItem.setOnAction(_ -> fileOperations.handleNewFolder());

        pinFolderItem.setOnAction(_ -> {
            Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
            if (selectedPath != null) {
                fileOperations.handlePinFolder(selectedPath);
            }
        });

        contextMenu.getItems().addAll(openItem, renameItem, deleteItem, copyItem, cutItem, pasteItem, newFolderItem, pinFolderItem);
        ui.getFileListView().setContextMenu(contextMenu);

        // Context menu for pinnedFoldersListView
        ContextMenu pinnedContextMenu = new ContextMenu();
        MenuItem pinnedOpenItem = new MenuItem("Open");
        MenuItem pinnedUnpinItem = new MenuItem("Unpin Folder");
        pinnedOpenItem.getStyleClass().add("menu-item");
        pinnedUnpinItem.getStyleClass().add("menu-item");

        ImageView PinOpenIcon = new ImageView(openImage);
        PinOpenIcon.setFitHeight(20);
        PinOpenIcon.setPreserveRatio(true);
        pinnedOpenItem.setGraphic(PinOpenIcon);
        pinnedUnpinItem.setGraphic(unpinIcon);
        pinnedContextMenu.getItems().addAll(pinnedOpenItem, pinnedUnpinItem);

        pinnedOpenItem.setOnAction(_ -> {
            Path selectedPath = ui.getPinnedFoldersListView().getSelectionModel().getSelectedItem();
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

        pinnedUnpinItem.setOnAction(_ -> {
            Path selectedPath = ui.getPinnedFoldersListView().getSelectionModel().getSelectedItem();
            if (selectedPath != null) {
                fileOperations.handleUnpinFolder(selectedPath);
            }
        });

        ui.getPinnedFoldersListView().setContextMenu(pinnedContextMenu);

        ui.getFileListView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
                if (selectedPath != null) {
                    try {
                        if (Files.isDirectory(selectedPath)) {
                            while (controller.getNavigationHistory().size() > controller.getHistoryIndex() + 1) {
                                controller.getNavigationHistory().removeLast();
                            }
                            controller.getNavigationHistory().add(selectedPath);
                            controller.setHistoryIndex(controller.getHistoryIndex() + 1);
                            controller.loadDirectory(selectedPath);
                        } else {
                            fileOperations.openItem(selectedPath);
                        }
                    } catch (AccessDeniedException e) {
                        System.err.println("Caught AccessDeniedException for: " + selectedPath);
                        controller.showErrorDialog("Access Denied", "Access Denied: Cannot open " + selectedPath);
                    } catch (IOException e) {
                        System.err.println("IOException accessing: " + selectedPath + ", Message: " + e.getMessage());
                        controller.showErrorDialog("Error", "Error accessing: " + e.getMessage());
                    }
                }
            }
        });

        ui.getPinnedFoldersListView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Path selectedPath = ui.getPinnedFoldersListView().getSelectionModel().getSelectedItem();
                if (selectedPath != null) {
                    try {
                        while (controller.getNavigationHistory().size() > controller.getHistoryIndex() + 1) {
                            controller.getNavigationHistory().removeLast();
                        }
                        controller.getNavigationHistory().add(selectedPath);
                        controller.setHistoryIndex(controller.getHistoryIndex() + 1);
                        controller.loadDirectory(selectedPath);
                    } catch (IOException e) {
                        System.err.println("Error opening pinned folder: " + e.getMessage());
                        controller.showErrorDialog("Error", "Error opening pinned folder: " + e.getMessage());
                    }
                }
            }
        });

        ui.getDriveListView().setOnMouseClicked(_ -> {
            DriveInfo selectedDrive = ui.getDriveListView().getSelectionModel().getSelectedItem();
            if (selectedDrive != null) {
                try {
                    while (controller.getNavigationHistory().size() > controller.getHistoryIndex() + 1) {
                        controller.getNavigationHistory().removeLast();
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
                                controller.getNavigationHistory().removeLast();
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

        ui.getSearchField().textProperty().addListener((_, oldValue, newValue) -> {
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

        // Setup cell factory for pinned folders
        ui.getPinnedFoldersListView().setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    GridPane cellContent = new GridPane();
                    cellContent.getStyleClass().add("pinned-folder-cell-content");

                    ImageView folderIcon = new ImageView(new Image(
                            Objects.requireNonNull(getClass().getResourceAsStream("icons/folder_64.png"))
                    ));
                    folderIcon.setFitHeight(16);
                    folderIcon.setPreserveRatio(true);

                    Label folderName = new Label(item.getFileName().toString());
                    folderName.getStyleClass().add("pinned-folder-name");

                    cellContent.setHgap(8);
                    cellContent.add(folderIcon, 0, 0);
                    cellContent.add(folderName, 1, 0);

                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });
    }
}