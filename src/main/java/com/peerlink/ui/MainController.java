package com.peerlink.ui;

import com.peerlink.logic.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MainController {

    @FXML private StackPane dropZone;
    @FXML private Label dropZoneLabel;
    
    @FXML private TextField sendCodeField;
    @FXML private Button sendBtn;
    
    @FXML private TextField receiveCodeInput;
    @FXML private Button receiveBtn;
    
    @FXML private Label statusLabel;
    @FXML private Button cancelBtn;
    @FXML private Button openFolderBtn;
    
    @FXML private ProgressBar progressBar;
    @FXML private HBox metricsBox;
    @FXML private Label speedLabel;
    @FXML private Label etaLabel;

    private File selectedFile;
    private FileSender activeSender;
    private FileReceiver activeReceiver;
    
    @FXML
    public void onDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    @FXML
    public void onDragDropped(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            selectedFile = e.getDragboard().getFiles().get(0);
            dropZoneLabel.setText(selectedFile.getName());
            dropZone.setStyle("-fx-border-color: #30d158;"); 
        }
        e.consume();
    }

    @FXML
    public void onSelectFile(ActionEvent e) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select file to share");
        File file = chooser.showOpenDialog(dropZone.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            dropZoneLabel.setText(file.getName());
            dropZone.setStyle("-fx-border-color: #30d158;");
        }
    }

    @FXML
    public void onCopyCode(ActionEvent e) {
        String code = sendCodeField.getText();
        if (code != null && !code.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(code);
            Clipboard.getSystemClipboard().setContent(content);
            statusLabel.setText("Invite code copied to clipboard.");
        }
    }

    @FXML
    public void onPasteCode(ActionEvent e) {
        String code = Clipboard.getSystemClipboard().getString();
        if (code != null) {
            receiveCodeInput.setText(code.trim());
        }
    }
    
    private void setTransferringState(boolean transferring, boolean showOpenFolder) {
        sendBtn.setDisable(transferring);
        receiveBtn.setDisable(transferring);
        cancelBtn.setVisible(transferring);
        cancelBtn.setManaged(transferring);
        progressBar.setVisible(transferring);
        progressBar.setManaged(transferring);
        metricsBox.setVisible(transferring);
        metricsBox.setManaged(transferring);
        
        openFolderBtn.setVisible(showOpenFolder);
        openFolderBtn.setManaged(showOpenFolder);
        
        if (!transferring) {
            progressBar.setProgress(0);
            speedLabel.setText("Speed: 0.0 MB/s");
            etaLabel.setText("ETA: --");
        }
    }

    @FXML
    public void onCancel(ActionEvent e) {
        if (activeSender != null) activeSender.cancel();
        if (activeReceiver != null) activeReceiver.cancel();
        setTransferringState(false, false);
        statusLabel.setText("Transfer cancelled.");
    }

    @FXML
    public void onOpenFolder(ActionEvent e) {
        File dir = new File(System.getProperty("user.home"), "Downloads");
        try {
            Desktop.getDesktop().open(dir);
        } catch (IOException ex) {
            statusLabel.setText("Error opening folder.");
        }
    }

    @FXML
    public void onSend(ActionEvent e) {
        if (selectedFile == null) {
            statusLabel.setText("Error: No file selected.");
            return;
        }

        setTransferringState(true, false);

        Thread.ofVirtual().start(() -> {
            try {
                int port = PortUtils.findFreePort();
                String host = NetworkUtils.getLocalIPAddress();
                String code = InviteCode.encode(host, port);
                
                Platform.runLater(() -> {
                    sendCodeField.setText(code);
                    statusLabel.setText("Waiting for receiver at " + host + "...");
                });
                
                activeSender = new FileSender(
                    selectedFile,
                    port,
                    stats -> Platform.runLater(() -> {
                        progressBar.setProgress(stats.progress);
                        speedLabel.setText(String.format("Speed: %.1f MB/s", stats.speedMBps));
                        etaLabel.setText("ETA: " + stats.etaSeconds);
                    }),
                    msg -> Platform.runLater(() -> {
                        statusLabel.setText(msg);
                        if (msg.contains("Error")) setTransferringState(false, false);
                        else if (msg.contains("successfully")) setTransferringState(false, false);
                    }),
                    this::askApproval
                );
                
                activeSender.startAndWait();
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + ex.getMessage());
                    setTransferringState(false, false);
                });
            } finally {
                activeSender = null;
            }
        });
    }

    @FXML
    public void onReceive(ActionEvent e) {
        String code = receiveCodeInput.getText().trim();
        if (code.isEmpty()) {
            statusLabel.setText("Error: Invite code is empty.");
            return;
        }

        setTransferringState(true, false);

        Thread.ofVirtual().start(() -> {
            try {
                String[] parts = InviteCode.decode(code);
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                File saveDir = new File(System.getProperty("user.home"), "Downloads");
                
                activeReceiver = new FileReceiver(
                    host,
                    port,
                    saveDir,
                    stats -> Platform.runLater(() -> {
                        progressBar.setProgress(stats.progress);
                        speedLabel.setText(String.format("Speed: %.1f MB/s", stats.speedMBps));
                        etaLabel.setText("ETA: " + stats.etaSeconds);
                    }),
                    msg -> Platform.runLater(() -> {
                        statusLabel.setText(msg);
                        if (msg.contains("Error")) setTransferringState(false, false);
                        else if (msg.contains("successfully")) setTransferringState(false, true);
                    })
                );
                
                activeReceiver.receive();
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + ex.getMessage());
                    setTransferringState(false, false);
                });
            } finally {
                activeReceiver = null;
            }
        });
    }

    private boolean askApproval(String ip) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Incoming Connection");
            alert.setContentText("Incoming connection from " + ip + ".\nDo you want to accept this transfer?");
            
            ButtonType accept = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
            ButtonType reject = new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(accept, reject);
            alert.showAndWait().ifPresent(type -> future.complete(type == accept));
        });
        
        try {
            return future.get();
        } catch (Exception e) {
            return false;
        }
    }
}
