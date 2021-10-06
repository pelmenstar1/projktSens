package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Represents a helper class that efficiently build strings
 */
public final class StringUtils {
    private static final byte[] DIGITS = {
            0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x20,
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x30,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40,
            0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x50,
            0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x60,
            0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x70,
            0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0xffffff80,
            0xffffff81, 0xffffff82, 0xffffff83, 0xffffff84, 0xffffff85, 0xffffff86,
            0xffffff87, 0xffffff88, 0xffffff89, 0xffffff90, 0xffffff91, 0xffffff92,
            0xffffff93, 0xffffff94, 0xffffff95, 0xffffff96, 0xffffff97, 0xffffff98,
            0xffffff99,
    };

    private static final char[] hexBuffer = new char[8];

    private StringUtils() {
    }

    @NotNull
    public static String toStringHexColors(int @NotNull [] colors) {
        StringBuilder sb = new StringBuilder(hexColorsLength(colors.length));
        appendHexColors(colors, sb);

        return sb.toString();
    }

    public static void appendHexColors(
            int @NotNull [] colors, int offset, int length,
            @NotNull StringBuilder sb
    ) {
        sb.ensureCapacity(sb.length() + hexColorsLength(length));

        sb.append('[');
        if (colors.length > 0) {
            int maxIdx = offset + length - 1;

            synchronized (hexBuffer) {
                for (int i = offset; i < maxIdx; i++) {
                    appendHexThroughBufferLocked(colors[i], sb);
                    sb.append(',');
                }

                appendHexThroughBufferLocked(colors[maxIdx], sb);
            }
        }
        sb.append(']');
    }

    public static void appendHexColors(
            int @NotNull [] colors,
            @NotNull StringBuilder sb
    ) {
        appendHexColors(colors, 0, colors.length, sb);
    }

    private static void appendHexThroughBufferLocked(int value, @NotNull StringBuilder sb) {
        sb.append('#');
        writeHex(value, hexBuffer, 0);
        sb.append(hexBuffer);
    }

    private static int hexColorsLength(int arrayLength) {
        int t = arrayLength << 3 + arrayLength; // arrayLength * 9
        return t + (arrayLength - 1) + 2; // including commas and [, ]
    }

