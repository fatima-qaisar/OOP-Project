import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private TextArea editor;
    private Label currentFileLabel;
    private VersionManager vm;
    private File currentSourceFile;
    private String loggedInUser;
    private UserManager userManager = new UserManager();
    private TreeView<String> fileExplorer;
    private TreeItem<String> rootItem;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    // ------------------ LOGIN SCREEN ------------------
    private void showLoginScreen(Stage stage) {
        Text softwareName = new Text("DocuVault");
        softwareName.setFont(Font.font("Arial", 24));
        softwareName.setStyle("-fx-font-weight: bold; -fx-fill: #ffffff;");

        Text tagline = new Text("Secure Document Version Control");
        tagline.setFont(Font.font(14));
        tagline.setStyle("-fx-fill: #ffffff;");

        VBox titleBox = new VBox(5, softwareName, tagline);
        titleBox.setAlignment(Pos.CENTER);

        Label userLabel = new Label("Username or email:");
        userLabel.setStyle("-fx-text-fill: #ffffff;");
        TextField userField = new TextField();
        userField.setPrefWidth(250);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: #ffffff;");
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(250);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        grid.add(userLabel, 1, 0);
        grid.add(userField, 1, 1);
        grid.add(passLabel, 1, 2);
        grid.add(passField, 1, 3);
        grid.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("Sign In");
        Button signupBtn = new Button("Sign Up");
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: black;");
        signupBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: black;");

        HBox buttonsBox = new HBox(20, loginBtn, signupBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox formBox = new VBox(20, titleBox, grid, buttonsBox);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(30));
        formBox.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        StackPane root = new StackPane(formBox);
        root.setStyle("-fx-background-color: #34495e;");

        Scene loginScene = new Scene(root, 450, 350);
        stage.setScene(loginScene);
        stage.setTitle("DocuVault Login");
        stage.show();

        loginBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (userManager.authenticate(user, pass)) {
                loggedInUser = user;
                stage.close();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Login successful! Welcome " + loggedInUser);
                    alert.showAndWait();
                    showEditorStage();
                });
            } else {
                showAlert("Invalid credentials! Try again or Sign Up.");
            }
        });

        signupBtn.setOnAction(e -> showSignUpStage(stage));
    }

    // ------------------ SIGNUP SCREEN ------------------
    private void showSignUpStage(Stage loginStage) {
        Stage signupStage = new Stage();
        signupStage.setTitle("DocuVault – Sign Up");

        Text title = new Text("Create a new account");
        title.setFont(Font.font("Arial", 18));
        title.setStyle("-fx-fill: #ffffff; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        userLabel.setStyle("-fx-text-fill: #ffffff;");
        TextField userField = new TextField();
        userField.setPrefWidth(250);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-text-fill: #ffffff;");
        PasswordField passField = new PasswordField();
        passField.setPrefWidth(250);

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.setAlignment(Pos.CENTER);

        Button registerBtn = new Button("Register");
        Button cancelBtn = new Button("Cancel");
        registerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonsBox = new HBox(15, registerBtn, cancelBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, title, grid, buttonsBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 10;");

        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: #34495e;");

        Scene scene = new Scene(wrapper, 450, 300);
        signupStage.setScene(scene);
        signupStage.show();

        registerBtn.setOnAction(e -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showAlert("Please enter both username and password.");
                return;
            }

            if (userManager.userExists(user)) {
                showAlert("Username already exists! Try another.");
                return;
            }

            userManager.registerUser(user, pass);
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "Account created successfully! You can now login.");
            alert.showAndWait();

            signupStage.close();
            loginStage.toFront();
        });

        cancelBtn.setOnAction(e -> signupStage.close());
    }

    // ------------------ EDITOR STAGE ------------------
    private void showEditorStage() {
        Stage editorStage = new Stage();
        this.vm = new VersionManager(loggedInUser);
        editorStage.setTitle("DocuVault - Document Version Control (" + loggedInUser + ")");

        this.editor = new TextArea();
        this.editor.setWrapText(true);
        this.editor.setPrefHeight(400);

        this.currentFileLabel = new Label("No file selected.");

        Button addFileBtn = new Button("Add File");
        Button editBtn = new Button("Edit");
        Button saveBtn = new Button("Save Changes");
        Button historyBtn = new Button("History");
        Button exitBtn = new Button("Exit");

        addFileBtn.setOnAction(e -> onAddFile(editorStage));
        editBtn.setOnAction(e -> onEdit());
        saveBtn.setOnAction(e -> onSaveChanges());
        historyBtn.setOnAction(e -> onHistory(editorStage));
        exitBtn.setOnAction(e -> editorStage.close());

        HBox buttonsBox = new HBox(12, addFileBtn, editBtn, saveBtn, historyBtn, exitBtn);
        buttonsBox.setPadding(new Insets(10));

        VBox editorBox = new VBox(10, buttonsBox, this.currentFileLabel, this.editor);
        editorBox.setPadding(new Insets(15));
        editorBox.setPrefWidth(700);

        rootItem = new TreeItem<>("My Documents");
        rootItem.setExpanded(true);

        fileExplorer = new TreeView<>(rootItem);
        fileExplorer.setPrefWidth(200);

        refreshFileExplorer();

        fileExplorer.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && !newSel.getValue().equals("My Documents")) {
                String filename = newSel.getValue().replace("_current.txt", "");
                try {
                    String content = vm.getLatestVersionContent(filename);
                    editor.setText(content);
                    currentSourceFile = vm.getUserDocs().resolve(filename + "_current.txt").toFile();
                    currentFileLabel.setText("Current: " + filename);
                } catch (IOException ex) {
                    showAlert("Error loading file: " + ex.getMessage());
                }
            }
        });

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(fileExplorer, editorBox);
        splitPane.setDividerPositions(0.25);

        Scene scene = new Scene(splitPane, 950, 600);
        editorStage.setScene(scene);
        editorStage.show();
    }

    // ------------------ DVCS METHODS ------------------
    private void refreshFileExplorer() {
        rootItem.getChildren().clear();
        Path userDocsPath = vm.getUserDocs();
        try {
            if (Files.exists(userDocsPath)) {
                Files.list(userDocsPath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> rootItem.getChildren().add(new TreeItem<>(path.getFileName().toString())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onAddFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a document to add");
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                vm.addFile(selectedFile);
                this.currentSourceFile = selectedFile;
                this.currentFileLabel.setText("Current: " + selectedFile.getName());
                String content = vm.getLatestVersionContent(selectedFile.getName());
                this.editor.setText(content);
                showAlert("File added and saved as version 1.");
                refreshFileExplorer();
            } catch (Exception ex) {
                showAlert("Error adding file: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void onEdit() {
        if (this.currentSourceFile == null) {
            showAlert("No file selected. Click 'Add File' first.");
        } else {
            this.editor.requestFocus();
            showAlert("You can now edit the document. When finished click 'Save Changes'.");
        }
    }

    private void onSaveChanges() {
        if (this.currentSourceFile == null) {
            showAlert("No file selected. Add a file first.");
        } else {
            try {
                String content = this.editor.getText();
                String originalName = currentSourceFile.getName().replace("_current.txt", "");
                vm.saveNewVersion(originalName, content);
                showAlert("Changes saved as a new version.");
                refreshFileExplorer();
            } catch (Exception ex) {
                showAlert("Error saving changes: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void onHistory(Stage stage) {
        if (currentSourceFile == null) {
            showAlert("No file selected. Add a file first.");
            return;
        }

        String originalName = currentSourceFile.getName().replace("_current.txt", "");

        List<String> versions = vm.listVersions(originalName);
        if (versions.isEmpty()) {
            showAlert("No versions found for this document.");
            return;
        }

        Stage historyStage = new Stage();
        historyStage.setTitle("Version History - " + originalName);

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(versions);

        Button openBtn = new Button("Open");
        Button revertBtn = new Button("Revert");
        Button compareBtn = new Button("Compare");
        Button closeBtn = new Button("Close");

        TextFlow diffFlow = new TextFlow();
        diffFlow.setPrefHeight(300);

        openBtn.setOnAction(e -> {
            String selectedVersion = listView.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) { showAlert("Select a version to open."); return; }
            try {
                String content = vm.getVersionContent(originalName, selectedVersion);
                editor.setText(content);
            } catch (IOException ex) { showAlert("Error opening version: " + ex.getMessage()); }
        });

        revertBtn.setOnAction(e -> {
            String selectedVersion = listView.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) { showAlert("Select a version to revert."); return; }
            try {
                vm.revertToVersion(originalName, selectedVersion);
                editor.setText(vm.getLatestVersionContent(originalName));
                listView.getItems().setAll(vm.listVersions(originalName));
                refreshFileExplorer();
                showAlert("Reverted to " + selectedVersion + ". New version created.");
            } catch (IOException ex) { showAlert("Error reverting: " + ex.getMessage()); }
        });

        compareBtn.setOnAction(e -> {
            String selectedVersion = listView.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) { showAlert("Select a version to compare."); return; }
            try {
                String prev = vm.getPreviousVersionName(originalName, selectedVersion);
                if (prev == null) { showAlert("No previous version to compare."); return; }

                String prevContent = vm.getVersionContent(originalName, prev);
                String currContent = vm.getVersionContent(originalName, selectedVersion);

                diffFlow.getChildren().clear();
                diffFlow.getChildren().addAll(DiffUtil.generateTextNodes(prevContent, currContent));
            } catch (IOException ex) { showAlert("Error generating diff: " + ex.getMessage()); }
        });

        closeBtn.setOnAction(e -> historyStage.close());

        HBox btnBox = new HBox(10, openBtn, revertBtn, compareBtn, closeBtn);
        VBox historyRoot = new VBox(10, listView, btnBox, new Label("Changes (green=added, red=deleted):"), diffFlow);
        historyRoot.setPadding(new Insets(10));

        Scene scene = new Scene(historyRoot, 800, 600);
        historyStage.setScene(scene);
        historyStage.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
