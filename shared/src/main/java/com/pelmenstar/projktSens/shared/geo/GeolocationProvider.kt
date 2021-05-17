package com.pelmenstar.projktSens.shared.geo

interface GeolocationProvider {
    suspend fun getLastLocation(): Geolocation
}