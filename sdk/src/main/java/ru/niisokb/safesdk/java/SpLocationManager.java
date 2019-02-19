package ru.niisokb.safesdk.java;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Criteria;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.List;

import androidx.annotation.RequiresApi;

/**
 * LocationManager который учитывает настройки ограничений GPS. Перед использованием необходимо
 * инициализировать класс при помощи [load]. А также для применения конфигураций должен быть
 * загружен SpConfigDispatcher
 */
@SuppressWarnings({"AccessStaticViaInstance", "unused"})
public class SpLocationManager {
    public static String GPS_RESTRICTION_KEY = "safephone.location.gps_enabled";

    private static ru.niisokb.safesdk.SpLocationManager instance =
            ru.niisokb.safesdk.SpLocationManager.INSTANCE;

    public static String NETWORK_PROVIDER = instance.NETWORK_PROVIDER;
    public static String GPS_PROVIDER = instance.GPS_PROVIDER;
    public static String PASSIVE_PROVIDER = instance.PASSIVE_PROVIDER;
    public static String KEY_PROXIMITY_ENTERING = instance.KEY_PROXIMITY_ENTERING;
    public static String KEY_STATUS_CHANGED = instance.KEY_STATUS_CHANGED;
    public static String KEY_PROVIDER_ENABLED = instance.KEY_PROVIDER_ENABLED;
    public static String KEY_LOCATION_CHANGED = instance.KEY_LOCATION_CHANGED;
    public static String PROVIDERS_CHANGED_ACTION = instance.PROVIDERS_CHANGED_ACTION;
    public static String MODE_CHANGED_ACTION = instance.MODE_CHANGED_ACTION;

    public SpLocationManager load(Context context) {
        instance.load(context);
        return this;
    }

    public boolean isInitialized() {
        return instance.isInitialized();
    }

    public boolean isGpsAccessEnabled() {
        return instance.isGpsAccessEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean addNmeaListener(OnNmeaMessageListener listener, Handler handler) {
        return instance.addNmeaListener(listener, handler);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean addNmeaListener(OnNmeaMessageListener listener) {
        return instance.addNmeaListener(listener);
    }

    public static void addProximityAlert(double latitude, double longitude,
                                         float radius, long expiration, PendingIntent intent) {
        instance.addProximityAlert(latitude, longitude, radius, expiration, intent);
    }

    public static void addTestProvider(String name, boolean requiresNetwork,
                                       boolean requiresSatellite, boolean requiresCell,
                                       boolean hasMonetaryCost, boolean supportsAltitude,
                                       boolean supportsSpeed, boolean supportsBearing,
                                       int powerRequirement, int accuracy) {
        instance.addTestProvider(name, requiresNetwork,
                requiresSatellite, requiresCell,
                hasMonetaryCost, supportsAltitude,
                supportsSpeed, supportsBearing, powerRequirement,
                accuracy);
    }

    public static void clearTestProviderEnabled(String provider) {
        instance.clearTestProviderEnabled(provider);
    }

    public static void clearTestProviderLocation(String provider) {
        instance.clearTestProviderLocation(provider);
    }

    public static void clearTestProviderStatus(String provider) {
        instance.clearTestProviderStatus(provider);
    }

    public static List<String> getAllProviders() {
        return instance.getAllProviders();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static String getGnssHardwareModelName() {
        return instance.getGnssHardwareModelName();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static int getGnssYearOfHardware() {
        return instance.getGnssYearOfHardware();
    }

    public static GpsStatus getGpsStatus(GpsStatus status) {
        return instance.getGpsStatus(status);
    }

    public static Location getLastKnownLocation(String provider) {
        return instance.getLastKnownLocation(provider);
    }

    public static LocationProvider getProvider(String name) {
        return instance.getProvider(name);
    }

    public static List<String> getProviders(boolean enabledOnly) {
        return instance.getProviders(enabledOnly);
    }

    public static List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        return instance.getProviders(criteria, enabledOnly);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static boolean isLocationEnabled() {
        return instance.isLocationEnabled();
    }

    public static boolean isProviderEnabled(String provider) {
        return instance.isProviderEnabled(provider);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean registerGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback) {
        return instance.registerGnssMeasurementsCallback(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean registerGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback, Handler handler) {
        return instance.registerGnssMeasurementsCallback(callback, handler);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean registerGnssNavigationMessageCallback(GnssNavigationMessage.Callback callback, Handler handler) {
        return instance.registerGnssNavigationMessageCallback(callback, handler);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean registerGnssNavigationMessageCallback(GnssNavigationMessage.Callback callback) {
        return instance.registerGnssNavigationMessageCallback(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean registerGnssStatusCallback(GnssStatus.Callback callback) {
        return instance.registerGnssStatusCallback(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean registerGnssStatusCallback(GnssStatus.Callback callback, Handler handler) {
        return instance.registerGnssStatusCallback(callback, handler);
    }

    public static void removeGpsStatusListener(GpsStatus.Listener listener) {
        instance.removeGpsStatusListener(listener);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void removeNmeaListener(OnNmeaMessageListener listener) {
        instance.removeNmeaListener(listener);
    }

    public static void removeProximityAlert(PendingIntent intent) {
        instance.removeProximityAlert(intent);
    }

    public static void removeTestProvider(String provider) {
        instance.removeTestProvider(provider);
    }

    public static void removeUpdates(LocationListener listener) {
        instance.removeUpdates(listener);
    }

    public static void removeUpdates(PendingIntent intent) {
        instance.removeUpdates(intent);
    }

    public static void requestLocationUpdates(String provider, long minTime,
                                              float minDistance, LocationListener listener) {
        instance.requestLocationUpdates(provider, minTime, minDistance, listener);
    }

    public static void requestLocationUpdates(String provider, long minTime, float minDistance,
                                              LocationListener listener, Looper looper) {
        instance.requestLocationUpdates(provider, minTime, minDistance, listener, looper);
    }

    public static void requestLocationUpdates(long minTime, float minDistance, Criteria criteria,
                                              PendingIntent intent) {
        instance.requestLocationUpdates(minTime, minDistance, criteria, intent);
    }

    public static void requestLocationUpdates(String provider, long minTime, float minDistance,
                                              PendingIntent intent) {
        instance.requestLocationUpdates(provider, minTime, minDistance, intent);
    }

    public static void requestSingleUpdate(String provider, PendingIntent intent) {
        instance.requestSingleUpdate(provider, intent);
    }

    public static void requestSingleUpdate(Criteria criteria, PendingIntent intent) {
        instance.requestSingleUpdate(criteria, intent);
    }

    public static void requestSingleUpdate(String provider, LocationListener listener, Looper looper) {
        instance.requestSingleUpdate(provider, listener, looper);
    }

    public static void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper) {
        instance.requestSingleUpdate(criteria, listener, looper);
    }

    public static void setTestProviderEnabled(String provider, boolean enabled) {
        instance.setTestProviderEnabled(provider, enabled);
    }

    public static void setTestProviderLocation(String provider, Location loc) {
        instance.setTestProviderLocation(provider, loc);
    }

    public static void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime) {
        instance.setTestProviderStatus(provider, status, extras, updateTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void unregisterGnssMeasurementsCallback(GnssMeasurementsEvent.Callback callback) {
        instance.unregisterGnssMeasurementsCallback(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void unregisterGnssNavigationMessageCallback(GnssNavigationMessage.Callback callback) {
        instance.unregisterGnssNavigationMessageCallback(callback);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void unregisterGnssStatusCallback(GnssStatus.Callback callback) {
        instance.unregisterGnssStatusCallback(callback);
    }
}
