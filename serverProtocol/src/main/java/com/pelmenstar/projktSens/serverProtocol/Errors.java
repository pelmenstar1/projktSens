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

    private Errors() {
    }

    /**
     * If {@code e} is {@link IOException} returns {@link Errors#IO},
     * if {@code e} is {@link SQLException} returns {@link Errors#INTERNAL_DB_ERROR},
     * otherwise {@link Errors#UNKNOWN}
     *
     */
    public static int exceptionToError(@NotNull Exception e) {
        if(e instanceof IOException) {
            return IO;
        } else if(e instanceof SQLException) {
            return INTERNAL_DB_ERROR;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Returns string representation of {@code error}
     * @param error error integer
     */
    @NotNull
    public static String toString(int error) {
        switch (error) {
            case INVALID_ARGUMENTS:
                return "INVALID_ARGUMENTS";
            case INVALID_COMMAND:
                return "INVALID_COMMAND";
            case INTERNAL_DB_ERROR:
                return "INTERNAL_DB_ERROR";
            case IO:
                return "IO";
            case UNKNOWN:
                return "UNKNOWN";
            default:
                return "NONE";
        }
    }
}
