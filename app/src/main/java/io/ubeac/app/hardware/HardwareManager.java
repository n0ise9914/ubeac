package io.ubeac.app.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ubeac.app.hardware.models.Frame;
import io.ubeac.app.hardware.models.SensorMapping;
import io.ubeac.app.hardware.models.Record;
import io.ubeac.app.helpers.FileHelper;
import io.ubeac.app.helpers.StringHelper;
import io.ubeac.app.utils.Constants;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HardwareManager {
    private static HardwareManager instance;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private ConcurrentHashMap<Integer, Object> listeners = new ConcurrentHashMap<>();
    private final Hashtable<Integer, Record> data;
    private int samplingPeriod;
    private LinkedHashMap<Integer, SensorMapping> sensors;
    private Gson gson = new Gson();

    private HardwareManager(Context context) {
        data = new Hashtable<>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        loadMappings(context);
    }

    private void loadMappings(Context context) {
        Type type = new TypeToken<LinkedHashMap<Integer, SensorMapping>>() {
        }.getType();
        sensors = gson.fromJson(FileHelper.loadJSONFromAsset(context, "sensors.json"), type);
    }

    public static HardwareManager getInstance(Context context) {
        if (instance == null)
            instance = new HardwareManager(context);
        return instance;
    }

    public List<Sensor> getAvailableSensors() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        List<Sensor> available = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (sensor.getType() <= 35 && this.sensors.containsKey(sensor.getType()) &&
                    sensorManager.getDefaultSensor(sensor.getType()) != null) {
                available.add(sensor);
            }
        }
        Collections.sort(available, new Comparator<Sensor>() {
            @Override
            public int compare(Sensor a, Sensor b) {
                return Integer.compare(a.getType(), b.getType());
            }
        });
        return available;
    }

    public void unbindAll() {
        synchronized (this.data) {
            for (Map.Entry<Integer, Record> item : new Hashtable<>(this.data).entrySet()) {
                unbind(item.getKey());
            }
        }
    }

    public void unbind(final int sensorType) {
        try {
            if (!listeners.containsKey(sensorType))
                return;
            this.data.remove(sensorType);
            final Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            Object listener = listeners.get(sensorType);
            if (sensorType == Constants.LOCATION_TYPE) {
                locationManager.removeUpdates((LocationListener) listener);
            } else if (sensor.getReportingMode() == Sensor.REPORTING_MODE_ONE_SHOT) {
                sensorManager.cancelTriggerSensor((TriggerEventListener) listener, sensorManager.getDefaultSensor(sensorType));
            } else {
                sensorManager.unregisterListener((SensorEventListener) listener);
            }
            listeners.remove(sensorType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void bind(final int sensorType) {
        try {
            if (listeners.containsKey(sensorType))
                return;
            Sensor sensor = sensorManager.getDefaultSensor(sensorType);
            if (sensorType == Constants.LOCATION_TYPE)
                bindLocation();
            if (sensor.getReportingMode() == Sensor.REPORTING_MODE_ONE_SHOT)
                bindTigerEvent(sensor, true);
            else
                bindSensorEvent(sensor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bindSensorEvent(final Sensor sensor) {
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                addData(sensor, event.timestamp, event.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(listener, sensor, samplingPeriod);
        listeners.put(sensor.getType(), listener);
    }

    @SuppressLint("MissingPermission")
    private void bindLocation() {
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            addData(location);
            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    addData(location);
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
            listeners.put(Constants.LOCATION_TYPE, listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bindTigerEvent(final Sensor sensor, boolean firstRequest) {
        TriggerEventListener listener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {
                addData(sensor, event.timestamp, event.values);
                bindTigerEvent(sensor, false);
            }
        };
        sensorManager.requestTriggerSensor(listener, sensor);
        if (firstRequest)
            listeners.put(sensor.getType(), listener);
    }

    public void setSamplingPeriod(int samplingPeriod) {
        this.samplingPeriod = (int) TimeUnit.NANOSECONDS.convert(samplingPeriod, TimeUnit.MILLISECONDS);
        ConcurrentHashMap<Integer, Object> listeners = new ConcurrentHashMap<>(this.listeners);
        for (Integer sensorType : this.listeners.keySet()) {
            unbind(sensorType);
        }
        for (Integer sensorType : listeners.keySet()) {
            bind(sensorType);
        }
    }

    private void addData(Location location) {
        synchronized (this.data) {
            Hashtable<String, Double> data = new Hashtable<>();
            data.put(Constants.LATITUDE, location.getLatitude());
            data.put(Constants.LONGITUDE, location.getLongitude());
            data.put(Constants.ALTITUDE, location.getAltitude());
            if (!this.data.containsKey(Constants.LOCATION_TYPE)) {
                this.data.put(Constants.LOCATION_TYPE, new Record(Constants.LOCATION_TEXT));
                SensorMapping mapping = sensors.get(Constants.LOCATION_TYPE);
                this.data.get(Constants.LOCATION_TYPE).getFrames().add(new Frame(System.currentTimeMillis(),
                        data, mapping.getType(), mapping.getUnit(), mapping.getPrefix()));
            } else
                this.data.get(Constants.LOCATION_TYPE).getFrames().add(new Frame(System.currentTimeMillis(), data));
        }
    }

    private void addData(Sensor sensor, long timestamp, float[] values) {
        synchronized (this.data) {
            try {
                Hashtable<String, Float> data = new Hashtable<>();
                SensorMapping mapping = sensors.get(sensor.getType());
                for (Map.Entry<Integer, String> value : mapping.getValues().entrySet())
                    data.put(value.getValue(), values[value.getKey()]);
                if (!this.data.containsKey(sensor.getType())) {
                    this.data.put(sensor.getType(), new Record(StringHelper.getSnakeCaseName(sensor.getStringType())));
                    this.data.get(sensor.getType()).getFrames().add(new Frame(timestamp,
                            data, mapping.getType(), mapping.getUnit(), mapping.getPrefix()));
                } else {
                    List<Frame> frames = this.data.get(sensor.getType()).getFrames();
                    if (frames.size() > 0 && frames.get(0).getTimestamp() == null)
                        frames.remove(0);
                    frames.add(new Frame(timestamp, data));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearOldFrames() {
        synchronized (this.data) {
            List<Frame> frames;
            for (Map.Entry<Integer, Record> item : data.entrySet()) {
                frames = item.getValue().getFrames();
                if (frames.size() > 1)
                    frames.subList(0, frames.size() - 1).clear();
                frames.get(0).setTimestamp(null);
            }
        }
    }

    public Hashtable<Integer, Record> getData() {
        synchronized (this.data) {
            return new Hashtable<>(this.data);
        }
    }

    public void clearFrames() {
        this.data.clear();
    }
}
