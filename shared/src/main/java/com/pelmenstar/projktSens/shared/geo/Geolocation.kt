package com.pelmenstar.projktSens.shared.geo

import com.pelmenstar.projktSens.shared.equalsPattern

/**
 * A data class which stores particular location described by latitude and longitude.
 */
class Geolocation(latitude: Float, longitude: Float) {
    val latitude: Float
    val longitude: Float

    init {
        if(latitude !in -90f..90f) {
            throw IllegalArgumentException("latitude=$latitude")
        }

        if(longitude !in -180f..180f) {
            throw IllegalArgumentException("longitude=$longitude")
        }

        this.latitude = latitude
        this.longitude = longitude
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o -> latitude == o.latitude && longitude == o.longitude }
    }

    override fun hashCode(): Int {
        return latitude.toBits() xor longitude.toBits()
    }

    override fun toString(): String {
        return "{latitude=$latitude, longitude=$longitude}"
    }
}