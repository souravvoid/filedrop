package com.peerlink.logic;

import java.util.Base64;

public class InviteCode {

    public static String encode(String host, int port) {
        String raw = host + ":" + port;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
    }

    public static String[] decode(String code) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(code);
        String raw = new String(decodedBytes);
        int lastColonIndex = raw.lastIndexOf(":");
        if (lastColonIndex == -1) {
            throw new IllegalArgumentException("Invalid invite code format.");
        }
        return new String[]{raw.substring(0, lastColonIndex), raw.substring(lastColonIndex + 1)};
    }
}
