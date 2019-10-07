package io.ubeac.app.hardware;

import android.content.Context;
import android.hardware.*;
import io.ubeac.app.util.Constants;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SensorService {
    private static SensorService instance;
    private List<Integer> triggerSensors = new ArrayList<Integer>(Arrays.asList(24, 25, 17));
    private SensorManager manager;
    private ConcurrentHashMap<Integer, float[]> data;
    private int samplingPeriod;
    private ConcurrentHashMap<Integer, TriggerEventListener> triggerEventListener = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, SensorEventListener> listeners = new ConcurrentHashMap<>();

    private SensorService(Context context) {
        data = new ConcurrentHashMap<>();
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public static SensorService getInstance(Context context) {
        if (instance == null)
            instance = new SensorService(context);
        return instance;
    }

    public List<Sensor> getAvailable() {
        List<Sensor> sensors = manager.getSensorList(Sensor.TYPE_ALL);
        List<Sensor> available = new ArrayList<>();
        for (Sensor sensor : sensors) {
            if (manager.getDefaultSensor(sensor.getType()) != null)
                available.add(sensor);
        }
        return available;
    }

    private void sensorChanged(Sensor sensor, TriggerEvent event) {
        sensorChanged(sensor, event.values);
    }

    private void sensorChanged(Sensor sensor, SensorEvent event) {
        sensorChanged(sensor, event.values);
    }

    private void sensorChanged(Sensor sensor, float[] values) {
        try {
            int i = values.length - 1;
            int j = 0;
            while (i > 0 && values[i] == 0) {
                j++;
                i--;
            }
            values = Arrays.copyOf(values, values.length - j);
            data.put(sensor.getType(), values);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void unbind(final int sensorType) {
        try {
            if (triggerSensors.contains(sensorType)) {
                TriggerEventListener listener = triggerEventListener.get(sensorType);
                if (listener != null) {
                    manager.cancelTriggerSensor(listener, manager.getDefaultSensor(sensorType));
                    triggerSensors.remove(sensorType);
                }
            }
            if (listeners.containsKey(sensorType)) {
                manager.unregisterListener(listeners.get(sensorType));
                listeners.remove(sensorType);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void bind(final int sensorType) {
        try {
            if (listeners.containsKey(sensorType))
                return;
            final Sensor sensor = manager.getDefaultSensor(sensorType);
            if (triggerSensors.contains(sensorType)) {
                TriggerEventListener listener = new TriggerEventListener() {
                    @Override
                    public void onTrigger(TriggerEvent event) {
                        sensorChanged(sensor, event);
                    }
                };
                manager.requestTriggerSensor(listener, sensor);
                triggerEventListener.put(sensorType, listener);
            } else {
                SensorEventListener listener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        sensorChanged(sensor, event);
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
                manager.registerListener(listener, sensor, samplingPeriod);
                listeners.put(sensorType, listener);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setSamplingPeriod(int samplingPeriod) {
        this.samplingPeriod = (int) TimeUnit.NANOSECONDS.convert(samplingPeriod, TimeUnit.MILLISECONDS);
        HashMap<Integer, SensorEventListener> listeners = new HashMap<>(this.listeners);
        for (Integer sensorType : this.listeners.keySet()) {
            unbind(sensorType);
        }
        for (Integer sensorType : listeners.keySet()) {
            bind(sensorType);
        }
    }

    public HashMap<Integer, float[]> getData() {
        HashMap<Integer, float[]> data = null;
        try {
            data = new HashMap<>(this.data);
            this.data.clear();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public HashMap<String, Serializable> convertData(HashMap<Integer, float[]> data) {
        HashMap<String, Serializable> result = new HashMap<>();
        for (Map.Entry<Integer, float[]> item : data.entrySet()) {
            result.put(manager.getDefaultSensor(item.getKey()).getName().replace(Constants.PSH, "")
                    .trim().replace(" ", "_").toLowerCase(), item.getValue());
        }
        return result;
    }

}
