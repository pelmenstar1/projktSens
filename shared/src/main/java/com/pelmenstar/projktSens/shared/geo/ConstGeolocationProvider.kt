package com.pelmenstar.projktSens.shared.geo

class ConstGeolocationProvider: GeolocationProvider {
    private val location: Geolocation

    constructor(latitude: Float, longitude: Float) {
        location = Geolocation(latitude, longitude)
    }

    constructor(location: Geolocation) {
        this.location = location
    }

    override suspend fun getLastLocation() = location
}