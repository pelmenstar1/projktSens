package com.pelmenstar.projktSens.serverProtocol

/**
 * Describe all possible server statuses
 */

enum class ServerStatus {
    AVAILABLE,
    NOT_AVAILABLE;

    override fun toString(): String {
        return if(this === AVAILABLE) {
            "<Available>"
        } else {
            "<NotAvailable>"
        }
    }
}

/*
sealed class ServerStatus(@JvmField val byteValue: Byte) {
    object Available : ServerStatus(1) {
        override fun toString(): String {
            return "<Available>"
        }
    }

    object NotAvailable : ServerStatus(0) {
        override fun toString(): String {
            return "<Not available>"
        }
    }
}
*/