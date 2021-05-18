package com.pelmenstar.projktSens.shared.geo

/**
 * A provider interface for [Geolocation]
 */
interface GeolocationProvider {
    /**
     * Returns last [Geolocation] of object chosen by implementation
     */
    suspend fun getLastLocation(): Geolocation
}