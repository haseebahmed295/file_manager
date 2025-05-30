package shop.fx.file_manager;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileOperations {

    private final FileManagerUI ui;
    private final FileSystemUtils fileSystemUtils;
    private final FileManagerController controller;
    private boolean isCutOperation;

    public FileOperations(FileManagerUI ui, FileSystemUtils fileSystemUtils, FileManagerController controller) {
        this.ui = ui;
        this.fileSystemUtils = fileSystemUtils;
        this.controller = controller;
        this.isCutOperation = false;
    }

    public void openItem(Path path) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            System.err.println("Desktop API not supported on this platform");
            controller.showErrorDialog("Error", "Opening items is not supported on this platform.");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        File file = path.toFile();
        if (!file.exists()) {
            System.err.println("Item does not exist: " + path);
            controller.showErrorDialog("Error", "Item does not exist: " + path.toString());
            return;
        }

        try {
            desktop.open(file);
            System.out.println("Opened item: " + path);
        } catch (IOException e) {
            System.err.println("Error opening item: " + path + ", Message: " + e.getMessage());
            throw e;
        }
    }

    public void handleRename(Path selectedPath, int selectedIndex) {
        if (selectedPath == null || selectedIndex < 0) {
            System.out.println("Rename aborted: Invalid path or index");
            controller.showErrorDialog("Error", "No file or folder selected.");
            return;
        }
        ui.getFileListView().scrollTo(selectedIndex);
        Platform.runLater(() -> {
            ListCell<Path> cell = getCellAtIndex(ui.getFileListView(), selectedIndex);
            if (cell != null) {
                System.out.println("Starting edit for cell at index: " + selectedIndex);
                cell.startEdit();
            } else {
                System.out.println("Failed to find cell at index: " + selectedIndex);
                controller.showErrorDialog("Error", "Cannot edit: Item is not visible.");
            }
        });
    }

    private ListCell<Path> getCellAtIndex(ListView<Path> listView, int index) {
        for (Node node : listView.lookupAll(".list-cell")) {
            if (node instanceof ListCell) {
                ListCell<?> cell = (ListCell<?>) node;
                if (cell.getIndex() == index && !cell.isEmpty()) {
                    return (ListCell<Path>) cell;
                }
            }
        }
        return null;
    }

    public void handleDelete() {
        Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedPath == null) {
            System.out.println("Delete failed: No item selected");
            controller.showErrorDialog("Error", "No file or folder selected.");
            return;
        }
        try {
            System.out.println("Deleting: " + selectedPath);
            if (Files.isDirectory(selectedPath)) {
                Files.walk(selectedPath)
                        .sorted((p1, p2) -> -p1.compareTo(p2))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            } else {
                Files.delete(selectedPath);
            }
            if (selectedPath.equals(controller.getCutPath())) {
                controller.setCutPath(null);
            }
            if (controller.isFolderPinned(selectedPath)) {
                controller.unpinFolder(selectedPath);
            }
            controller.loadDirectory(controller.getCurrentPath());
        } catch (IOException e) {
            System.err.println("Error deleting: " + e.getMessage());
            controller.showErrorDialog("Error", "Error deleting file or folder: " + e.getMessage());
        }
    }

    public void handleCopy() {
        Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedPath == null) {
            System.out.println("Copy failed: No item selected");
            controller.showErrorDialog("Error", "No file or folder selected.");
            return;
        }
        controller.setCopiedPath(selectedPath);
        controller.setCutPath(null);
        isCutOperation = false;
        System.out.println("Copied path: " + selectedPath);
    }

    public void handleCut() {
        Path selectedPath = ui.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedPath == null) {
            System.out.println("Cut failed: No item selected");
            controller.showErrorDialog("Error", "No file or folder selected.");
            return;
        }
        controller.setCopiedPath(selectedPath);
        controller.setCutPath(selectedPath);
        isCutOperation = true;
        System.out.println("Cut path: " + selectedPath);
        ui.getFileListView().refresh();
    }

    public void handlePaste() {
        Path copiedPath = controller.getCopiedPath();
        if (copiedPath == null) {
            System.out.println("Paste failed: No item copied or cut");
            controller.showErrorDialog("Error", "No file or folder copied or cut.");
            return;
        }
        if (controller.getCurrentPath() == null) {
            System.out.println("Paste failed: No destination directory");
            controller.showErrorDialog("Error", "No destination directory selected.");
            return;
        }
        try {
            System.out.println((isCutOperation ? "Moving: " : "Copying: ") + copiedPath + " to " + controller.getCurrentPath());
            Path targetPath = controller.getCurrentPath().resolve(copiedPath.getFileName());
            if (Files.isDirectory(copiedPath)) {
                copyDirectory(copiedPath, targetPath);
            } else {
                if (isCutOperation) {
                    Files.move(copiedPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.copy(copiedPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            if (isCutOperation) {
                controller.setCopiedPath(null);
                controller.setCutPath(null);
                isCutOperation = false;
            }
            controller.loadDirectory(controller.getCurrentPath());
        } catch (IOException e) {
            System.err.println("Error pasting: " + e.getMessage());
            controller.showErrorDialog("Error", "Error pasting file or folder: " + e.getMessage());
        }
    }

    public void handleNewFolder() {
        if (controller.getCurrentPath() == null) {
            System.out.println("New folder creation failed: No directory selected");
            controller.showErrorDialog("Error", "No directory selected for creating a new folder.");
            return;
        }
        try {
            String baseName = "New Folder";
            Path newFolderPath = controller.getCurrentPath().resolve(baseName);
            int counter = 1;
            while (Files.exists(newFolderPath)) {
                newFolderPath = controller.getCurrentPath().resolve(baseName + " (" + counter + ")");
                counter++;
            }
            Files.createDirectory(newFolderPath);
            System.out.println("Created new folder: " + newFolderPath);
            controller.loadDirectory(controller.getCurrentPath());
            // Select and rename the new folder
            int newFolderIndex = ui.getFileListView().getItems().indexOf(newFolderPath);
            if (newFolderIndex >= 0) {
                ui.getFileListView().getSelectionModel().select(newFolderIndex);
                ui.getFileListView().scrollTo(newFolderIndex);
                Platform.runLater(() -> {
                    ListCell<Path> cell = getCellAtIndex(ui.getFileListView(), newFolderIndex);
                    if (cell != null) {
                        System.out.println("Starting rename for new folder at index: " + newFolderIndex);
                        cell.startEdit();
                    } else {
                        System.out.println("Failed to find cell for new folder at index: " + newFolderIndex);
                        controller.showErrorDialog("Error", "Cannot rename: New folder is not visible.");
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Error creating new folder: " + e.getMessage());
            controller.showErrorDialog("Error", "Error creating new folder: " + e.getMessage());
        }
    }

    public void handlePinFolder(Path folder) {
        if (Files.isDirectory(folder)) {
            controller.pinFolder(folder);
        } else {
            controller.showErrorDialog("Error", "Selected item is not a folder.");
        }
    }

    public void handleUnpinFolder(Path folder) {
        controller.unpinFolder(folder);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(s -> {
            try {
                Path t = target.resolve(source.relativize(s));
                if (Files.isDirectory(s)) {
                    Files.createDirectories(t);
                } else {
                    Files.copy(s, t, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}