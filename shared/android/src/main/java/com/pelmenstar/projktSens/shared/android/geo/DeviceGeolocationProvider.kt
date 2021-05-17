package com.pelmenstar.projktSens.shared.android.geo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import com.pelmenstar.projktSens.shared.geo.Geolocation
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicReference

/**
 * The implementation of [GeolocationProvider] which takes [Geolocation] from a user device
 */
@SuppressLint("VisibleForTests")
class DeviceGeolocationProvider(context: Context) : GeolocationProvider {
    private val client = FusedLocationProviderClient(context)

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun getLastLocation(): Geolocation {
        return suspendCancellableCoroutine { cont ->
            val callback =  object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    client.removeLocationUpdates(this)

                    val l = result.lastLocation
                    val geolocation = Geolocation(l.latitude.toFloat(), l.longitude.toFloat())
                    cont.resumeWith(Result.success(geolocation))
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    if (!availability.isLocationAvailable && cont.isActive) {
                        cont.resumeWith(Result.failure(RuntimeException("Cannot receive geolocation")))
                    }
                }
            }
            cont.invokeOnCancellation {
                client.removeLocationUpdates(callback)
            }

            client.requestLocationUpdates(createRequest(), callback, Looper.getMainLooper())
        }
    }

    private fun createRequest(): LocationRequest {
        return LocationRequest.create()
            .setNumUpdates(1)
            .setWaitForAccurateLocation(false)
            .setInterval(1000)
    }
}