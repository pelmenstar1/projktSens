package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {
    private static final char[] TO_STRING_BUFFER = new char[15 /* max possible length of string */];

    /**
     * Special IP which signals about some kind of error
     */
    public static final int IP_ERROR = 0;
    public static final int FREE_MIN_PORT = 1024;
    public static final int FREE_MAX_PORT = 49151;

    /**
     * Determines whether port is free (not reserved).
     */
    public static boolean isValidFreePort(int port) {
        return port >= FREE_MIN_PORT && port <= FREE_MAX_PORT;
    }

    /**
     * Packs 4 bytes into int. Each byte should be in range of [0; 255].
     * Pretty useful in case of creating constant address, like {@code ip(127, 0, 0, 1)}
     */
    public static int ip(int b1, int b2, int b3, int b4) {
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
    }

    public static void appendIntIpv4(int value, @NotNull StringBuilder sb) {
        int b1 = value & 0xff;
        int b2 = (value >> 8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        sb.append(b1);
        sb.append('.');
        sb.append(b2);
        sb.append('.');
        sb.append(b3);
        sb.append('.');
        sb.append(b4);
    }

    /**
     * Returns string representation of IPv4 stored in int.
     */
    @NotNull
    public static String intIpv4ToString(int value) {
        int b1 = value & 0xff;
        int b2 = (value >> 8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        int b1Length = MyMath.decimalDigitCount(b1);
        int b2Length = MyMath.decimalDigitCount(b2);
        int b3Length = MyMath.decimalDigitCount(b3);
        int b4Length = MyMath.decimalDigitCount(b4);

        int b2Index = b1Length + 1;
        int b3Index = b2Index + b2Length + 1;
        int b4Index = b3Index + b3Length + 1;

        int bufferLength = b4Index + b4Length;
        char[] buffer = TO_STRING_BUFFER;

        StringUtils.writeByte(buffer, 0, b1);
        buffer[b1Length] = '.';
        StringUtils.writeByte(buffer, b2Index, b2);
        buffer[b2Index + b2Length] = '.';
        StringUtils.writeByte(buffer, b3Index, b3);
        buffer[b3Index + b4Length] = '.';
        StringUtils.writeByte(buffer, b4Index, b4);

        return new String(buffer, 0, bufferLength);
    }

    /**
     * Converts IPv4 stored in int to {@link InetAddress}
     */
    @NotNull
    public static InetAddress parseInt(int value) {
        byte[] data = new byte[]{
                (byte) value,
                (byte) (value >> 8),
                (byte) (value >> 16),
                (byte) (value >> 24)
        };

        try {
            return InetAddress.getByAddress(data);
        } catch (UnknownHostException e) {
            // can't happen
            // rethrow to make compiler happy
            throw new RuntimeException();
        }
    }

    /**
     * Parses IPv4 string to int
     *
     * @return ip stored in int. If buffer has invalid format, returns {@link InetAddressUtils#IP_ERROR}
     */
    public static int parseNumericalIpv4ToInt(@NotNull String str) {
        int ip = 0;
        int byteIndex = 0;
        int currentByte = 0;
        int strByteStartIndex = 0;

        int maxIdx = str.length() - 1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int d = c - '0';

            if (d >= 0 && d <= 9) {
                currentByte = (currentByte << 3) + (currentByte << 1) + d;

                if (currentByte > 255) {
                    return IP_ERROR;
                }
            } else if (c == '.') {
                if (byteIndex == 4 || i == maxIdx || i == strByteStartIndex) {
                    return IP_ERROR;
                }

                ip = Bytes.withByte(ip, byteIndex, currentByte);
                strByteStartIndex = i + 1;
                byteIndex++;

                currentByte = 0;
            } else {
                return IP_ERROR;
            }
        }

        if (byteIndex != 3) {
            return IP_ERROR;
        }

        return Bytes.withByte(ip, byteIndex, currentByte);
    }


    /**
     * Parses only numerical IP address (like 1.2.3.4).
     *
     * @return result {@link InetAddress} if parsing succeed, otherwise, null
     */
    @Nullable
    public static InetAddress parseNumericalIpv4OrNull(@NotNull String str) {
        int ip = parseNumericalIpv4ToInt(str);
        if (ip == IP_ERROR) {
            return null;
        }

        return parseInt(ip);
    }

    /**
     * Returns whether numerical IPv4 string is valid
     *
     * @param str IPv4 string
     */
    public static boolean isValidNumericalIpv4(@NotNull String str) {
        return parseNumericalIpv4ToInt(str) != IP_ERROR;
    }
}
