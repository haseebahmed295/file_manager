package shop.fx.file_manager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

public class FileManagerController {

    private final FileManagerUI ui;
    private final FileSystemUtils fileSystemUtils;
    private Path currentPath;
    private final List<Path> navigationHistory;
    private int historyIndex;
    private final SimpleObjectProperty<Path> copiedPathProperty;
    private Path cutPath;
    private final Image maximizeImage;
    private final Image restoreImage;
    private final List<Path> pinnedFolders;
    private static final String PREFS_NODE = "shop.fx.file_manager.pinned_folders";
    private static final Preferences prefs = Preferences.userNodeForPackage(FileManagerController.class);

    public FileManagerController(FileManagerUI ui, FileSystemUtils fileSystemUtils) {
        this.ui = ui;
        this.fileSystemUtils = fileSystemUtils;
        this.navigationHistory = new ArrayList<>();
        this.historyIndex = -1;
        this.copiedPathProperty = new SimpleObjectProperty<>(null);
        this.cutPath = null;
        this.pinnedFolders = new ArrayList<>();
        maximizeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/maximize_64.png")));
        restoreImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/restore_64.png")));
        if (maximizeImage.isError() || restoreImage.isError()) {
            System.err.println("Failed to load maximize_64.png or restore_64.png");
        }
        initialize();
//         prefs.remove(PREFS_NODE);
        loadPinnedFolders();
    }

    private void initialize() {
        CellFactorySetup cellFactorySetup = new CellFactorySetup(ui, fileSystemUtils, this);
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
        cutPath = null;
        setCopiedPath(null);
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

    public void loadPinnedFolders() {
        pinnedFolders.clear();
        ui.getPinnedFoldersListView().getItems().clear();
        String pinnedPaths = prefs.get(PREFS_NODE, "");
        System.out.println("Raw preferences content: " + pinnedPaths);
        if (pinnedPaths != null && !pinnedPaths.trim().isEmpty()) {
            String[] paths = pinnedPaths.split(";");
            for (String path : paths) {
                if (path == null || path.trim().isEmpty()) {
                    System.err.println("Skipping empty or null path in preferences");
                    continue;
                }
                try {
                    Path folder = Paths.get(path);
                    if (Files.exists(folder) && Files.isDirectory(folder) && Files.isReadable(folder)) {
                        if (!pinnedFolders.contains(folder)) {
                            pinnedFolders.add(folder);
                            ui.getPinnedFoldersListView().getItems().add(folder);
                            System.out.println("Loaded pinned folder: " + folder);
                        }
                    } else {
                        System.err.println("Skipping invalid pinned item: " + path + " (not a directory, does not exist, or not readable)");
                    }
                } catch (Exception e) {
                    System.err.println("Error loading pinned folder: " + path + ", Message: " + e.getMessage());
                }
            }
        }
        ui.getPinnedFoldersListView().refresh();
        savePinnedFolders();
    }

    public void pinFolder(Path folder) {
        try {
            if (folder == null) {
                System.err.println("Cannot pin folder: Path is null");
                return;
            }
            if (!Files.exists(folder) || !Files.isDirectory(folder) || !Files.isReadable(folder)) {
                System.err.println("Cannot pin: " + folder + " (does not exist, not a directory, or not readable)");
                return;
            }
            if (!pinnedFolders.contains(folder)) {
                pinnedFolders.add(folder);
                ui.getPinnedFoldersListView().getItems().add(folder);
                ui.getPinnedFoldersListView().refresh();
                savePinnedFolders();
                System.out.println("Pinned folder: " + folder);

            } else {
                System.err.println("Cannot pin folder: " + folder + " (already pinned)");
            }
        } catch (Exception e) {
            System.err.println("Error pinning folder: " + folder + ", Message: " + e.getMessage());
        }
    }

    public void unpinFolder(Path folder) {
        try {
            if (folder == null) {
                System.err.println("Cannot unpin folder: Path is null");
                return;
            }
            if (pinnedFolders.remove(folder)) {
                ui.getPinnedFoldersListView().getItems().remove(folder);
                ui.getPinnedFoldersListView().refresh();
                savePinnedFolders();
                System.out.println("Unpinned folder: " + folder);
            } else {
                System.err.println("Cannot unpin folder: " + folder + " (not found in pinned list)");
            }
        } catch (Exception e) {
            System.err.println("Error unpinning folder: " + folder + ", Message: " + e.getMessage());
        }
    }

    public boolean isFolderPinned(Path folder) {
        return folder != null && pinnedFolders.contains(folder);
    }





    private void savePinnedFolders() {
        StringBuilder pinnedPaths = new StringBuilder();
        for (Path folder : pinnedFolders) {
            if (folder != null && Files.exists(folder) && Files.isDirectory(folder)) {
                if (pinnedPaths.length() > 0) {
                    pinnedPaths.append(";");
                }
                pinnedPaths.append(folder.toString());
            }
        }
        prefs.put(PREFS_NODE, pinnedPaths.toString());
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Error saving pinned folders: " + e.getMessage());
        }
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
        return copiedPathProperty.get();
    }

    public void setCopiedPath(Path copiedPath) {
        this.copiedPathProperty.set(copiedPath);
    }

    public SimpleObjectProperty<Path> copiedPathProperty() {
        return copiedPathProperty;
    }

    public Path getCutPath() {
        return cutPath;
    }

    public void setCutPath(Path cutPath) {
        this.cutPath = cutPath;
    }

    public Image getMaximizeImage() {
        return maximizeImage;
    }

    public Image getRestoreImage() {
        return restoreImage;
    }
}