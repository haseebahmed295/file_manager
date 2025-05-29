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
class DriveData {
    private final DriveInfo driveInfo;
    private final String totalSize;
    private final String freeSize;

    public DriveData(DriveInfo driveInfo, String totalSize, String freeSize) {
        this.driveInfo = driveInfo;
        this.totalSize = totalSize;
        this.freeSize = freeSize;
    }

    public DriveInfo getDriveInfo() {
        return driveInfo;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public String getFreeSize() {
        return freeSize;
    }
}