    public static void appendHexColor(int value, @NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + 9);
        sb.append('#');
        appendHex(value, sb);
    }

    public static void appendHex(int value, @NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + 8);

        synchronized (hexBuffer) {
            writeHex(value, hexBuffer, 0);
            sb.append(hexBuffer);
        }
    }

    public static void writeHex(int value, char @NotNull [] buffer, int offset) {
        for (int i = offset + 7; i >= offset; i--) {
            int digit = (value & 0x0F);
            int c = digit + '0';
            if (c > '9') {
                c += 0x27;
            }

            buffer[i] = (char) c;
            value >>= 4;
        }
    }

    public static int parsePositiveInt(@NotNull CharSequence text) {
        int n = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int d = c - '0';

            if (d >= 0 && d <= 9) {
                n = ((n << 3) + (n << 1)) + d;
            } else {
                return -1;
            }
        }

        return n;
    }

    /**
     * Appends given array of {@link Object}'s to the end of {@link StringBuilder} in format: <br/>
     * - if specified array is null, appends 'null' string
     * - otherwise, appends [$1st element$, $2nd element$, $3rd element$, ...]
     *
     * @param array array to append to {@link StringBuilder}
     * @param sb    receiver
     */
    public static void appendArray(@Nullable Object @Nullable [] array, @NotNull StringBuilder sb) {
        if (array == null) {
            appendNull(sb);
        } else {
            sb.append('[');
            if (array.length > 0) {
                int endIdx = array.length - 1;

                for (int i = 0; i < endIdx; i++) {
                    sb.append(array[i]);
                    sb.append(',');
                }

                sb.append(array[endIdx]);
            }
            sb.append(']');
        }
    }

    /**
     * Appends given array of {@link AppendableToStringBuilder}'s to the end of {@link StringBuilder} in format: <br/>
     * - if specified array is null, appends 'null' string
     * - otherwise, appends [$1st element$, $2nd element$, $3rd element$, ...]
     *
     * @param array array to append to {@link StringBuilder}
     * @param sb    receiver
     */
    public static void appendArray(@Nullable AppendableToStringBuilder @Nullable [] array, @NotNull StringBuilder sb) {
        if (array == null) {
            appendNull(sb);
        } else {
            sb.append('[');
            if (array.length > 0) {
                int endIdx = array.length - 1;

                for (int i = 0; i < endIdx; i++) {
                    AppendableToStringBuilder element = array[i];

                    append(element, sb);
                    sb.append(',');
                }

                append(array[endIdx], sb);
            }
            sb.append(']');
        }
    }

    private static void appendNull(@NotNull StringBuilder sb) {
        sb.append((String)null);
    }

    private static void append(@Nullable AppendableToStringBuilder value, @NotNull StringBuilder sb) {
        if(value == null) {
            appendNull(sb);
        } else {
            value.append(sb);
        }
    }

    public static int getRound1Length(float number) {
        int i = (int)number;
        int length = MyMath.decimalDigitCount(i) + 2;
        if(i < 0) {
            length++;
        }

        return length;
    }

    public static int getSignedRound1Length(float number) {
        return MyMath.decimalDigitCount((int)number) + 3;
    }

    @NotNull
    public static String toStringRound1(float number) {
        StringBuilder sb = new StringBuilder(getRound1Length(number));
        appendRound1(number, sb);

        return sb.toString();
    }

    public static void appendRound1(float number, @NotNull StringBuilder sb) {
        int i = (int) number;
        if(number < 0) {
            i = -i;
            number = -number;
            sb.append('-');
        }

        int fr = (int) ((number - i) * 10f);

        sb.append(i);
        sb.append('.');
        sb.append((char) ('0' + fr));
    }

    public static void appendSignedRound1(float number, @NotNull StringBuilder sb) {
        if (number > 0f) {
            sb.append('+');
        }

        appendRound1(number, sb);
    }

    @NotNull
    public static String twoDigits(int number) {
        char[] buffer = new char[2];

        writeTwoDigits(buffer, 0, number);

        return new String(buffer, 0, 2);
    }

    public static void appendTwoDigits(int number, @NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + 2);
        appendTwoDigitsInternal(number, sb);
    }

    public static void writeTwoDigits(char @NotNull [] buffer, int offset, int number) {
        int tenOne = DIGITS[number] & 0xFF;
        int ten = tenOne >> 4;
        int one = tenOne & 0xF;

        buffer[offset] = (char)('0' + ten);
        buffer[offset + 1] = (char)('0' + one);
    }

    private static void appendTwoDigitsInternal(int number, @NotNull StringBuilder sb) {
        int tenOne = DIGITS[number] & 0xFF;
        int ten = tenOne >> 4;
        int one = tenOne & 16;

        sb.append((char)('0' + ten));
        sb.append((char)('0' + one));
    }

    private static void writeThreeDigits(char @NotNull [] buffer, int offset, int number) {
        int d = number / 10;
        int r = number - ((d << 3) + (d << 1));

        writeTwoDigits(buffer, offset, d);
        buffer[offset + 2] = (char) ('0' + r);
    }

    @NotNull
    public static String fourDigits(int number) {
        char[] buffer = new char[4];

        writeFourDigits(buffer, 0, number);

        return new String(buffer, 0, 4);
    }

    public static void appendFourDigits(int number, @NotNull StringBuilder sb) {
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("number=" + number + ". Number must be in range [0; 9999]");
        }

        sb.ensureCapacity(sb.length() + 4);

        int d = number / 100;
        int r = number - ((d << 6) + (d << 5) + (d << 2));

        appendTwoDigitsInternal(d, sb);
        appendTwoDigitsInternal(r, sb);
    }

    public static void writeFourDigits(char @NotNull [] buffer, int offset, int number) {
        int d = number / 100;
        int r = number - ((d << 6) + (d << 5) + (d << 2));

        writeTwoDigits(buffer, offset, d);
        writeTwoDigits(buffer, offset + 2, r);
    }

    public static void writeByte(char @NotNull [] buffer, int offset, int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("value");
        }

        if (value < 10) {
            buffer[offset] = (char) ('0' + value);
        } else if (value < 100) {
            writeTwoDigits(buffer, offset, value);
        } else {
            writeThreeDigits(buffer, offset, value);
        }
    }
}
