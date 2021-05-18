package com.pelmenstar.projktSens.shared.geo

/**
 * Implementation of [GeolocationProvider] that returns only one, constant [Geolocation]
 */
class ConstGeolocationProvider: GeolocationProvider {
    private val location: Geolocation

    /**
     * Initializes instance of [ConstGeolocationProvider] using latitude and longitude
     *
     * @param latitude latitude, must be in range of [-90; 90]
     * @param longitude longitude, must be in range of [-180; 180]
     */
    constructor(latitude: Float, longitude: Float) {
        location = Geolocation(latitude, longitude)
    }

    /**
     * Creates instance of [ConstGeolocationProvider] using specified [location]
     */
    constructor(location: Geolocation) {
        this.location = location
    }

    override suspend fun getLastLocation() = location
}