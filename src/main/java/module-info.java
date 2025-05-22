module shop.fx.file_manager {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires atlantafx.base;
    requires BorderlessSceneFX;
    requires java.desktop;

    opens shop.fx.file_manager to javafx.fxml;
    exports shop.fx.file_manager;
}