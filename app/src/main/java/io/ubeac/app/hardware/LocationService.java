package io.ubeac.app.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import io.ubeac.app.util.Constants;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class LocationService {
    private static LocationService instance;
    private ConcurrentHashMap<String, Double> data;
    private LocationManager manager;
    private LocationListener listener;

    private LocationService(Context context) {
        data = new ConcurrentHashMap<>();
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static LocationService getInstance(Context context) {
        if (instance == null)
            instance = new LocationService(context);
        return instance;
    }

    public void unbind() {
        try {
            if (listener != null)
                manager.removeUpdates(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void bind() {
        try {
            Location location = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            update(location);
            listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    update(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void update(Location location) {
        data.put(Constants.LATITUDE, location.getLatitude());
        data.put(Constants.LONGITUDE, location.getLongitude());
        data.put(Constants.ALTITUDE, location.getAltitude());
    }

    public HashMap<String, Double> getData() {
        HashMap<String, Double> data = null;
        try {
            data = new HashMap<>(this.data);
            this.data.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

}
