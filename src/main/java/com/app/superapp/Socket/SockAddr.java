package com.app.superapp.Socket;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class SockAddr extends Structure {
    public short sin_family; // Address family (e.g., AF_INET)
    public short sin_port;   // Port number
    public byte[] sin_addr = new byte[4]; // IPv4 address
    public byte[] sin_zero = new byte[8]; // Padding for structure alignment

    public SockAddr() {
        super();
    }
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("sin_family", "sin_port", "sin_addr", "sin_zero");
    }
    public SockAddr(short family, int port, byte[] addr) {
        sin_family = family;
        sin_port = (short) port;
        System.arraycopy(addr, 0, sin_addr, 0, 4);
    }
}
