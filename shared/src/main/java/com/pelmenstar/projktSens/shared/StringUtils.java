package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a helper class that efficiently build strings
 */
public final class StringUtils {
    private static final byte[] digits = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9'
    };

    private static final byte [] DigitTens = {
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
    } ;

    private static final byte [] DigitOnes = {
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
    } ;

    private static final char[] hexBuffer = new char[8];

    private StringUtils() {
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

            if (c >= '0' && c <= '9') {
                n = n * 10 + (c - '0');
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
                    if (element == null) {
                        appendNull(sb);
                    } else {
                        element.append(sb);
                    }

                    sb.append(',');
                }

                AppendableToStringBuilder last = array[endIdx];
                if (last == null) {
                    appendNull(sb);
                } else {
                    last.append(sb);
                }
            }
            sb.append(']');
        }
    }

    private static void appendNull(@NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + 4);
        sb.append('n');
        sb.append('u');
        sb.append('l');
        sb.append('l');
    }

    public static int getBufferSizeForRoundedFloat(float number) {
        int intPart = (int)number;
        int sign = 0;
        if(intPart < 0) {
            sign = 1;
            intPart = -intPart;
        }

        return MyMath.decimalDigitCount(intPart) + sign + 2;
    }

    public static int getBufferSizeForSignedRoundedFloat(float number) {
       return MyMath.decimalDigitCount((int)number) + 3;
    }

    public static void writeSignedFloatRound1(float number, char @NotNull [] buffer, int offset) {
        buffer[offset] = number >= 0 ? '+' : '-';
        writeAbsoluteFloatRound1(Math.abs(number), buffer, offset + 1);
    }

    public static void writeFloatRound1(float number, char @NotNull [] buffer, int offset) {
        if(number < 0) {
            buffer[offset++] = '-';
        }

        writeAbsoluteFloatRound1(Math.abs(number), buffer, offset);
    }

    private static void writeAbsoluteFloatRound1(float number, char @NotNull [] buffer, int offset) {
        int intPart = (int)number;
        int frDigit = (int)((number - intPart) * 10f);

        int digitCount = MyMath.decimalDigitCount(intPart);
        int intPartLastIndex = offset + digitCount;
        getCharsForPositive(intPart, buffer, intPartLastIndex);
        buffer[intPartLastIndex] = '.';
        buffer[intPartLastIndex + 1] = (char)('0' + frDigit);
    }

    // Stolen from Android 30 source
    private static void getCharsForPositive(int i, char @NotNull [] buffer, int bIndex) {
        int q, r;
        int charPos = bIndex;

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buffer [--charPos] = (char)DigitOnes[r];
            buffer [--charPos] = (char)DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        do {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buffer[--charPos] = (char)digits[r];
            i = q;
        } while (i != 0);
    }

    /**
     * Appends number to {@link StringBuilder}, but represented with its sign.
     * {@link StringBuilder#append(int)} on non-negative numbers appends number without its '+' sign.
     */
    public static void appendSigned(int number, @NotNull StringBuilder sb) {
        if (number > 0) {
            sb.append('+');
        }

        sb.append(number);
    }

    /**
     * Appends number to {@link StringBuilder}, but represented with its sign.
     * {@link StringBuilder#append(float)} on non-negative numbers appends number without its '+' sign.
     */
    public static void appendSigned(float number, @NotNull StringBuilder sb) {
        if (number > 0) {
            sb.append('+');
        }

        sb.append(number);
    }

    @NotNull
    public static String twoDigits(int number) {
        char[] buffer = new char[2];

        writeTwoDigits(buffer, 0, number);

        return new String(buffer, 0, 2);
    }

    public static void appendTwoDigits(int number, @NotNull StringBuilder sb) {
        if (number < 0 || number >= 100) {
            throw new IllegalArgumentException("number=" + number + ". Number must to be in range [0; 99]");
        }

        if (number < 10) {
            sb.append('0');
            sb.append((char) ('0' + number));
        } else {
            int d2 = number / 10;

            sb.append((char) ('0' + d2));
            sb.append((char) ('0' + (number - (d2 * 10))));
        }
    }

    public static void writeTwoDigits(char @NotNull [] buffer, int offset, int number) {
        if (number < 0 || number > 99) {
            throw new IllegalArgumentException("number=" + number + ". Number must to be in range [0; 99]");
        }

        if (number < 10) {
            buffer[offset] = '0';
            buffer[offset + 1] = (char) ('0' + number);
        } else {
            int d2 = number / 10;

            buffer[offset] = (char) ('0' + d2);
            buffer[offset + 1] = (char) ('0' + (number - (d2 * 10)));
        }
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

        if (number < 10) {
            sb.append('0');
            sb.append('0');
            sb.append('0');
            sb.append((char) ('0' + number));
        } else if (number < 100) {
            int d2 = number / 10;

            sb.append('0');
            sb.append('0');
            sb.append((char) ('0' + d2));
            sb.append((char) ('0' + (number - (d2 * 10))));
        } else if (number < 1000) {
            int d3 = number / 100;

            number -= (d3 * 100);

            int d2 = number / 10;

            sb.append('0');
            sb.append((char) ('0' + d3));
            sb.append((char) ('0' + d2));
            sb.append((char) ('0' + (number - (d2 * 10))));
        } else {
            int d4 = number / 1000;

            number -= (d4 * 1000);

            int d3 = number / 100;

            number -= (d3 * 100);

            int d2 = number / 10;

            sb.append((char) ('0' + d4));
            sb.append((char) ('0' + d3));
            sb.append((char) ('0' + d2));
            sb.append((char) ('0' + (number - (d2 * 10))));
        }
    }

    public static void writeFourDigits(char @NotNull [] buffer, int offset, int number) {
        if (number < 0 || number > 9999) {
            throw new IllegalArgumentException("number=" + number + ". Number must be in range [0; 9999]");
        }

        if (number < 10) {
            buffer[offset] = '0';
            buffer[offset + 1] = '0';
            buffer[offset + 2] = '0';
            buffer[offset + 3] = (char) ('0' + number);
        } else if (number < 100) {
            int d2 = number / 10;

            buffer[offset] = '0';
            buffer[offset + 1] = '0';
            buffer[offset + 2] = (char) ('0' + d2);
            buffer[offset + 3] = (char) ('0' + (number - (d2 * 10)));
        } else if (number < 1000) {
            int d3 = number / 100;

            number -= (d3 * 100);

            int d2 = number / 10;

            buffer[offset] = '0';
            buffer[offset + 1] = (char) ('0' + d3);
            buffer[offset + 2] = (char) ('0' + d2);
            buffer[offset + 3] = (char) ('0' + (number - (d2 * 10)));
        } else {
            int d4 = number / 1000;

            number -= (d4 * 1000);

            int d3 = number / 100;

            number -= (d3 * 100);

            int d2 = number / 10;

            buffer[offset] = (char) ('0' + d4);
            buffer[offset + 1] = (char) ('0' + d3);
            buffer[offset + 2] = (char) ('0' + d2);
            buffer[offset + 3] = (char) ('0' + (number - (d2 * 10)));
        }
    }

    public static void writeByte(char @NotNull [] buffer, int offset, int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("value");
        }

        if (value < 10) {
            buffer[offset] = (char) ('0' + value);
        } else if (value < 100) {
            int d2 = value / 10;

            buffer[offset] = (char) ('0' + d2);
            buffer[offset + 1] = (char) ('0' + (value - (d2 * 10)));
        } else {
            int d3 = value / 100;

            value -= (d3 * 100);

            int d2 = value / 10;

            buffer[offset] = (char) ('0' + d3);
            buffer[offset + 1] = (char) ('0' + d2);
            buffer[offset + 2] = (char) ('0' + (value - (d2 * 10)));
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
