package io.ubeac.app.hardware;

import com.blankj.utilcode.util.SPUtils;
import io.ubeac.app.util.Constants;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class HardwareStatus {

    private static HardwareStatus instance;
    private ConcurrentHashMap<Integer, Boolean> sensors = new ConcurrentHashMap<>();
    private Boolean locationEnabled = null;

    private HardwareStatus() {

    }

    public static HardwareStatus getInstance() {
        if (instance == null)
            instance = new HardwareStatus();
        return instance;
    }

    public boolean getEnabled(Integer sensorType) {
        Boolean enabled = sensors.get(sensorType);
        if (enabled == null) {
            if (!SPUtils.getInstance(Constants.SENSOR).contains(sensorType.toString())) {
                SPUtils.getInstance(Constants.SENSOR).put(sensorType.toString(), true);
                enabled = true;
            } else {
                enabled = SPUtils.getInstance(Constants.SENSOR).getBoolean(sensorType.toString());
            }
        }
        return enabled;
    }

    public boolean getLocationEnabled() {
        if (locationEnabled == null) {
            if (!SPUtils.getInstance(Constants.LOCATION).contains(Constants.ENABLED)) {
                SPUtils.getInstance(Constants.LOCATION).put(Constants.ENABLED, true);
                locationEnabled = true;
            } else {
                locationEnabled = SPUtils.getInstance(Constants.LOCATION).getBoolean(Constants.ENABLED);
            }
        }
        return locationEnabled;
    }

    public void setLocationEnabled(Boolean locationEnabled) {
        this.locationEnabled = locationEnabled;
    }

    public void setEnabled(Integer sensorType, boolean enabled) {
        SPUtils.getInstance(Constants.SENSOR).put(sensorType.toString(), enabled);
        sensors.put(sensorType, enabled);
    }
}
