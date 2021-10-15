package com.pelmenstar.projktSens.serverProtocol;

import org.jetbrains.annotations.NotNull;

/**
 * Contains constant commands.
 */
public final class Commands {
    private Commands() {
    }

    /**
     * Makes repo-server to return instance of day report
     */
    public static final int GET_DAY_REPORT = 1;

    /**
     * Makes repo-server to return instance of day range report
     */
    public static final int GET_DAY_RANGE_REPORT = 2;

    /**
     * Makes repo-server to get available date range that contains repository
     */
    public static final int GET_AVAILABLE_DATE_RANGE = 3;

    /**
     * Makes repo-server to return last weather in data store
     */
    public static final int GET_LAST_WEATHER = 4;

    public static final int GET_NEXT_WEATHER_TIME = 5;

    private static final int MAX_COMMAND = GET_NEXT_WEATHER_TIME;

    private static final @NotNull String @NotNull [] COMMAND_NAMES = new String[]{
            "GET_DAY_REPORT",
            "GET_DAY_RANGE_REPORT",
            "GET_AVAILABLE_DATE_RANGE",
            "GET_LAST_WEATHER",
            "GET_NEXT_WEATHER_TIME"
    };

    public static int fromString(@NotNull String commandName) {
        for (int i = 0; i < COMMAND_NAMES.length; i++) {
            if (COMMAND_NAMES[i].equalsIgnoreCase(commandName)) {
                return i + 1;
            }
        }

        throw new IllegalArgumentException("Invalid command name '" + commandName + "'");
    }

    /**
     * Returns string representation of command integer
     *
     * @param command command
     * @return string representation of {@code command}
     * @throws IllegalArgumentException if {@code command} is invalid
     */
    @NotNull
    public static String toString(int command) {
        if (command <= 0 || command > MAX_COMMAND) {
            throw new IllegalArgumentException("command");
        }

        return COMMAND_NAMES[command - 1];
    }
}
