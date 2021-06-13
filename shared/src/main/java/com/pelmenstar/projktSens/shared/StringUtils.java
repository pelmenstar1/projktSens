package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a helper class that efficiently build strings
 */
public final class StringUtils {
    private StringUtils() {}

    /**
     * Appends given array of {@link Object}'s to the end of {@link StringBuilder} in format: <br/>
     * - if specified array is null, appends 'null' string
     *  - otherwise, appends [$1st element$, $2nd element$, $3rd element$, ...]
     * @param array array to append to {@link StringBuilder}
     * @param sb receiver
     */
    public static void appendArray(@Nullable Object @Nullable [] array, @NotNull StringBuilder sb) {
        if(array == null) {
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
     *  - otherwise, appends [$1st element$, $2nd element$, $3rd element$, ...]
     * @param array array to append to {@link StringBuilder}
     * @param sb receiver
     */
    public static void appendArray(@Nullable AppendableToStringBuilder @Nullable [] array, @NotNull StringBuilder sb) {
        if(array == null) {
            appendNull(sb);
        } else {
            sb.append('[');
            if (array.length > 0) {
                int endIdx = array.length - 1;

                for (int i = 0; i < endIdx; i++) {
                    AppendableToStringBuilder element = array[i];
                    if(element == null) {
                        appendNull(sb);
                    } else {
                        element.append(sb);
                    }

                    sb.append(',');
                }

                AppendableToStringBuilder last = array[endIdx];
                if(last == null) {
                    appendNull(sb);
                } else {
                    last.append(sb);
                }
            }
            sb.append(']');
        }
    }

    /**
     * Appends given array of bytes to the end of {@link StringBuilder} in format: <br/>
     * - if specified array is null, appends 'null' string
     *  - otherwise, appends [$1st element$, $2nd element$, $3rd element$, ...]
     * @param array array to append to {@link StringBuilder}
     * @param sb receiver
     */
    public static void appendArray(byte @Nullable [] array, @NotNull StringBuilder sb) {
        if(array == null) {
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

    private static void appendNull(@NotNull StringBuilder sb) {
        sb.ensureCapacity(sb.length() + 4);
        sb.append('n');
        sb.append('u');
        sb.append('l');
        sb.append('l');
    }

    /**
     * Returns string representation of specified float number, that will be previously rounded to 1 decimal place
     */
    @NotNull
    public static String toStringRound1(float number) {
        StringBuilder sb = new StringBuilder();
        if(number < 0) {
            number = -number;
            sb.append('-');
        }

        int i = (int)number;
        int fr1 = (int)((number - i) * 10f);

        sb.append(i);

        sb.append('.');
        sb.append((char) ('0' + fr1));

        return sb.toString();
    }

    /**
     * Appends number to {@link StringBuilder}, but represented with its sign.
     * {@link StringBuilder#append(int)} on non-negative numbers appends number without its '+' sign.
     */
    public static void appendSigned(int number, @NotNull StringBuilder sb) {
        if(number > 0) {
            sb.append('+');
        }

        sb.append(number);
    }

    /**
     * Appends number to {@link StringBuilder}, but represented with its sign.
     * {@link StringBuilder#append(float)} on non-negative numbers appends number without its '+' sign.
     */
    public static void appendSigned(float number, @NotNull StringBuilder sb) {
        if(number > 0) {
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
        if(number < 0 || number >= 100) {
            throw new IllegalArgumentException("number=" + number + ". Number must to be in range [0; 99]");
        }

        if(number < 10) {
            sb.append('0');
            sb.append((char)('0' + number));
        } else {
            int d2 = number / 10;

            sb.append((char)('0' + d2));
            sb.append((char) ('0' + (number - (d2 * 10))));
        }
    }

    public static void writeTwoDigits(char @NotNull [] buffer, int offset, int number) {
        if(number < 0 || number > 99) {
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
        if(number < 0 || number > 9999) {
            throw new IllegalArgumentException("number=" + number + ". Number must be in range [0; 9999]");
        }

        if (number < 10) {
            sb.append('0');
            sb.append('0');
            sb.append('0');
            sb.append((char)('0' + number));
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
        if(number < 0 || number > 9999) {
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
}
