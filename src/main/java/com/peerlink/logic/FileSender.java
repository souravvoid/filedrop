package com.peerlink.logic;

import com.peerlink.security.CryptoUtils;
import com.peerlink.security.HandshakeManager;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FileSender {

    private final File file;
    private final int port;
    private final Consumer<TransferStats> onProgress;
    private final Consumer<String> onStatus;
    private final Predicate<String> approvalCallback;
    
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private Socket activeSocket;

    public FileSender(File file, int port, Consumer<TransferStats> onProgress, Consumer<String> onStatus, Predicate<String> approvalCallback) {
        this.file = file;
        this.port = port;
        this.onProgress = onProgress;
        this.onStatus = onStatus;
        this.approvalCallback = approvalCallback;
    }

    public void cancel() {
        cancelled.set(true);
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        try { if (activeSocket != null) activeSocket.close(); } catch (Exception ignored) {}
    }

    public void startAndWait() throws Exception {
        serverSocket = new ServerSocket(port);
        long sizeMB = file.length() / (1024 * 1024);
        int timeout = (int) Math.max(sizeMB * 2000, 600000);
        serverSocket.setSoTimeout(timeout);

        try {
            while (!cancelled.get()) {
                onStatus.accept("Waiting for receiver to connect...");
                try {
                    activeSocket = serverSocket.accept();
                    activeSocket.setTcpNoDelay(true);
                    activeSocket.setSendBufferSize(4 * 1024 * 1024);
                    activeSocket.setReceiveBufferSize(4 * 1024 * 1024);
                    activeSocket.setPerformancePreferences(0, 0, 1);

                    String peerIp = activeSocket.getInetAddress().getHostAddress();
                    if (!approvalCallback.test(peerIp)) {
                        activeSocket.close();
                        continue; // Keep listening for right peer if rejected
                    }

                    onStatus.accept("Performing secure handshake...");
                    DataInputStream in = new DataInputStream(new BufferedInputStream(activeSocket.getInputStream(), 4 * 1024 * 1024));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(activeSocket.getOutputStream(), 4 * 1024 * 1024));

                    SecretKey key = HandshakeManager.senderHandshake(in, out);
                    onStatus.accept("Sending file metadata...");
                    sendMetadata(out, key, file);
                    
                    onStatus.accept("Sending file data...");
                    sendFileData(out, key, file);
                    
                    if (!cancelled.get()) {
                        onStatus.accept("Transfer completed successfully.");
                    }
                    break;
                } catch (SocketTimeoutException e) {
                    if (!cancelled.get()) onStatus.accept("Error: Connection timed out.");
                    break;
                } catch (Exception e) {
                    if (!cancelled.get()) onStatus.accept("Error: " + e.getMessage());
                    break;
                } finally {
                    if (activeSocket != null && !activeSocket.isClosed()) {
                        activeSocket.close();
                    }
                }
            }
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    private void sendMetadata(DataOutputStream out, SecretKey key, File file) throws Exception {
        byte[] nameBytes = file.getName().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(4 + nameBytes.length + 8);
        buffer.putInt(nameBytes.length);
        buffer.put(nameBytes);
        buffer.putLong(file.length());

        byte[] encryptedMetadata = CryptoUtils.encrypt(buffer.array(), key);
        out.writeInt(encryptedMetadata.length);
        out.write(encryptedMetadata);
        out.flush();
    }

    private void sendFileData(DataOutputStream out, SecretKey key, File file) throws Exception {
        byte[] baseIv = new byte[12];
        new SecureRandom().nextBytes(baseIv);
        byte[] ivCopy = Arrays.copyOf(baseIv, baseIv.length);

        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(8);
        byte[] POISON = new byte[0];

        Thread producer = Thread.ofVirtual().start(() -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4 * 1024 * 1024];
                int read;
                while (!cancelled.get() && (read = fis.read(buffer)) != -1) {
                    byte[] chunk = Arrays.copyOf(buffer, read);
                    byte[] encrypted = CryptoUtils.encryptWithIV(chunk, key, ivCopy);
                    CryptoUtils.incrementIV(ivCopy);
                    queue.put(encrypted);
                }
            } catch (Exception ignored) {
            } finally {
                try { queue.put(POISON); } catch (Exception ignored) {}
            }
        });

        long totalSent = 0;
        long fileSize = file.length();
        long startTime = System.currentTimeMillis();
        
        try {
            while (!cancelled.get()) {
                byte[] encrypted = queue.take();
                if (encrypted == POISON) {
                    break;
                }

                out.writeInt(encrypted.length);
                out.write(encrypted);
                
                totalSent += (encrypted.length - 28);
                totalSent = Math.min(totalSent, fileSize);
                
                long elapsedMs = System.currentTimeMillis() - startTime;
                double speedMBps = 0;
                String eta = "Calculating...";
                if (elapsedMs > 500) {
                    speedMBps = (totalSent / 1024.0 / 1024.0) / (elapsedMs / 1000.0);
                    if (speedMBps > 0) {
                        double remainingMB = (fileSize - totalSent) / 1024.0 / 1024.0;
                        int etaSec = (int) Math.round(remainingMB / speedMBps);
                        eta = etaSec + "s";
                    }
                }
                
                double progress = fileSize == 0 ? 1.0 : (double) totalSent / fileSize;
                onProgress.accept(new TransferStats(progress, speedMBps, eta));
            }
        } finally {
            if (!cancelled.get()) {
                out.writeInt(-1);
                out.flush();
            }
            producer.join();
        }
        
        if (totalSent != fileSize && !cancelled.get()) {
            throw new Exception("File read failed prematurely.");
        }
    }
}
