package com.pelmenstar.projktSens.serverProtocol

interface AvailabilityProvider {
    suspend fun getStatus(): ServerStatus
}