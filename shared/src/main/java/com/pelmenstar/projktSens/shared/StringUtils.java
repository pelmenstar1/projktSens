package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Represents a helper class that efficiently build strings
 */
public final class StringUtils {
    private static final byte[] DIGITS_TENS = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    private static final byte[] DIGIT_ONES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
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

    public static void appendHexColors(int @NotNull [] colors, @NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + hexColorsLength(colors.length));

        sb.append('[');
        if (colors.length > 0) {
            int maxIdx = colors.length - 1;

            synchronized (hexBuffer) {
                for (int i = 0; i < maxIdx; i++) {
                    appendHexThroughBufferLocked(colors[i], sb);
                    sb.append(',');
                }

                appendHexThroughBufferLocked(colors[maxIdx], sb);
            }
        }
        sb.append(']');
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
        if(i < 0) {
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
        buffer[offset] = (char) DIGITS_TENS[number];
        buffer[offset + 1] = (char) DIGIT_ONES[number];
    }

    private static void appendTwoDigitsInternal(int number, @NotNull StringBuilder sb) {
        sb.append((char) DIGITS_TENS[number]);
        sb.append((char) DIGIT_ONES[number]);
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

    public static void writeAsciiBytes(@NotNull String str, byte @NotNull [] outBuffer, int offset) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            byte mapped;
            if (c > 0x7f) {
                mapped = '?';
            } else {
                mapped = (byte) c;
            }

            outBuffer[offset + i] = mapped;
        }
    }
}
