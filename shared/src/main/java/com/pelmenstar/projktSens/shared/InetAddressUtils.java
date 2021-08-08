package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {
    public static final int IP_ERROR = 0;

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
        char[] buffer = new char[bufferLength];
        StringUtils.writeByte(buffer, 0, b1);
        buffer[b1Length] = '.';
        StringUtils.writeByte(buffer, b2Index, b2);
        buffer[b2Index + b2Length] = '.';
        StringUtils.writeByte(buffer, b3Index, b3);
        buffer[b3Index + b4Length] = '.';
        StringUtils.writeByte(buffer, b4Index, b4);

        return new String(buffer, 0, bufferLength);
    }

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

    public static int parseNumericalIpv4ToInt(@NotNull String str) {
        int ip = 0;
        int byteIndex = 0;
        int currentByte = 0;
        int strByteStartIndex = 0;

        int maxIdx = str.length() - 1;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if(c >= '0' && c <= '9') {
                currentByte = currentByte * 10 + (c - '0');

                if(currentByte > 255) {
                    return IP_ERROR;
                }
            } else if(c == '.') {
                if(byteIndex == 4 || i == maxIdx || i == strByteStartIndex) {
                    return IP_ERROR;
                }

                ip = Bytes.withByte(ip, byteIndex, currentByte & 0xff);
                strByteStartIndex = i + 1;
                byteIndex++;

                currentByte = 0;
            } else {
                return IP_ERROR;
            }
        }

        if(byteIndex != 3) {
            return IP_ERROR;
        }

        return Bytes.withByte(ip, byteIndex, currentByte & 0xff);
    }

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
        int ip = parseNumericalIpv4ToInt(str);
        if(ip == IP_ERROR) {
            return null;
        }

        return parseInt(ip);
    }

    public static boolean isValidNumericalIpv4(@NotNull String str) {
        return parseNumericalIpv4ToInt(str) != IP_ERROR;
    }
}
