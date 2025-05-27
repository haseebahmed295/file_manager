package shop.fx.file_manager;

import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileSystemUtils {

    public void loadDrives(ListView<DriveInfo> driveListView) {
        FileSystem fileSystem = FileSystems.getDefault();
        for (Path root : fileSystem.getRootDirectories()) {
            try {
                if (Files.exists(root) && Files.isReadable(root)) {
                    String displayName = getDriveName(root);
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        System.out.println("Loaded drive: " + displayName);
                        driveListView.getItems().add(new DriveInfo(root, displayName));
                    } else {
                        System.err.println("Skipping drive with empty name: " + root);
                    }
                }
            } catch (Exception e) {
                System.err.println("Cannot access drive: " + root + ", error: " + e.getMessage());
            }
        }
    }

    public void loadDrivesInGrid(VBox mainContent) {
        GridPane driveGrid = new GridPane();
        driveGrid.getStyleClass().add("drive-grid");
        driveGrid.setHgap(10);
        driveGrid.setVgap(10);
        driveGrid.setPadding(new javafx.geometry.Insets(10));

        FileSystem fileSystem = FileSystems.getDefault();
        List<DriveInfo> drives = new ArrayList<>();
        for (Path root : fileSystem.getRootDirectories()) {
            try {
                if (Files.exists(root) && Files.isReadable(root)) {
                    String displayName = getDriveName(root);
                    if (displayName != null && !displayName.trim().isEmpty()) {
                        System.out.println("Loaded drive for grid: " + displayName);
                        drives.add(new DriveInfo(root, displayName));
                    } else {
                        System.err.println("Skipping drive with empty name for grid: " + root);
                    }
                }
            } catch (Exception e) {
                System.err.println("Cannot access drive for grid: " + root + ", error: " + e.getMessage());
            }
        }

        int colCount = 2; // Two columns
        for (int i = 0; i < drives.size(); i++) {
            DriveInfo drive = drives.get(i);
            GridPane cellContent = new GridPane();
            cellContent.getStyleClass().add("drive-cell-content");
            cellContent.setUserData(drive);

            // Drive icon
            ImageView driveIcon = new ImageView();
            driveIcon.getStyleClass().add("drive-icon");
            driveIcon.setFitHeight(16);
            driveIcon.setPreserveRatio(true);
            Image driveImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/hdd_64.png")));
            if (driveImage.isError()) {
                System.err.println("Failed to load hdd_64.png");
            }
            driveIcon.setImage(driveImage);

            // Vertical arrangement of drive name, sizes, and progress bar
            VBox textAndProgress = new VBox(8);
            Label driveName = new Label(drive.getDisplayName());
            driveName.getStyleClass().add("drive-name");

            // Total and free space labels
            Label totalSizeLabel = new Label();
            totalSizeLabel.getStyleClass().add("drive-total-size");

            ProgressBar progressBar = new ProgressBar();
            progressBar.getStyleClass().add("drive-progress");
            progressBar.setMaxWidth(160);

            try {
                FileStore store = Files.getFileStore(drive.getPath());
                long totalSpace = store.getTotalSpace();
                long freeSpace = store.getUsableSpace();
                long usedSpace = totalSpace - freeSpace;
                double progress = totalSpace > 0 ? (double) usedSpace / totalSpace : 0;
                progressBar.setProgress(progress);
                totalSizeLabel.setText("Total: " + formatSize(totalSpace)+" | Free: "+formatSize(freeSpace));
            } catch (IOException e) {
                System.err.println("Error calculating progress or sizes for drive: " + drive.getDisplayName());
                progressBar.setProgress(0);
                totalSizeLabel.setText("Total: (Error)");
            }

            textAndProgress.getChildren().addAll(driveName,progressBar, totalSizeLabel);

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

            VBox cell = new VBox(cellContent);
            cell.getStyleClass().add("drive-cell");
            driveGrid.add(cell, i % colCount, i / colCount);
        }

        // Replace the second child (fileListView) with the driveGrid
        mainContent.getChildren().set(1, driveGrid);
        VBox.setVgrow(driveGrid, Priority.ALWAYS);
    }

    public void loadDirectory(Path directory, ListView<Path> fileListView) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                fileListView.getItems().add(path);
            }
        } catch (AccessDeniedException e) {
            System.err.println("Access denied to: " + directory);
            throw e;
        }
    }

    public void searchDirectory(Path directory, String query, ListView<Path> fileListView) throws IOException {
        fileListView.getItems().clear();
        if (query.isEmpty()) {
            loadDirectory(directory, fileListView);
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, path ->
                path.getFileName().toString().toLowerCase().contains(query.toLowerCase()))) {
            for (Path path : stream) {
                fileListView.getItems().add(path);
            }
        } catch (AccessDeniedException e) {
            System.err.println("Access denied to: " + directory);
            throw e;
        }
    }

    public String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String getDriveName(Path root) {
        try {
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File drive = root.toFile();
            String name = fsv.getSystemDisplayName(drive);
            if (name == null || name.trim().isEmpty()) {
                name = root.toString();
            }
            String formattedName = name;
            return formattedName;
        } catch (Exception e) {
            System.err.println("Error getting name for drive: " + root + ", error: " + e.getMessage());
            return "Drive (" + root.toString() + ")";
        }
    }
}