package shop.fx.file_manager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.nio.file.Path;
import java.util.Objects;

public class FileManagerUI {

    private final ListView<Path> fileListView;
    private final ListView<DriveInfo> driveListView;
    private final Button homeButton;
    private final Button backButton;
    private final Button forwardButton;
    private final Button minimizeButton;
    private final Button maximizeButton;
    private final Button closeButton;
    private final BorderPane root;
    private final VBox header;
    private final TextField pathField;
    private final TextField searchField;
    private final VBox mainContent;

    public FileManagerUI() {
        // Initialize UI components
        fileListView = new ListView<>();
        fileListView.getStyleClass().add("file-list-view");

        driveListView = new ListView<>();
        driveListView.getStyleClass().add("drive-list-view");


        // Load header logo
        Image headerLogoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/logo_64.png")));
        if (headerLogoImage.isError()) {
            System.err.println("Failed to load header_logo.png");
        }
        ImageView headerLogoView = new ImageView(headerLogoImage);
        headerLogoView.getStyleClass().add("logo");
        headerLogoView.setFitHeight(24);
        headerLogoView.setPreserveRatio(true);

        // Navigation buttons
        homeButton = new Button();
        Image homeButtonLogoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/home_64.png")));
        if (homeButtonLogoImage.isError()) {
            System.err.println("Failed to load home_button_logo.png");
        }
        ImageView homeButtonLogo = new ImageView(homeButtonLogoImage);
        homeButtonLogo.getStyleClass().add("nav-button-logo");
        homeButtonLogo.setFitHeight(16);
        homeButtonLogo.setPreserveRatio(true);
        homeButton.setGraphic(homeButtonLogo);
        homeButton.getStyleClass().add("nav-button");
        homeButton.getStyleClass().add("house-button");

        backButton = new Button();
        Image backButtonLogoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/back.png")));
        if (backButtonLogoImage.isError()) {
            System.err.println("Failed to load back_button_logo.png");
        }
        ImageView backButtonLogo = new ImageView(backButtonLogoImage);
        backButtonLogo.getStyleClass().add("nav-button-logo");
        backButtonLogo.setFitHeight(16);
        backButtonLogo.setPreserveRatio(true);
        backButton.setGraphic(backButtonLogo);
        backButton.getStyleClass().add("nav-button");
        backButton.getStyleClass().add("back-button");

        forwardButton = new Button();
        Image forwardButtonLogoImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/forward_64.png")));
        if (forwardButtonLogoImage.isError()) {
            System.err.println("Failed to load forward_button_logo.png");
        }
        ImageView forwardButtonLogo = new ImageView(forwardButtonLogoImage);
        forwardButtonLogo.getStyleClass().add("nav-button-logo");
        forwardButtonLogo.setFitHeight(16);
        forwardButtonLogo.setPreserveRatio(true);
        forwardButton.setGraphic(forwardButtonLogo);
        forwardButton.getStyleClass().add("nav-button");
        forwardButton.getStyleClass().add("forward-button");

        pathField = new TextField();
        pathField.getStyleClass().add("path-field");
        pathField.setEditable(false);
        pathField.setPromptText("Home");

        searchField = new TextField();
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText("Search...");

        Label drivesLabel = new Label("Drives");
        drivesLabel.getStyleClass().add("drives-label");

        // Window control buttons with custom icons
        minimizeButton = new Button();
        Image minimizeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/minus_64.png")));
        if (minimizeImage.isError()) {
            System.err.println("Failed to load minimize_16.png");
        }
        ImageView minimizeIcon = new ImageView(minimizeImage);
        minimizeIcon.setFitHeight(16);
        minimizeIcon.setPreserveRatio(true);
        minimizeButton.setGraphic(minimizeIcon);
        minimizeButton.getStyleClass().addAll("window-button", "minimize-button");

        maximizeButton = new Button();
        Image maximizeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/maximize_64.png")));
        if (maximizeImage.isError()) {
            System.err.println("Failed to load maximize_16.png");
        }
        ImageView maximizeIcon = new ImageView(maximizeImage);
        maximizeIcon.setFitHeight(16);
        maximizeIcon.setPreserveRatio(true);
        maximizeButton.setGraphic(maximizeIcon);
        maximizeButton.getStyleClass().addAll("window-button", "maximize-button");

        closeButton = new Button();
        Image closeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("icons/close_64.png")));
        if (closeImage.isError()) {
            System.err.println("Failed to load close_16.png");
        }
        ImageView closeIcon = new ImageView(closeImage);
        closeIcon.setFitHeight(16);
        closeIcon.setPreserveRatio(true);
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().addAll("window-button", "close-button");

        // Button container for window controls
        HBox buttonContainer = new HBox(0, minimizeButton, maximizeButton, closeButton);
        buttonContainer.getStyleClass().add("button-container");
        buttonContainer.setAlignment(Pos.TOP_RIGHT);

        // Draggable bar at the top
        HBox dragBar = new HBox();
        dragBar.getStyleClass().add("drag-bar");
        dragBar.setMinHeight(10);
        dragBar.setMaxSize(Double.MAX_VALUE, 10);

        // Sidebar layout
        VBox sidebar = new VBox(10, headerLogoView, searchField, drivesLabel, driveListView);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setMinWidth(200);
        sidebar.setPrefWidth(200);
        sidebar.setMaxWidth(600);

        // Header layout with two rows
        HBox topRow = new HBox(dragBar, new Region(), buttonContainer);
        topRow.getStyleClass().add("header-row");
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setMinHeight(30);
        HBox.setHgrow(dragBar, Priority.ALWAYS);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

        // Group back and forward buttons in an HBox with no spacing
        HBox navGroup = new HBox(0, backButton, forwardButton);
        navGroup.setAlignment(Pos.CENTER);

        // Second row with home button, navigation group, and path field
        HBox bottomRow = new HBox(10, homeButton, navGroup, pathField, new Region());
        bottomRow.getStyleClass().add("header-row");
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.setMinHeight(30);
        bottomRow.setPadding(new Insets(0, 10, 0, 0));
        HBox.setHgrow(pathField, Priority.ALWAYS);
        HBox.setMargin(homeButton, new Insets(0, 0, 0, 20));
        HBox.setMargin(navGroup, new Insets(0));

        header = new VBox(topRow, bottomRow);
        header.getStyleClass().add("header");

        // Main content layout
        mainContent = new VBox(header, fileListView);
        mainContent.getStyleClass().add("main-content");
        VBox.setVgrow(fileListView, Priority.ALWAYS);

        // Split pane for sidebar and main content
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(sidebar, mainContent);
        splitPane.setDividerPositions(0.25);
        splitPane.getStyleClass().add("root-pane");
        // Lock the divider position to prevent sidebar resizing
        splitPane.getDividers().getFirst().positionProperty().addListener((_, _, _) -> splitPane.setDividerPositions(200.0 / splitPane.getWidth()));

        // Root layout with split pane in center
        root = new BorderPane();
        root.getStyleClass().add("root");
        root.setCenter(splitPane);
    }

    public VBox getHeader() {
        return header;
    }

    public ListView<Path> getFileListView() {
        return fileListView;
    }

    public ListView<DriveInfo> getDriveListView() {
        return driveListView;
    }

    public Button getHomeButton() {
        return homeButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public Button getForwardButton() {
        return forwardButton;
    }

    public Button getMinimizeButton() {
        return minimizeButton;
    }

    public Button getMaximizeButton() {
        return maximizeButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public BorderPane getRoot() {
        return root;
    }

    public TextField getPathField() {
        return pathField;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public VBox getMainContent() {
        return mainContent;
    }
}