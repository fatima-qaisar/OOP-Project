import java.io.File;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

    private TextArea editor;
    private Label currentFileLabel;
    private VersionManager vm;
    private File currentSourceFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage stage) {
        // Login controls
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginBtn = new Button("Login");

        VBox loginRoot = new VBox(10, userLabel, userField, passLabel, passField, loginBtn);
        loginRoot.setPadding(new Insets(20));
        Scene loginScene = new Scene(loginRoot, 300, 200);

        stage.setScene(loginScene);
        stage.setTitle("Login");
        stage.show();

        // Login action
        loginBtn.setOnAction(e -> {
            String user = userField.getText();
            String pass = passField.getText();

            if (user.equals("admin") && pass.equals("1234")) {
                stage.close();
                showEditorStage();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid credentials!");
                alert.showAndWait();
            }
        });
    }

    private void showEditorStage() {
        Stage editorStage = new Stage();
        this.vm = new VersionManager();
        editorStage.setTitle("Document Version Control System (DVCS)");

        // Buttons
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

        this.editor = new TextArea();
        this.editor.setWrapText(true);
        this.editor.setPrefHeight(450.0);

        this.currentFileLabel = new Label("No file added.");

        HBox buttonsBox = new HBox(10, addFileBtn, editBtn, saveBtn, historyBtn, exitBtn);
        buttonsBox.setPadding(new Insets(10));

        VBox root = new VBox(10, buttonsBox, this.currentFileLabel, this.editor);
        root.setPadding(new Insets(12));

        Scene scene = new Scene(root, 900, 600);
        editorStage.setScene(scene);
        editorStage.show();
    }

    private void onAddFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a document to add");
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                this.vm.addFile(selectedFile);
                this.currentSourceFile = selectedFile;
                this.currentFileLabel.setText("Current: " + selectedFile.getName());
                String content = this.vm.getLatestVersionContent(selectedFile.getName());
                this.editor.setText(content);
                showAlert("File added and saved as version 1.");
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
                this.vm.saveNewVersion(this.currentSourceFile.getName(), content);
                showAlert("Changes saved as a new version.");
            } catch (Exception ex) {
                showAlert("Error saving changes: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void onHistory(Stage stage) {
        if (this.currentSourceFile == null) {
            showAlert("No file selected. Add a file first.");
            return;
        }

        Stage historyStage = new Stage();
        historyStage.initOwner(stage);
        historyStage.setTitle("Version History - " + this.currentSourceFile.getName());

        ListView<String> listView = new ListView<>();
        try {
            listView.getItems().addAll(this.vm.listVersions(this.currentSourceFile.getName()));
        } catch (Exception ex) {
            showAlert("Error listing versions: " + ex.getMessage());
        }

        Button openBtn = new Button("Open Selected");
        Button revertBtn = new Button("Revert to Selected");
        Button compareBtn = new Button("Show Changes vs Previous");
        Button closeBtn = new Button("Close");

        TextFlow diffFlow = new TextFlow();
        diffFlow.setPrefHeight(300);

        openBtn.setOnAction(e -> {
            String selectedVersion = listView.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) {
                showAlert("Select a version to open.");
                return;
            }
            try {
                String content = this.vm.getVersionContent(this.currentSourceFile.getName(), selectedVersion);
                this.editor.setText(content);
                showAlert("Loaded version: " + selectedVersion);
            } catch (Exception ex) {
                showAlert("Error opening version: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        revertBtn.setOnAction(e -> {
            String selectedVersion = listView.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) {
                showAlert("Select a version to revert.");
                return;
            }
            try {
                this.vm.revertToVersion(this.currentSourceFile.getName(), selectedVersion);
                String latest = this.vm.getLatestVersionContent(this.currentSourceFile.getName());
                this.editor.setText(latest);
                listView.getItems().setAll(this.vm.listVersions(this.currentSourceFile.getName()));
                showAlert("Reverted. A new version was created as the current version.");
            } catch (Exception ex) {
                showAlert("Error reverting: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        compareBtn.setOnAction(e -> {
            String selectedVersion = listView.getSelectionModel().getSelectedItem();
            if (selectedVersion == null) {
                showAlert("Select a version to compare.");
                return;
            }
            try {
                String previous = this.vm.getPreviousVersionName(this.currentSourceFile.getName(), selectedVersion);
                if (previous == null) {
                    showAlert("No previous version to compare with.");
                    return;
                }
                String prevContent = this.vm.getVersionContent(this.currentSourceFile.getName(), previous);
                String currContent = this.vm.getVersionContent(this.currentSourceFile.getName(), selectedVersion);
                diffFlow.getChildren().clear();
                diffFlow.getChildren().addAll(DiffUtil.generateTextNodes(prevContent, currContent));
            } catch (Exception ex) {
                showAlert("Error generating diff: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        closeBtn.setOnAction(e -> historyStage.close());

        HBox buttonsBox = new HBox(10, openBtn, revertBtn, compareBtn, closeBtn);
        buttonsBox.setPadding(new Insets(8));

        VBox historyRoot = new VBox(8, listView, buttonsBox, new Label("Changes (green = added, red = deleted):"), diffFlow);
        historyRoot.setPadding(new Insets(10));

        Scene scene = new Scene(historyRoot, 800, 600);
        historyStage.setScene(scene);
        historyStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
