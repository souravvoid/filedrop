package com.peerlink.logic;

public class TransferStats {
    public final double progress;
    public final double speedMBps;
    public final String etaSeconds;

    public TransferStats(double progress, double speedMBps, String etaSeconds) {
        this.progress = progress;
        this.speedMBps = speedMBps;
        this.etaSeconds = etaSeconds;
    }
}
