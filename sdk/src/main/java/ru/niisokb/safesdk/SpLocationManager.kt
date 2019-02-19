@file:Suppress("unused", "DEPRECATION")

package ru.niisokb.safesdk

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import ru.niisokb.safesdk.SpLocationManager.load
import ru.niisokb.safesdk.configuration.ConfigAttr
import ru.niisokb.safesdk.mixins.ConfigurableModule
import ru.niisokb.safesdk.configuration.ValType

/**
 * LocationManager, который учитывает настройки ограничений GPS. Перед использованием необходимо
 * инициализировать класс при помощи [load].
 */
@Suppress("MemberVisibilityCanBePrivate")
object SpLocationManager : ConfigurableModule {

    private const val TAG = "SpLocationManager"
    private const val missingMethodExceptionMessage =
            "This method is not available, try to update library to the latest version."

    const val GPS_ENABLED_KEY = "safephone.restrictions.gps_enabled"
    const val GPS_ENABLED_DEFAULT = true

    override val attrs = listOf(ConfigAttr(GPS_ENABLED_KEY, ValType.Boolean, GPS_ENABLED_DEFAULT))
    override val dispatch: (Bundle) -> Unit = { config ->
        gpsEnabled = config.getBoolean(GPS_ENABLED_KEY, GPS_ENABLED_DEFAULT)
    }

    init {
        register()
    }

    private var gpsEnabled = GPS_ENABLED_DEFAULT
    private var initialized = false
    private lateinit var locationManager: LocationManager

    private val gpsDisabledException =
            SpLocationManagerException.GpsDisabled("Gps disabled.")
    private val notInitializedException =
            SpLocationManagerException.NotInitialized("Location manager is not initialized.")


    /** @see LocationManager.NETWORK_PROVIDER */
    const val NETWORK_PROVIDER = "network"

    /** @see LocationManager.GPS_PROVIDER */
    const val GPS_PROVIDER = "gps"

    /** @see LocationManager.PASSIVE_PROVIDER */
    const val PASSIVE_PROVIDER = "passive"

    /** @see LocationManager.KEY_PROXIMITY_ENTERING */
    const val KEY_PROXIMITY_ENTERING = "entering"

    /** @see LocationManager.KEY_STATUS_CHANGED */
    const val KEY_STATUS_CHANGED = "status"

    /** @see LocationManager.KEY_PROVIDER_ENABLED */
    const val KEY_PROVIDER_ENABLED = "providerEnabled"

    /** @see LocationManager.KEY_LOCATION_CHANGED */
    const val KEY_LOCATION_CHANGED = "location"

    /** @see LocationManager.PROVIDERS_CHANGED_ACTION */
    const val PROVIDERS_CHANGED_ACTION = "android.location.PROVIDERS_CHANGED"

    /** @see LocationManager.MODE_CHANGED_ACTION */
    const val MODE_CHANGED_ACTION = "android.location.MODE_CHANGED"

    /** Инициализирует [SpLocationManager]. */
    fun load(context: Context): SpLocationManager {
        if (!initialized) {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            initialized = true
        }

        return this
    }

    fun isInitialized() = initialized

    fun isGpsAccessEnabled() = gpsEnabled

