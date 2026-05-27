package com.peerlink.ui;

import com.peerlink.logic.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class MainController {

    // Locked @FXML fields that must remain exactly as per spec
    @FXML private Label dropZoneLabel;
    @FXML private Label inviteCodeLabel;
    @FXML private TextField inviteCodeInput;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    // Sidebar and Navigation HBoxes
    @FXML private HBox navHome;
    @FXML private HBox navSend;
    @FXML private HBox navReceive;
    @FXML private HBox navDevices;
    @FXML private HBox navSettings;

    // Main StackPane Screens
    @FXML private VBox homeScreen;
    @FXML private VBox sendScreen;
    @FXML private VBox receiveScreen;
    @FXML private VBox devicesScreen;
    @FXML private ScrollPane settingsScreen;

    // Top Bar Info
    @FXML private Label deviceIpLabel;
    @FXML private Circle connectionStatusDot;

    // Home Screen elements
    @FXML private Label homeDeviceName;
    @FXML private Label homeDeviceIp;
    @FXML private Label homeDevicePort;
    @FXML private Circle homeStatusDot;
    @FXML private Label homeStatusText;

    // Drop zone / file selection
    @FXML private StackPane dropZone;
    @FXML private VBox dropZoneDefault;
    @FXML private VBox dropZoneFileInfo;
    @FXML private Label dropZoneFileName;
    @FXML private Label dropZoneFileSize;
    @FXML private Button removeFileBtn;
    @FXML private Button selectFileBtn;

    // Invite code row
    @FXML private VBox inviteCodeSection;
    @FXML private Button copyCodeBtn;
    @FXML private Button sendBtn;

    // Send Progress
    @FXML private VBox sendProgressSection;
    @FXML private Label sendProgressFileName;
    @FXML private Label sendProgressFileSize;
    @FXML private ProgressBar sendProgressBar;
    @FXML private Label sendStatSent;
    @FXML private Label sendStatSpeed;
    @FXML private Label sendStatEta;

    // Receive Card
    @FXML private HBox codeInputContainer;
    @FXML private Button pasteCodeBtn;
    @FXML private Button receiveBtn;

    // Receive Progress Section elements
    @FXML private VBox receiveProgressSection;
    @FXML private Label receiveFileName;
    @FXML private Label receiveFileSize;
    @FXML private Label receivePercent;
    @FXML private Label receiveSpeed;
    @FXML private Label receiveEta;

    // Status bar Cancel button
    @FXML private Label statusDot;
    @FXML private Button cancelBtn;

    // Devices & Manual Connect
    @FXML private TextField manualIpInput;

    // Settings elements
    @FXML private ToggleButton confirmReceiveToggle;
    @FXML private Label settingsPortLabel;
    @FXML private Label settingsDeviceNameLabel;

    // Private state
    private File selectedFile = null;
    private volatile boolean transferActive = false;
    private String currentInviteCode = "";
    private FileSender activeSender;
    private FileReceiver activeReceiver;
    private String localIp = "127.0.0.1";
    private int listeningPort = 4220;
    private File downloadFolder = new File(System.getProperty("user.home"), "Downloads");

    @FXML
    public void initialize() {
        // Setup local identity
        try {
            localIp = NetworkUtils.getLocalIPAddress();
            String host = InetAddress.getLocalHost().getHostName();
            
            deviceIpLabel.setText("IP: " + localIp);
            homeDeviceName.setText(host);
            homeDeviceIp.setText(localIp);
            settingsDeviceNameLabel.setText(host);
        } catch (Exception e) {
            deviceIpLabel.setText("IP: 127.0.0.1");
            homeDeviceName.setText("LocalHost");
            homeDeviceIp.setText("127.0.0.1");
            settingsDeviceNameLabel.setText("LocalHost");
        }

        // Wire inviteCodeInput focus listener for code-focused CSS class
        inviteCodeInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                codeInputContainer.getStyleClass().add("code-container-focused");
            } else {
                codeInputContainer.getStyleClass().remove("code-container-focused");
            }
        });

        // Wire removeFileBtn action
        removeFileBtn.setOnAction(e -> clearSelectedFile());

        // Initially disable sendBtn
        sendBtn.setDisable(true);
        
        // Setup status
        setStatusWithDot("Ready to transfer.", "idle");
    }

    // Navigation FXML Actions
    @FXML
    void onNavHome() {
        navigateTo("home");
    }

    @FXML
    void onNavSend() {
        navigateTo("send");
    }

    @FXML
    void onNavReceive() {
        navigateTo("receive");
    }

    @FXML
    void onNavDevices() {
        navigateTo("devices");
    }

    @FXML
    void onNavSettings() {
        navigateTo("settings");
    }

    private void navigateTo(String screen) {
        homeScreen.setVisible(screen.equals("home"));
        homeScreen.setManaged(screen.equals("home"));
        sendScreen.setVisible(screen.equals("send"));
        sendScreen.setManaged(screen.equals("send"));
        receiveScreen.setVisible(screen.equals("receive"));
        receiveScreen.setManaged(screen.equals("receive"));
        devicesScreen.setVisible(screen.equals("devices"));
        devicesScreen.setManaged(screen.equals("devices"));
        settingsScreen.setVisible(screen.equals("settings"));
        settingsScreen.setManaged(screen.equals("settings"));

        // Update nav styling
        updateNavItemActive(navHome, screen.equals("home"));
        updateNavItemActive(navSend, screen.equals("send"));
        updateNavItemActive(navReceive, screen.equals("receive"));
        updateNavItemActive(navDevices, screen.equals("devices"));
        updateNavItemActive(navSettings, screen.equals("settings"));

        Node activeNode = null;
        if (screen.equals("home")) activeNode = homeScreen;
        else if (screen.equals("send")) activeNode = sendScreen;
        else if (screen.equals("receive")) activeNode = receiveScreen;
        else if (screen.equals("devices")) activeNode = devicesScreen;
        else if (screen.equals("settings")) activeNode = settingsScreen;

        if (activeNode != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(150), activeNode);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    private void updateNavItemActive(HBox item, boolean active) {
        item.getStyleClass().remove("nav-item-active");
        if (active) {
            item.getStyleClass().add("nav-item-active");
        }
    }

    // Devices actions
    @FXML
    void onRefreshDevices() {
        // Discovery is simulated/coming soon
        setStatusWithDot("Refreshing devices...", "idle");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> setStatusWithDot("No other devices discovered on local network.", "idle"));
        pause.play();
    }

    @FXML
    void onCopyMyCode() {
        if (currentInviteCode != null && !currentInviteCode.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentInviteCode);
            clipboard.setContent(content);
            setStatusWithDot("Invite code copied!", "idle");
        } else {
            setStatusWithDot("No active transfer. Go to Send screen to get a code.", "idle");
        }
    }

    @FXML
    void onManualConnect() {
        String manualIp = manualIpInput.getText().trim();
        if (manualIp.isEmpty()) {
            setStatusWithDot("Please enter a valid IP address.", "error");
            return;
        }
        inviteCodeInput.setText(InviteCode.encode(manualIp, 4220));
        navigateTo("receive");
    }

    // Settings actions
    @FXML
    void onChangeDownloadsFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Save Folder");
        File dir = chooser.showDialog(settingsScreen.getScene().getWindow());
        if (dir != null) {
            downloadFolder = dir;
            setStatusWithDot("Save directory changed to " + dir.getName(), "idle");
        }
    }

    // LOCKED METHOD — onDragOver
    @FXML
    void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
            if (!dropZone.getStyleClass().contains("drop-zone-drag-over")) {
                dropZone.getStyleClass().add("drop-zone-drag-over");
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
        dropZone.getStyleClass().remove("drop-zone-drag-over");
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
                listeningPort = port;
                String host = NetworkUtils.getLocalIPAddress();
                currentInviteCode = InviteCode.encode(host, port);

                Platform.runLater(() -> {
                    showInviteCode(currentInviteCode);
                    settingsPortLabel.setText(String.valueOf(port));
                    homeDevicePort.setText("PORT " + port);
                    setStatusWithDot("Waiting for connection...", "send");
                });

                activeSender = new FileSender(
                    selectedFile,
                    port,
                    stats -> Platform.runLater(() -> {
                        progressBar.setProgress(stats.progress);
                        sendProgressBar.setProgress(stats.progress);
                        updateSendProgress(stats.progress, stats.speedMBps, stats.etaSeconds);
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

                downloadFolder.mkdirs();

                activeReceiver = new FileReceiver(
                    host,
                    port,
                    downloadFolder,
                    stats -> Platform.runLater(() -> {
                        progressBar.setProgress(stats.progress);
                        updateReceiveProgress(stats.progress, stats.speedMBps, stats.etaSeconds);
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

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(currentInviteCode);
        clipboard.setContent(content);

        copyCodeBtn.setText("✓");
        copyCodeBtn.getStyleClass().add("copy-success");

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
        try {
            Desktop.getDesktop().open(downloadFolder);
        } catch (IOException ex) {
            setStatusWithDot("Error opening folder.", "error");
        }
    }

    private void setSelectedFile(File f) {
        selectedFile = f;

        dropZoneDefault.setVisible(false);
        dropZoneDefault.setManaged(false);
        dropZoneFileInfo.setVisible(true);
        dropZoneFileInfo.setManaged(true);
        removeFileBtn.setVisible(true);
        removeFileBtn.setManaged(true);

        String name = f.getName();
        if (name.length() > 28) {
            name = name.substring(0, 25) + "...";
        }
        dropZoneFileName.setText(name);
        dropZoneFileSize.setText(formatSize(f.length()));

        dropZone.getStyleClass().remove("drop-zone-drag-over");
        dropZone.getStyleClass().add("drop-zone-file-selected");

        sendBtn.setDisable(false);

        setStatusWithDot(f.getName() + " · " + formatSize(f.length()), "idle");
    }

    private void clearSelectedFile() {
        selectedFile = null;

        dropZoneDefault.setVisible(true);
        dropZoneDefault.setManaged(true);
        dropZoneFileInfo.setVisible(false);
        dropZoneFileInfo.setManaged(false);
        removeFileBtn.setVisible(false);
        removeFileBtn.setManaged(false);

        dropZone.getStyleClass().remove("drop-zone-file-selected");
        sendBtn.setDisable(true);
        setStatusWithDot("Ready to transfer.", "idle");
    }

    private void showInviteCode(String code) {
        inviteCodeLabel.setText(code);
        inviteCodeSection.setVisible(true);
        inviteCodeSection.setManaged(true);

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

    private void updateSendProgress(double progress, double speed, String eta) {
        sendProgressSection.setVisible(true);
        sendProgressSection.setManaged(true);
        
        String name = selectedFile != null ? selectedFile.getName() : "Unknown File";
        if (name.length() > 28) {
            name = name.substring(0, 25) + "...";
        }
        sendProgressFileName.setText(name);
        sendProgressFileSize.setText(selectedFile != null ? formatSize(selectedFile.length()) : "");
        
        int pct = (int)(progress * 100);
        sendStatSent.setText(pct + "%");
        sendStatSpeed.setText(String.format("%.1f MB/s", speed));
        sendStatEta.setText(eta);
        
        setStatusWithDot("Sending... " + pct + "%", "send");
    }

    private void updateReceiveProgress(double progress, double speed, String eta) {
        int pct = (int)(progress * 100);
        receivePercent.setText(pct + "%");
        receiveSpeed.setText(String.format("%.1f MB/s", speed));
        receiveEta.setText(eta);
        
        setStatusWithDot("Receiving... " + pct + "%", "receive");
    }

    private void setReceiveActiveState() {
        receiveBtn.setDisable(true);
        receiveBtn.setText("⟳ Connecting...");
        receiveProgressSection.setVisible(true);
        receiveProgressSection.setManaged(true);
        progressBar.setProgress(0);
        showCancelBtn(true);
        
        receiveFileName.setText("Incoming File");
        receiveFileSize.setText("Calculating...");
        
        setStatusWithDot("Connecting to sender...", "receive");
    }

    private void showSendComplete() {
        sendBtn.setText("→ Send Another");
        sendBtn.setDisable(false);
        inviteCodeSection.setVisible(false);
        inviteCodeSection.setManaged(false);
        sendProgressSection.setVisible(false);
        sendProgressSection.setManaged(false);
        showCancelBtn(false);
        setStatusWithDot("Transfer complete!", "complete");
    }

    private void showReceiveComplete() {
        receiveBtn.setText("↓ Receive Another");
        receiveBtn.setDisable(false);
        receiveProgressSection.setVisible(false);
        receiveProgressSection.setManaged(false);
        showCancelBtn(false);
        setStatusWithDot("File received successfully!", "complete");
    }

    private void resetSendCard() {
        sendBtn.setText("→ Start Sending");
        sendBtn.setDisable(selectedFile == null);
        inviteCodeSection.setVisible(false);
        inviteCodeSection.setManaged(false);
        sendProgressSection.setVisible(false);
        sendProgressSection.setManaged(false);
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
        homeStatusText.setText(msg);
        statusDot.getStyleClass().removeAll(
            "status-dot-idle", "status-dot-send",
            "status-dot-receive", "status-dot-complete",
            "status-dot-error");
        statusDot.getStyleClass().add("status-dot-" + state);
        
        // Update connection status dot at home and top
        connectionStatusDot.setFill(Paint.valueOf(state.equals("idle") || state.equals("complete") ? "#4a9e6b" : "#9e7a4a"));
        homeStatusDot.setFill(Paint.valueOf(state.equals("idle") || state.equals("complete") ? "#4a9e6b" : "#9e7a4a"));
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
            
            // Apply custom stylesheet to alert dialog if possible
            DialogPane pane = alert.getDialogPane();
            pane.getStylesheets().add(getClass().getResource("/com/peerlink/ui/peerlink.css").toExternalForm());
            pane.getStyleClass().add("dialog-card");
            
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