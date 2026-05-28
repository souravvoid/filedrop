package com.peerlink.logic;

import com.peerlink.security.CryptoUtils;
import com.peerlink.security.HandshakeManager;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FileReceiver {

    private final String host;
    private final int port;
    private final File saveDir;
    private final Consumer<TransferStats> onProgress;
    private final Consumer<String> onStatus;
    
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Socket activeSocket;

    public FileReceiver(String host, int port, File saveDir, Consumer<TransferStats> onProgress, Consumer<String> onStatus) {
        this.host = host;
        this.port = port;
        this.saveDir = saveDir;
        this.onProgress = onProgress;
        this.onStatus = onStatus;
    }

    public void cancel() {
        cancelled.set(true);
        try { if (activeSocket != null) activeSocket.close(); } catch (Exception ignored) {}
    }

    public void receive() throws Exception {
        int retries = 3;
        while (retries > 0 && !cancelled.get()) {
            try {
                activeSocket = new Socket(host, port);
                break;
            } catch (Exception e) {
                retries--;
                if (retries == 0) throw new Exception("Could not connect to sender: " + e.getMessage());
                Thread.sleep(500);
            }
        }
        if (cancelled.get()) return;

        try {
            activeSocket.setTcpNoDelay(true);
            activeSocket.setSendBufferSize(4 * 1024 * 1024);
            activeSocket.setReceiveBufferSize(4 * 1024 * 1024);
            activeSocket.setSoTimeout(60000); 
            activeSocket.setPerformancePreferences(0, 0, 1);

            onStatus.accept("Performing secure handshake...");
            DataInputStream in = new DataInputStream(new BufferedInputStream(activeSocket.getInputStream(), 4 * 1024 * 1024));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(activeSocket.getOutputStream(), 4 * 1024 * 1024));

            SecretKey key = HandshakeManager.receiverHandshake(in, out);
            receiveFileData(in, key, saveDir);
            
            if (!cancelled.get()) {
               onStatus.accept("Transfer completed successfully.");
            }
        } finally {
            if (activeSocket != null && !activeSocket.isClosed()) {
                activeSocket.close();
            }
        }
    }

    private void receiveFileData(DataInputStream in, SecretKey key, File saveDir) throws Exception {
        int metaLen = in.readInt();
        if (metaLen <= 0 || metaLen > 8192) {
            throw new Exception("Protocol error: Invalid metadata length. Possible memory bomb.");
        }
        
        byte[] encryptedMetadata = in.readNBytes(metaLen);
        byte[] decryptedMetadata = CryptoUtils.decrypt(encryptedMetadata, key);

        ByteBuffer buffer = ByteBuffer.wrap(decryptedMetadata);
        int nameLen = buffer.getInt();
        if (nameLen <= 0 || nameLen > 4096) throw new Exception("Protocol error: Invalid filename length.");
        
        byte[] nameBytes = new byte[nameLen];
        buffer.get(nameBytes);
        String fileName = new String(nameBytes);
        long fileSize = buffer.getLong();

        onStatus.accept("Receiving file: " + fileName + " (" + (fileSize / 1024 / 1024) + " MB)");

        // Security: sanitize sender-supplied filename to prevent path traversal attacks
        String safeFileName = new File(fileName).getName();
        if (safeFileName.isEmpty()) safeFileName = "received_file";
        File outputFile = new File(saveDir, safeFileName);
        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(8);
        byte[] POISON = new byte[0];
        long[] totalReceived = new long[1];

        Thread consumer = Thread.ofVirtual().start(() -> {
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                long startTime = System.currentTimeMillis();
                while (!cancelled.get()) {
                    byte[] encryptedChunk = queue.take();
                    if (encryptedChunk == POISON) break;

                    byte[] plainChunk = CryptoUtils.decrypt(encryptedChunk, key);
                    fos.write(plainChunk);

                    totalReceived[0] += plainChunk.length;
                    
                    long elapsedMs = System.currentTimeMillis() - startTime;
                    double speedMBps = 0;
                    String eta = "Calculating...";
                    if (elapsedMs > 500) {
                        speedMBps = (totalReceived[0] / 1024.0 / 1024.0) / (elapsedMs / 1000.0);
                        if (speedMBps > 0) {
                            double remainingMB = (fileSize - totalReceived[0]) / 1024.0 / 1024.0;
                            int etaSec = (int) Math.round(remainingMB / speedMBps);
                            eta = etaSec + "s";
                        }
                    }
                    double progress = fileSize == 0 ? 1.0 : (double) totalReceived[0] / fileSize;
                    onProgress.accept(new TransferStats(progress, speedMBps, eta));
                }
            } catch (Exception e) {
                if (!cancelled.get()) onStatus.accept("Error writing to disk: " + e.getMessage());
                cancel();
            }
        });

        try {
            while (!cancelled.get()) {
                int chunkLen = in.readInt();
                if (chunkLen == -1) break;
                if (chunkLen <= 0 || chunkLen > 4 * 1024 * 1024 + 28) {
                    throw new Exception("Protocol error: Invalid chunk length limit exceeded.");
                }

                byte[] encryptedChunk = in.readNBytes(chunkLen);
                queue.put(encryptedChunk);
            }
        } catch (Exception e) {
            if (!cancelled.get()) throw e;
        } finally {
            queue.put(POISON);
            consumer.join();
        }

        if (!cancelled.get() && totalReceived[0] != fileSize) {
            throw new Exception("Transfer incomplete. Server disconnected prematurely.");
        } else if (!cancelled.get()) {
            onStatus.accept(String.format("File successfully saved to %s", outputFile.getAbsolutePath()));
        }
    }
}
