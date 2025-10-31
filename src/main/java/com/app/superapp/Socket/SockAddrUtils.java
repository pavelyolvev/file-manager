package com.app.superapp.Socket;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SockAddrUtils {
    public static int ipToInteger(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            byte[] bytes = inetAddress.getAddress();
            int result = 0;
            for (byte b : bytes) {
                result = (result << 8) | (b & 0xFF);
            }
            return result;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

