package shop.fx.file_manager;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class CellFactorySetup {

    private final FileManagerUI ui;
    private final FileSystemUtils fileSystemUtils;
    private final FileManagerController controller;

    public CellFactorySetup(FileManagerUI ui, FileSystemUtils fileSystemUtils, FileManagerController controller) {
        this.ui = ui;
        this.fileSystemUtils = fileSystemUtils;
        this.controller = controller;
    }

    public void setupCellFactories() {
        ui.getFileListView().setCellFactory(param -> new ListCell<>() {
            private final ImageView icon = new ImageView();
            private final Label name = new Label();
            private final TextField nameField = new TextField();
            private final Label sizeInfo = new Label();
            private final Label extensionInfo = new Label();
            private final GridPane content = new GridPane();
            private boolean isEditing = false;

            {
                ColumnConstraints iconCol = new ColumnConstraints(24);
                iconCol.setHalignment(HPos.LEFT);
                ColumnConstraints nameCol = new ColumnConstraints();
                nameCol.setHgrow(Priority.ALWAYS);
                nameCol.setHalignment(HPos.LEFT);
                ColumnConstraints sizeCol = new ColumnConstraints(100);
                sizeCol.setHalignment(HPos.RIGHT);
                ColumnConstraints extCol = new ColumnConstraints(60);
                extCol.setHalignment(HPos.CENTER);
                content.getColumnConstraints().addAll(iconCol, nameCol, sizeCol, extCol);
                content.setHgap(10);
                content.setAlignment(Pos.CENTER_LEFT);

                content.add(icon, 0, 0);
                content.add(name, 1, 0);
                content.add(sizeInfo, 2, 0);
                content.add(extensionInfo, 3, 0);

                icon.getStyleClass().add("file-icon");
                icon.setFitHeight(16);
                icon.setPreserveRatio(true);
                name.getStyleClass().add("file-name");
                nameField.getStyleClass().add("file-name-field");
                sizeInfo.getStyleClass().add("file-size");
                sizeInfo.setMinWidth(100);
                sizeInfo.setMaxWidth(100);
                extensionInfo.getStyleClass().add("file-extension");
                extensionInfo.setMinWidth(60);
                extensionInfo.setMaxWidth(60);

                setEditable(true);
                nameField.setOnAction(event -> {
                    if (isEditing) {
                        System.out.println("Committing edit via Enter key for: " + getItem());
                        commitEdit(getItem());
                    }
                    event.consume();
                });
                nameField.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE && isEditing) {
                        System.out.println("Canceling edit for: " + getItem());
                        cancelEdit();
                        event.consume();
                    }
                });
                nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused && isEditing) {
                        System.out.println("Committing edit via focus loss for: " + getItem());
                        commitEdit(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                    isEditing = false;
                    System.out.println("Updating empty cell at index: " + getIndex());
                } else {
                    String fileName = item.getFileName() != null ? item.getFileName().toString() : item.toString();
                    boolean isDirectory = false;
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(item, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                        isDirectory = attrs.isDirectory();
                        System.out.println("Path: " + item + ", isDirectory: " + isDirectory);
                    } catch (AccessDeniedException e) {
                        System.err.println("Access denied checking attributes for: " + item);
                    } catch (IOException e) {
                        System.err.println("IOException checking attributes for: " + item + ", Message: " + e.getMessage());
                    }

                    try {
                        String extension = "-";
                        if (isDirectory) {
                            Image folderImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/folder_64.png")));
                            if (folderImage.isError()) {
                                System.err.println("Failed to load folder_64.png");
                            }
                            icon.setImage(folderImage);
                            name.setText(fileName);
                            nameField.setText(fileName);
                            sizeInfo.setText("-");
                        } else {
                            Image fileImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/file_64.png")));
                            if (fileImage.isError()) {
                                System.err.println("Failed to load file_64.png");
                            }
                            icon.setImage(fileImage);
                            name.setText(fileName);
                            nameField.setText(fileName);
                            try {
                                long size = Files.size(item);
                                sizeInfo.setText(fileSystemUtils.formatSize(size));
                            } catch (AccessDeniedException e) {
                                sizeInfo.setText("(Access Denied)");
                            }
                            int dotIndex = fileName.lastIndexOf('.');
                            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                                extension = fileName.substring(dotIndex + 1).toLowerCase();
                            }
                        }
                        extensionInfo.setText(extension);
                    } catch (AccessDeniedException e) {
                        System.err.println("Access denied for: " + item);
                        Image lockedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/lock_64.png")));
                        if (lockedImage.isError()) {
                            System.err.println("Failed to load lock_64.png");
                        }
                        icon.setImage(lockedImage);
                        name.setText(fileName);
                        nameField.setText(fileName);
                        sizeInfo.setText("(Access Denied)");
                        extensionInfo.setText("-");
                    } catch (IOException e) {
                        System.err.println("IOException for: " + item + ", Message: " + e.getMessage());
                        Image lockedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/lock_64.png")));
                        if (lockedImage.isError()) {
                            System.err.println("Failed to load lock_64.png");
                        }
                        icon.setImage(lockedImage);
                        name.setText(fileName);
                        nameField.setText(fileName);
                        sizeInfo.setText("(Error)");
                        extensionInfo.setText("-");
                    }

                    if (!isEditing) {
                        content.getChildren().remove(nameField);
                        if (!content.getChildren().contains(name)) {
                            content.add(name, 1, 0);
                        }
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
                    System.out.println("Updated cell at index: " + index + " with item: " + fileName);
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (getItem() != null && !isEditing) {
                    System.out.println("Starting edit for item: " + getItem());
                    isEditing = true;
                    content.getChildren().remove(name);
                    if (!content.getChildren().contains(nameField)) {
                        content.add(nameField, 1, 0);
                    }
                    nameField.requestFocus();
                    nameField.selectAll();
                    setGraphic(content);
                }
            }

            @Override
            public void commitEdit(Path item) {
                if (!isEditing) {
                    return;
                }
                super.commitEdit(item);
                isEditing = false;
                if (item != null) {
                    String newName = nameField.getText().trim();
                    System.out.println("Committing edit for: " + item + ", new name: " + newName);
                    if (!newName.isEmpty() && !newName.equals(name.getText())) {
                        try {
                            Path newPath = item.getParent().resolve(newName);
                            Files.move(item, newPath, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Renamed " + item + " to " + newPath);
                            controller.loadDirectory(controller.getCurrentPath());
                        } catch (IOException e) {
                            System.err.println("Error renaming: " + e.getMessage());
                            controller.showErrorDialog("Error", "Error renaming file or folder: " + e.getMessage());
                        }
                    }
                }
                content.getChildren().remove(nameField);
                if (!content.getChildren().contains(name)) {
                    content.add(name, 1, 0);
                }
                setGraphic(content);
            }

            @Override
            public void cancelEdit() {
                if (!isEditing) {
                    return;
                }
                super.cancelEdit();
                isEditing = false;
                System.out.println("Canceling edit for: " + getItem());
                content.getChildren().remove(nameField);
                if (!content.getChildren().contains(name)) {
                    content.add(name, 1, 0);
                }
                setGraphic(content);
            }
        });

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

                    ImageView driveIcon = new ImageView();
                    driveIcon.getStyleClass().add("drive-icon");
                    driveIcon.setFitHeight(16);
                    driveIcon.setPreserveRatio(true);
                    Image driveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/hdd_64.png")));
                    if (driveImage.isError()) {
                        System.err.println("Failed to load hdd_64.png");
                    }
                    driveIcon.setImage(driveImage);

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
}