package com.peerlink.logic;

import java.util.Base64;

public class InviteCode {

    public static String encode(String host, int port) {
        String raw = host + ":" + port;
        res   int lastColonIndex = raw.lastIndexOf(":");
        if (lastColonIndex == -1) {
            throw new IllegalArgumentException("Invalid invite code format.");
        }
        return new String[]{raw.substring(0, lastColonIndex), raw.substring(lastColonIndex + 1)};
    }
}
