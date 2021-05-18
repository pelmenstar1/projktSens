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