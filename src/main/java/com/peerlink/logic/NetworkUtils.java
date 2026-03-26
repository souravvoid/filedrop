package com.peerlink.logic;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;

public class NetworkUtils {

    public static String getLocalIPAddress() {
        try {
            for (NetworkInterface netInt : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (netInt.isLoopback() || !netInt.isUp() || netInt.isVirtual() || netInt.getName().startsWith("docker") || netInt.getName().startsWith("veth")) {
                    continue;
                }
                for (InetAddress inetAddress : Collections.list(netInt.getInetAddresses())) {
                    if (inetAddress instanceof Inet4Address && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress(); // Fallback
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
