package it.unipd.milan.padovaquest.shared_quests.data.location

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import it.unipd.milan.padovaquest.shared_quests.domain.model.LocationStatus
import it.unipd.milan.padovaquest.shared_quests.domain.repo.LocationClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

class LocationClientImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationClient {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var isLocationEnabled: Boolean = false

    private lateinit var locationCallback: LocationCallback
    private var lastLocation: Location? = null

    private val locationProviderChangedReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION == intent.action) {
                val curIsLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (curIsLocationEnabled == isLocationEnabled)
                    return

                isLocationEnabled = curIsLocationEnabled
                if (isLocationEnabled)
                    locationFlow.tryEmit(LocationStatus(lastLocation, true))
                else
                    locationFlow.tryEmit(LocationStatus(null, false))
            }
        }
    }

    private val locationFlow = MutableSharedFlow<LocationStatus>(replay = 1)


    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(interval: Long) {
        isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        context.registerReceiver(locationProviderChangedReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        locationFlow.tryEmit(LocationStatus(lastLocation, isLocationEnabled))
        val locationRequest = LocationRequest.Builder(interval)
            .setMinUpdateDistanceMeters(5f)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    lastLocation = location
                    locationFlow.tryEmit(LocationStatus(location, isLocationEnabled))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun emitAgain() {
        locationFlow.tryEmit(LocationStatus(lastLocation, isLocationEnabled))
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(): Flow<LocationStatus> {
        return locationFlow.asSharedFlow()
    }

    override fun getLastLocationStatus(): LocationStatus =
        LocationStatus(lastLocation, isLocationEnabled)

    override fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        context.unregisterReceiver(locationProviderChangedReceiver)
    }


}