    /** @see LocationManager.addNmeaListener */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun addNmeaListener(listener: OnNmeaMessageListener, handler: Handler): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }

        return try {
            locationManager.addNmeaListener(listener, handler)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.addNmeaListener */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    fun addNmeaListener(listener: OnNmeaMessageListener): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }

        return try {
            locationManager.addNmeaListener(listener)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.addProximityAlert */
    @SuppressLint("MissingPermission")
    fun addProximityAlert(latitude: Double, longitude: Double, radius: Float,
                          expiration: Long, intent: PendingIntent) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }

        try {
            locationManager.addProximityAlert(latitude, longitude, radius, expiration, intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.addTestProvider */
    fun addTestProvider(name: String, requiresNetwork: Boolean, requiresSatellite: Boolean,
                        requiresCell: Boolean, hasMonetaryCost: Boolean, supportsAltitude: Boolean,
                        supportsSpeed: Boolean, supportsBearing: Boolean, powerRequirement: Int,
                        accuracy: Int) {
        if (!initialized) {
            throw notInitializedException
        }

        try {
            locationManager.addTestProvider(name, requiresNetwork, requiresSatellite,
                    requiresCell, hasMonetaryCost, supportsAltitude,
                    supportsSpeed, supportsBearing, powerRequirement, accuracy)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)

        }
    }

    /** @see LocationManager.clearTestProviderEnabled */
    fun clearTestProviderEnabled(provider: String) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.clearTestProviderEnabled(provider)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)

        }

    }

    /** @see LocationManager.clearTestProviderLocation */
    fun clearTestProviderLocation(provider: String) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.clearTestProviderLocation(provider)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.clearTestProviderStatus */
    fun clearTestProviderStatus(provider: String) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.clearTestProviderLocation(provider)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.getAllProviders */
    fun getAllProviders(): List<String> {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.allProviders
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            ArrayList()
        }
    }

    /** @see LocationManager.getBestProvider */
    fun getBestProvider(criteria: Criteria, enabledOnly: Boolean): String {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.getBestProvider(criteria, enabledOnly)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            ""
        }
    }

    /** @see LocationManager.getGnssHardwareModelName */
    @RequiresApi(Build.VERSION_CODES.P)
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun getGnssHardwareModelName(): String {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.gnssHardwareModelName
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            ""
        }

    }

    /** @see LocationManager.getGnssYearOfHardware */
    @RequiresApi(Build.VERSION_CODES.P)
    fun getGnssYearOfHardware(): Int {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.gnssYearOfHardware
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            -1
        }
    }

    /** @see LocationManager.getGpsStatus */
    @SuppressLint("MissingPermission")
    fun getGpsStatus(status: GpsStatus): GpsStatus {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.getGpsStatus(status)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            status
        }
    }

    /** @see LocationManager.getLastKnownLocation */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(provider: String): Location {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.getLastKnownLocation(provider)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            Location(provider)
        }
    }

    /** @see LocationManager.getProvider */
    fun getProvider(name: String): LocationProvider? {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.getProvider(name)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            null
        }
    }

    /** @see LocationManager.getProvider */
    fun getProviders(enabledOnly: Boolean): List<String> {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.getProviders(enabledOnly)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            ArrayList()
        }
    }

    /** @see LocationManager.getProviders */
    fun getProviders(criteria: Criteria, enabledOnly: Boolean): List<String> {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.getProviders(criteria, enabledOnly)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            ArrayList()
        }
    }

    /** @see LocationManager.isLocationEnabled */
    @RequiresApi(Build.VERSION_CODES.P)
    fun isLocationEnabled(): Boolean {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.isLocationEnabled
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.isProviderEnabled */
    fun isProviderEnabled(provider: String): Boolean {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.isProviderEnabled(provider)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.registerGnssMeasurementsCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun registerGnssMeasurementsCallback(callback: GnssMeasurementsEvent.Callback): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }

        return try {
            locationManager.registerGnssMeasurementsCallback(callback)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.registerGnssMeasurementsCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun registerGnssMeasurementsCallback(callback: GnssMeasurementsEvent.Callback,
                                         handler: Handler): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.registerGnssMeasurementsCallback(callback, handler)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.registerGnssMeasurementsCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun registerGnssNavigationMessageCallback(callback: GnssNavigationMessage.Callback,
                                              handler: Handler): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.registerGnssNavigationMessageCallback(callback, handler)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.registerGnssNavigationMessageCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    fun registerGnssNavigationMessageCallback(callback: GnssNavigationMessage.Callback): Boolean {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.registerGnssNavigationMessageCallback(callback)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.registerGnssStatusCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun registerGnssStatusCallback(callback: GnssStatus.Callback): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.registerGnssStatusCallback(callback)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.registerGnssStatusCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun registerGnssStatusCallback(callback: GnssStatus.Callback, handler: Handler): Boolean {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }

        return try {
            locationManager.registerGnssStatusCallback(callback, handler)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.removeGpsStatusListener */
    fun removeGpsStatusListener(listener: GpsStatus.Listener) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.removeGpsStatusListener(listener)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.removeNmeaListener */
    @RequiresApi(Build.VERSION_CODES.N)
    fun removeNmeaListener(listener: OnNmeaMessageListener) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.removeNmeaListener(listener)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.removeProximityAlert */
    @SuppressLint("MissingPermission")
    fun removeProximityAlert(intent: PendingIntent) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.removeProximityAlert(intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.removeTestProvider */
    fun removeTestProvider(provider: String) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.removeTestProvider(provider)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.removeUpdates */
    @SuppressLint("MissingPermission")
    fun removeUpdates(listener: LocationListener) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.removeUpdates(listener)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.removeUpdates */
    fun removeUpdates(intent: PendingIntent) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.removeUpdates(intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestLocationUpdates */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(provider: String, minTime: Long,
                               minDistance: Float, listener: LocationListener) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestLocationUpdates(provider, minTime, minDistance, listener)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestLocationUpdates */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(minTime: Long, minDistance: Float, criteria: Criteria,
                               listener: LocationListener, looper: Looper) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestLocationUpdates(minTime, minDistance,
                    criteria, listener, looper)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestLocationUpdates */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(provider: String, minTime: Long, minDistance: Float,
                               listener: LocationListener, looper: Looper) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestLocationUpdates(provider, minTime, minDistance,
                    listener, looper)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestLocationUpdates */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(minTime: Long, minDistance: Float,
                               criteria: Criteria, intent: PendingIntent) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestLocationUpdates(minTime, minDistance, criteria, intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestLocationUpdates */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(provider: String, minTime: Long,
                               minDistance: Float, intent: PendingIntent) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestLocationUpdates(provider, minTime, minDistance, intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestSingleUpdate */
    @SuppressLint("MissingPermission")
    fun requestSingleUpdate(provider: String, intent: PendingIntent) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestSingleUpdate(provider, intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestSingleUpdate */
    @SuppressLint("MissingPermission")
    fun requestSingleUpdate(criteria: Criteria, intent: PendingIntent) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestSingleUpdate(criteria, intent)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.requestSingleUpdate */
    @SuppressLint("MissingPermission")
    fun requestSingleUpdate(provider: String, listener: LocationListener, looper: Looper) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestSingleUpdate(provider, listener, looper)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.requestSingleUpdate */
    @SuppressLint("MissingPermission")
    fun requestSingleUpdate(criteria: Criteria, listener: LocationListener, looper: Looper) {
        if (!gpsEnabled) {
            throw gpsDisabledException
        }
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.requestSingleUpdate(criteria, listener, looper)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }

    }

    /** @see LocationManager.sendExtraCommand */
    fun sendExtraCommand(provider: String, command: String, extras: Bundle): Boolean {
        if (!initialized) {
            throw notInitializedException
        }
        return try {
            locationManager.sendExtraCommand(provider, command, extras)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
            false
        }
    }

    /** @see LocationManager.setTestProviderEnabled */
    fun setTestProviderEnabled(provider: String, enabled: Boolean) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.setTestProviderEnabled(provider, enabled)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.setTestProviderLocation */
    fun setTestProviderLocation(provider: String, loc: Location) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.setTestProviderLocation(provider, loc)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.setTestProviderStatus */
    fun setTestProviderStatus(provider: String, status: Int, extras: Bundle, updateTime: Long) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.setTestProviderStatus(provider, status, extras, updateTime)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.unregisterGnssMeasurementsCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    fun unregisterGnssMeasurementsCallback(callback: GnssMeasurementsEvent.Callback) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.unregisterGnssMeasurementsCallback(callback)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.unregisterGnssNavigationMessageCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    fun unregisterGnssNavigationMessageCallback(callback: GnssNavigationMessage.Callback) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.unregisterGnssNavigationMessageCallback(callback)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }

    /** @see LocationManager.unregisterGnssStatusCallback */
    @RequiresApi(Build.VERSION_CODES.N)
    fun unregisterGnssStatusCallback(callback: GnssStatus.Callback) {
        if (!initialized) {
            throw notInitializedException
        }
        try {
            locationManager.unregisterGnssStatusCallback(callback)
        } catch (e: NoSuchMethodException) {
            Log.w(TAG, missingMethodExceptionMessage)
        }
    }
}

sealed class SpLocationManagerException(msg: String) : RuntimeException(msg) {
    class GpsDisabled(msg: String) : SpLocationManagerException(msg)
    class NotInitialized(msg: String) : SpLocationManagerException(msg)
}
