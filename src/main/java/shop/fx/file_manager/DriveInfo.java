package shop.fx.file_manager;

import java.nio.file.Path;

public class DriveInfo {
    private final Path path;
    private final String displayName;

    public DriveInfo(Path path, String displayName) {
        this.path = path;
        this.displayName = displayName;
    }

    public Path getPath() {
        return path;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}