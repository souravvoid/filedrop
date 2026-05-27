package com.peerlink.ui;

import com.peerlink.logic.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainController {

    // Locked @FXML fields that must remain exactly as per spec
    @FXML private Label dropZoneLabel;
    @FXML private Label inviteCodeLabel;
    @FXML private TextField inviteCodeInput;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    // New @FXML fields for the new design
    @FXML private VBox sendCard;
    @FXML private VBox receiveCard;
    @FXML private StackPane dropZone;
    @FXML private VBox dropZoneDefault;
    @FXML private Label dropZoneIcon;
    @FXML private VBox dropZoneFileInfo;
    @FXML private Label dropZoneFileName;
    @FXML private Label dropZoneFileSize;
    @FXML private Button removeFileBtn;
    @FXML private Button selectFileBtn;
    @FXML private VBox inviteCodeSection;
    @FXML private Button copyCodeBtn;
    @FXML private Button sendBtn;
    @FXML private HBox codeInputContainer;
    @FXML private Button pasteCodeBtn;
    @FXML private VBox receiveProgressSection;
    @FXML private Label receiveFileName;
    @FXML private Label receiveFileSize;
    @FXML private Label receivePercent;
    @FXML private Label receiveSpeed;
    @FXML private Button receiveBtn;
    @FXML private HBox statusPanel;
    @FXML private Label statusDot;
    @FXML private Button cancelBtn;

    // Private state
    private File selectedFile = null;
    private volatile boolean transferActive = false;
    private String currentInviteCode = "";
    private FileSender activeSender;
    private FileReceiver activeReceiver;

    @FXML
    public void initialize() {
        // Wire inviteCodeInput focus listener for code-focused CSS class
        inviteCodeInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                codeInputContainer.getStyleClass().add("code-focused");
            } else {
                codeInputContainer.getStyleClass().remove("code-focused");
            }
        });

        // Wire removeFileBtn action
        removeFileBtn.setOnAction(e -> clearSelectedFile());

        // Initially disable sendBtn
        sendBtn.setDisable(true);
    }

    // LOCKED METHOD — onDragOver
    @FXML
    void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
            // Add hover style to drop zone
            if (!dropZone.getStyleClass().contains("drop-zone-active")) {
                dropZone.getStyleClass().add("drop-zone-hover");
            }
        }
        e.consume();
    }

    // LOCKED METHOD — onDragDropped
    @FXML
    void onDragDropped(DragEvent e) {
        var files = e.getDragboard().getFiles();
        if (!files.isEmpty()) {
            setSelectedFile(files.get(0));
        }
        dropZone.getStyleClass().remove("drop-zone-hover");
        e.consume();
    }

    // LOCKED METHOD — onSelectFile
    @FXML
    void onSelectFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select file to share");
        File f = chooser.showOpenDialog(selectFileBtn.getScene().getWindow());
        if (f != null) {
            setSelectedFile(f);
        }
    }

    // LOCKED METHOD — onSend
    @FXML
    void onSend() {
        if (selectedFile == null) {
            setStatusWithDot("Please select a file first.", "idle");
            return;
        }

        setSendWaitingState();

        Thread.ofVirtual().start(() -> {
            try {
                int port = PortUtils.findFreePort();
                String host = NetworkUtils.getLocalIPAddress();
                currentInviteCode = InviteCode.encode(host, port);

                Platform.runLater(() -> {
                    showInviteCode(currentInviteCode);
                    setStatusWithDot("Waiting for connection...", "send");
                });

                activeSender = new FileSender(
                    selectedFile,
                    port,
                    stats -> Platform.runLater(() -> {
                        progressBar.setProgress(stats.progress);
                        updateSendProgress(stats.progress);
                    }),
                    msg -> Platform.runLater(() -> {
                        setStatusWithDot(msg, "send");
                        if (msg.contains("Error")) {
                            resetSendCard();
                        }
                        if (msg.contains("complete") || msg.contains("successfully")) {
                            showSendComplete();
                        }
                    }),
                    this::askApproval
                );

                activeSender.startAndWait();
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setStatusWithDot("Error: " + ex.getMessage(), "error");
                    resetSendCard();
                });
            } finally {
                activeSender = null;
            }
        });
    }

    // LOCKED METHOD — onReceive
    @FXML
    void onReceive() {
        String code = inviteCodeInput.getText().trim();
        if (code.isEmpty()) {
            setStatusWithDot("Please enter an invite code.", "idle");
            shakeNode(codeInputContainer);
            return;
        }

        setReceiveActiveState();

        Thread.ofVirtual().start(() -> {
            try {
                String[] parts = InviteCode.decode(code);
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);

                File saveDir = new File(System.getProperty("user.home"), "Downloads");
                saveDir.mkdirs();

                activeReceiver = new FileReceiver(
                    host,
                    port,
                    saveDir,
                    stats -> Platform.runLater(() -> {
                        progressBar.setProgress(stats.progress);
                        updateReceiveProgress(stats.progress);
                    }),
                    msg -> Platform.runLater(() -> {
                        setStatusWithDot(msg, "receive");
                        if (msg.contains("Error")) {
                            resetReceiveCard();
                        }
                        if (msg.contains("complete") || msg.contains("successfully")) {
                            showReceiveComplete();
                        }
                    })
                );

                activeReceiver.receive();
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setStatusWithDot("Error: " + ex.getMessage(), "error");
                    resetReceiveCard();
                });
            } finally {
                activeReceiver = null;
            }
        });
    }

    @FXML
    void onCopyCode() {
        if (currentInviteCode.isEmpty()) return;

        // Copy to system clipboard
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(currentInviteCode);
        clipboard.setContent(content);

        // Show copied feedback
        copyCodeBtn.setText("✓");
        copyCodeBtn.getStyleClass().add("copy-success");

        // Revert after 2 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            copyCodeBtn.setText("⎘");
            copyCodeBtn.getStyleClass().remove("copy-success");
        });
        pause.play();
    }

    @FXML
    void onPasteCode() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            inviteCodeInput.setText(clipboard.getString().trim());
            inviteCodeInput.positionCaret(inviteCodeInput.getText().length());
        }
    }

    @FXML
    void onCancel() {
        if (activeSender != null) activeSender.cancel();
        if (activeReceiver != null) activeReceiver.cancel();
        resetSendCard();
        resetReceiveCard();
        setStatusWithDot("Transfer cancelled.", "idle");
        showCancelBtn(false);
    }

    @FXML
    void onOpenFolder() {
        File dir = new File(System.getProperty("user.home"), "Downloads");
        try {
            Desktop.getDesktop().open(dir);
        } catch (IOException ex) {
            setStatusWithDot("Error opening folder.", "error");
        }
    }

    // Private helper methods

    private void setSelectedFile(File f) {
        selectedFile = f;

        // Show file info in drop zone
        dropZoneDefault.setVisible(false);
        dropZoneDefault.setManaged(false);
        dropZoneFileInfo.setVisible(true);
        dropZoneFileInfo.setManaged(true);
        removeFileBtn.setVisible(true);
        removeFileBtn.setManaged(true);

        // Truncate long filenames
        String name = f.getName();
        if (name.length() > 28) {
            name = name.substring(0, 25) + "...";
        }
        dropZoneFileName.setText(name);
        dropZoneFileSize.setText(formatSize(f.length()));

        // Update drop zone style
        dropZone.getStyleClass().add("drop-zone-active");
        dropZone.getStyleClass().remove("drop-zone-hover");

        // Enable send button
        sendBtn.setDisable(false);

        setStatusWithDot(f.getName() + " · " + formatSize(f.length()), "idle");
    }

    private void clearSelectedFile() {
        selectedFile = null;

        // Restore default drop zone
        dropZoneDefault.setVisible(true);
        dropZoneDefault.setManaged(true);
        dropZoneFileInfo.setVisible(false);
        dropZoneFileInfo.setManaged(false);
        removeFileBtn.setVisible(false);
        removeFileBtn.setManaged(false);

        dropZone.getStyleClass().remove("drop-zone-active");
        sendBtn.setDisable(true);
        setStatusWithDot("Ready to transfer.", "idle");
    }

    private void showInviteCode(String code) {
        inviteCodeLabel.setText(code);
        inviteCodeSection.setVisible(true);
        inviteCodeSection.setManaged(true);

        // Fade in animation
        FadeTransition ft = new FadeTransition(Duration.millis(200), inviteCodeSection);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(200), inviteCodeSection);
        tt.setFromY(6);
        tt.setToY(0);

        new ParallelTransition(ft, tt).play();
    }

    private void setSendWaitingState() {
        sendBtn.setText("⟳ Waiting for connection...");
        sendBtn.setDisable(true);
        showCancelBtn(true);
        setStatusWithDot("Waiting for connection...", "send");
    }

    private void updateSendProgress(double progress) {
        int pct = (int)(progress * 100);
        setStatusWithDot("Sending... " + pct + "%", "send");
    }

    private void updateReceiveProgress(double progress) {
        int pct = (int)(progress * 100);
        receivePercent.setText(pct + "%");
        setStatusWithDot("Receiving... " + pct + "%", "receive");
    }

    private void setReceiveActiveState() {
        receiveBtn.setDisable(true);
        receiveBtn.setText("⟳ Connecting...");
        receiveProgressSection.setVisible(true);
        receiveProgressSection.setManaged(true);
        progressBar.setProgress(0);
        showCancelBtn(true);
        setStatusWithDot("Connecting to sender...", "receive");
    }

    private void showSendComplete() {
        sendBtn.setText("→ Send Another");
        sendBtn.setDisable(false);
        inviteCodeSection.setVisible(false);
        inviteCodeSection.setManaged(false);
        showCancelBtn(false);
        setStatusWithDot("Transfer complete!", "complete");
    }

    private void showReceiveComplete() {
        receiveBtn.setText("↓ Receive Another");
        receiveBtn.setDisable(false);
        showCancelBtn(false);
        setStatusWithDot("File received successfully!", "complete");
    }

    private void resetSendCard() {
        sendBtn.setText("→ Start Sending");
        sendBtn.setDisable(selectedFile == null);
        inviteCodeSection.setVisible(false);
        inviteCodeSection.setManaged(false);
        showCancelBtn(false);
    }

    private void resetReceiveCard() {
        receiveBtn.setText("↓ Receive File");
        receiveBtn.setDisable(false);
        receiveProgressSection.setVisible(false);
        receiveProgressSection.setManaged(false);
        progressBar.setProgress(0);
    }

    private void showCancelBtn(boolean show) {
        cancelBtn.setVisible(show);
        cancelBtn.setManaged(show);
    }

    private void setStatusWithDot(String msg, String state) {
        statusLabel.setText(msg);
        statusDot.getStyleClass().removeAll(
            "status-dot-idle", "status-dot-send",
            "status-dot-receive", "status-dot-complete",
            "status-dot-error");
        statusDot.getStyleClass().add("status-dot-" + state);
    }

    private void shakeNode(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(6);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    // Locked helper methods from original code
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024L * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024L * 1024 * 1024)) + " GB";
    }

    private boolean askApproval(String ip) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Incoming Connection");
            alert.setHeaderText("Incoming connection from " + ip);
            alert.setContentText("File: " + (selectedFile != null ? selectedFile.getName() : "unknown") + "\nAccept this transfer?");

            ButtonType accept = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            ButtonType reject = new ButtonType("Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(reject, accept);
            alert.showAndWait().ifPresentOrElse(
                btn -> future.complete(btn == accept),
                () -> future.complete(false)
            );
        });
        try {
            return future.get();
        } catch (Exception e) {
            return false;
        }
    }
}