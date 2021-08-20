package com.pelmenstar.projktSens.serverProtocol;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contains available error ids
 */
public final class Errors {
    /**
     * Signals that no error was happened
     */
    public static final int NONE = 0;

    /**
     * Signals that unknown error was happened
     */
    public static final int UNKNOWN = 1;

    /**
     * Signals that given arguments was invalid
     */
    public static final int INVALID_ARGUMENTS = 2;

    /**
     * Signals that given command was invalid
     */
    public static final int INVALID_COMMAND = 3;

    /**
     * Signals that some kind of database error was happened
     */
    public static final int INTERNAL_DB_ERROR = 4;

    /**
     * Signals that some kind of IO error was happened
     */
    public static final int IO = 5;

    /**
     * Made for clients to mark that server's response was not valid
     */
    public static final int INVALID_RESPONSE = 6;

    private static final String[] ERROR_NAMES = new String[]{
            "NONE",
            "UNKNOWN",
            "INVALID_ARGUMENTS",
            "INVALID_COMMAND",
            "INTERNAL_DB_ERROR",
            "IO",
            "INVALID_RESPONSE"
    };

    private Errors() {
    }

    /**
     * If {@code e} is {@link IOException} returns {@link Errors#IO},
     * if {@code e} is {@link SQLException} returns {@link Errors#INTERNAL_DB_ERROR},
     * otherwise {@link Errors#UNKNOWN}
     */
    public static int exceptionToError(@NotNull Exception e) {
        if (e instanceof IOException) {
            return IO;
        } else if (e instanceof SQLException) {
            return INTERNAL_DB_ERROR;
        } else {
            return UNKNOWN;
        }
    }

    public static int fromString(@NotNull String name) {
        for (int i = 0; i < ERROR_NAMES.length; i++) {
            if (ERROR_NAMES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }

        return UNKNOWN;
    }

    /**
     * Returns string representation of {@code error}
     *
     * @param error error integer
     */
    @NotNull
    public static String toString(int error) {
        if (error < 0 || error >= ERROR_NAMES.length) {
            throw new IllegalArgumentException("error");
        }

        return ERROR_NAMES[error];
    }
}
