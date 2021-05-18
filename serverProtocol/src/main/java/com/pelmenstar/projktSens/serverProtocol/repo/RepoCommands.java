package com.pelmenstar.projktSens.serverProtocol.repo;

import org.jetbrains.annotations.NotNull;

/**
 * Contains constant commands for repo-server.
 */
public final class RepoCommands {
    private RepoCommands() {}

    /**
     * Makes repo-server to return instance of day report
     */
    public static final int GEN_DAY_REPORT = 1;

    /**
     * Makes repo-server to return instance of day range report
     */
    public static final int GEN_DAY_RANGE_REPORT = 2;

    /**
     * Makes repo-server to get available date range that contains repository
     */
    public static final int GET_AVAILABLE_DATE_RANGE = 3;

    /**
     * Makes repo-server to return last weather in data store
     */
    public static final int GET_LAST_WEATHER = 4;

    /**
     * Returns string representation of command integer
     * @param command command
     * @return string representation of {@code command}
     *
     * @throws IllegalArgumentException if {@code command} is invalid
     */
    @NotNull
    public static String toString(int command) {
        switch (command) {
            case GEN_DAY_REPORT: return "GEN_DAY_REPORT";
            case GEN_DAY_RANGE_REPORT: return "GEN_DAY_RANGE_REPORT";
            case GET_AVAILABLE_DATE_RANGE: return "GET_AVAILABLE_DATE_RANGE";
            case GET_LAST_WEATHER: return "GET_LAST_WEATHER";

            default: throw new IllegalArgumentException("command");
        }
    }
}
