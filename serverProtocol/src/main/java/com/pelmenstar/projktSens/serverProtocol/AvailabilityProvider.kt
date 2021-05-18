package com.pelmenstar.projktSens.serverProtocol

/**
 * Provides information about current status of servers
 */
interface AvailabilityProvider {
    /**
     * Returns status of servers
     */
    suspend fun getStatus(): ServerStatus
}