package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {
    @NotNull
    public static InetAddress parseNumericalIpv4OrThrow(@NotNull String str) {
        InetAddress result = parseNumericalIpv4OrNull(str);
        if(result == null) {
            throw new RuntimeException("str has invalid format");
        }

        return result;
    }

    /**
     * Parses only numerical IP address (like 1.2.3.4).
     *
     * @return result {@link InetAddress} if parsing succeed, otherwise, null
     */
    @Nullable
    public static InetAddress parseNumericalIpv4OrNull(@NotNull String str) {
        byte[] buffer = new byte[4];
        int bufferIndex = 0;
        int currentByte = 0;

        int maxIdx = str.length() - 1;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if(c >= '0' && c <= '9') {
                currentByte = currentByte * 10 + (c - '0');

                if(currentByte > 255) {
                    return null;
                }
            } else if(c == '.') {
                if(bufferIndex == 4 || i == maxIdx) {
                    return null;
                }

                buffer[bufferIndex++] = (byte)currentByte;

                currentByte = 0;
            } else {
                return null;
            }
        }

        if(bufferIndex != 3) {
            return null;
        }
        buffer[bufferIndex] = (byte)currentByte;

        InetAddress address = null;
        try {
            address = InetAddress.getByAddress(buffer);
        } catch (UnknownHostException e) {
            // throws only when buffer has invalid size
        }

        // here it's not null
        return address;
    }

    public static boolean isValidNumericalIpv4(@NotNull String str) {
        int bufferIndex = 0;
        int currentByte = 0;

        int maxIdx = str.length() - 1;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if(c >= '0' && c <= '9') {
                currentByte = currentByte * 10 + (c - '0');

                if(currentByte > 255) {
                    return false;
                }
            } else if(c == '.') {
                if(bufferIndex == 4 || i == maxIdx) {
                    return false;
                }

                bufferIndex++;
                currentByte = 0;
            } else {
                return false;
            }
        }

        return bufferIndex == 3;
    }
}
