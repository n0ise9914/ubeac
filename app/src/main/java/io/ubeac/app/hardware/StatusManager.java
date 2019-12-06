package io.ubeac.app.hardware;

import com.blankj.utilcode.util.SPUtils;
import io.ubeac.app.utils.Constants;

import java.util.concurrent.ConcurrentHashMap;

public class StatusManager {

    private static StatusManager instance;
    private ConcurrentHashMap<Integer, Boolean> sensors = new ConcurrentHashMap<>();

    private StatusManager() {

    }

    public static StatusManager getInstance() {
        if (instance == null)
            instance = new StatusManager();
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

    public void setEnabled(Integer sensorType, boolean enabled) {
        SPUtils.getInstance(Constants.SENSOR).put(sensorType.toString(), enabled);
        sensors.put(sensorType, enabled);
    }
}
