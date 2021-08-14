package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder
import com.pelmenstar.projktSens.shared.equalsPattern

/**
 * Represents all possible types of response
 */
sealed class Response: AppendableToStringBuilder() {
    /**
     * Empty response
     */
    object Empty: Response() {
        override fun append(sb: StringBuilder) {
            sb.append("{Empty}")
        }

        override fun equals(other: Any?): Boolean = other === Empty
        override fun hashCode(): Int = 31
    }

    /**
     * Response with error.
     * This class has private constructor, so static methods [Companion.error] should be used
     */
    class Error internal constructor(val error: Int): Response() {
        override fun append(sb: StringBuilder) {
            sb.append("{Error=")
            sb.append(Errors.toString(error))
            sb.append('}')
        }

        override fun equals(other: Any?): Boolean {
           return equalsPattern(other) { o ->
               error == o.error
           }
        }

        override fun hashCode(): Int = error
    }

    /**
     * Response with value paired with its class.
     * This class has private constructor, so static methods [Companion.ok] should be used
     */
    class Ok<T:Any> internal constructor(val value: T): Response() {
        override fun append(sb: StringBuilder) {
            sb.append("{ Value=")
            sb.append(value)
            sb.append('}')
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                value == o.value
            }
        }

        override fun hashCode(): Int = value.hashCode()
    }

    fun isEmpty(): Boolean {
        return Empty === this
    }

    companion object {
        /**
         * Creates error response
         *
         * @param error error of response
         */
        fun error(error: Int): Error {
            return Error(error)
        }

        /**
         * Creates error response
         *
         * @param e [Exception] that will be converted to error id
         */
        fun error(e: Exception): Error {
            return Error(Errors.exceptionToError(e))
        }

        /**
         * Creates OK response
         *
         * @param value value of response
         */
        fun<T:Any> ok(value: T): Ok<T> {
            return Ok(value)
        }

        /**
         * If [value] is null, returns [Empty] response, otherwise, returns [Ok] response
         * Note that generic parameter of this method is marked as reified,
         * so type of [value] should be known in compile-time
         */
        fun<T:Any> okOrEmpty(value: T?): Response {
            return if(value != null) Ok(value) else Empty
        }
    }
}