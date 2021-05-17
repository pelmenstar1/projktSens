package com.pelmenstar.projktSens.serverProtocol.repo

import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.shared.*
import java.lang.StringBuilder

/**
 * Represents all possible types of response returned by repo-server
 */
sealed class RepoResponse: AppendableToStringBuilder() {
    /**
     * Empty response
     */
    object Empty: RepoResponse() {
        override fun append(sb: StringBuilder) {
            sb.append("{Empty}")
        }

        override fun equals(other: Any?): Boolean {
            return other === Empty
        }

        override fun hashCode(): Int {
            return 31
        }
    }

    /**
     * Response with error.
     * This class has private constructor, so static methods [Companion.error] should be used
     */
    class Error internal constructor(val error: Int): RepoResponse() {
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

        override fun hashCode(): Int {
            return error
        }
    }

    /**
     * Response with value paired with its class.
     * This class has private constructor, so static methods [Companion.ok] should be used
     */
    class Ok<T:Any> internal constructor(val value: T, val valueClass: Class<T>): RepoResponse() {
        override fun append(sb: StringBuilder) {
            sb.append("{ Value=")
            sb.append(value)
            sb.append("; ValueClass=")
            sb.append(valueClass)
            sb.append('}')
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                value == o.value && valueClass == o.valueClass
            }
        }

        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + valueClass.hashCode()

            return result
        }
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
         * Creates response with value and its class
         *
         * @param value value of response
         * @param valueClass class of [value]
         */
        fun<T:Any> ok(value: T, valueClass: Class<T>): Ok<T> {
            return Ok(value, valueClass)
        }

        /**
         * Creates response with value and its class.
         * Note that generic parameter of this method is marked as reified,
         * so type of [value] should be known in compile-time
         *
         * @param value value of response
         */
        inline fun<reified T:Any> ok(value: T): Ok<T> {
            return ok(value, T::class.java)
        }

        /**
         * If [value] is null, returns [Empty] response, otherwise, returns [Ok] response
         * Note that generic parameter of this method is marked as reified,
         * so type of [value] should be known in compile-time
         */
        inline fun<reified T:Any> okOrEmpty(value: T?): RepoResponse {
            return if(value != null) ok(value) else Empty
        }
    }
}