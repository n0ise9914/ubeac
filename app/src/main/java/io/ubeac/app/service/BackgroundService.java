package io.ubeac.app.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import io.ubeac.app.R;
import io.ubeac.app.features.home.MainActivity;
import io.ubeac.app.hardware.LocationService;
import io.ubeac.app.hardware.SensorService;
import io.ubeac.app.network.Api;
import io.ubeac.app.service.models.Destination;
import io.ubeac.app.service.models.Signal;
import io.ubeac.app.service.models.SignalType;
import io.ubeac.app.util.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends BaseService {
    private SensorService sensorService = null;
    private LocationService locationService = null;
    private Api api = null;
    private Timer timer = null;

    @Override
    public void onSignal(Signal signal) {
        if (signal == null)
            return;
        SignalType signalType = signal.getType();
        HashMap<String, Serializable> data = signal.getData();
        switch (signalType) {
            case Setting:
                onSettingChanged(data);
                break;
            case SensorEnabled:
                int sensorType = (int) data.get(Constants.SENSOR_TYPE);
                boolean sensorEnabled = (boolean) data.get(Constants.ENABLED);
                if (sensorEnabled) {
                    sensorService.bind(sensorType);
                } else {
                    sensorService.unbind(sensorType);
                }
                break;
            case LocationEnabled:
                boolean locationEnabled = (boolean) data.get(Constants.ENABLED);
                if (locationEnabled) {
                    locationService.bind();
                } else {
                    locationService.unbind();
                }
                break;
        }
    }

    private void onSettingChanged(HashMap<String, Serializable> data) {
        String token = (String) data.get(Constants.SETTING_TOKEN);
        String protocol = (String) data.get(Constants.SETTING_PROTOCOL);
        boolean enabled = (boolean) data.get(Constants.SETTING_ENABLED);
        int samplingPeriod = (int) data.get(Constants.SETTING_SAMPLING_PERIOD);
        int transmissionPeriod = (int) data.get(Constants.SETTING_TRANSMISSION_PERIOD);
        sensorService.setSamplingPeriod(samplingPeriod);
        api = Api.getInstance(protocol, token);
        stopTimer();
        startTimer(enabled, transmissionPeriod);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();
        sensorService = SensorService.getInstance(this);
        locationService = LocationService.getInstance(this);
    }

    private void stopTimer() {
        if (timer != null)
            timer.cancel();
    }

    private void startTimer(final boolean transmissionEnabled, int transmissionPeriod) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                HashMap<Integer, float[]> sensorData = sensorService.getData();
                HashMap<String, Double> locationData = locationService.getData();
                updateUi(sensorData, locationData);
                if (transmissionEnabled)
                    transmitChanges(sensorData, locationData);
            }
        }, transmissionPeriod, transmissionPeriod);
    }

    private void transmitChanges(HashMap<Integer, float[]> sensorData, HashMap<String, Double> locationData) {
        HashMap<String, Serializable> transmissionData = new HashMap<>();
        transmissionData.putAll(sensorService.convertData(sensorData));
        transmissionData.putAll(locationData);
        api.sendData(transmissionData);
    }

    private void updateUi(HashMap<Integer, float[]> sensorData, HashMap<String, Double> locationData) {
        if (sensorData.size() > 0) {
            HashMap<String, Serializable> signalSensorData = new HashMap<>();
            signalSensorData.put(Constants.DATA, sensorData);
            sendSignal(Destination.Activity, new Signal(SignalType.SensorData, signalSensorData));
        }
        if (locationData.size() > 0) {
            HashMap<String, Serializable> signalLocationData = new HashMap<>();
            signalLocationData.put(Constants.DATA, locationData);
            sendSignal(Destination.Activity, new Signal(SignalType.LocationData, signalLocationData));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            if (intent.getAction() != null) {
                SignalType type = SignalType.valueOf(intent.getAction());
                switch (type) {
                    case Exit:
                        exit();
                        break;
                    case StartMainActivity:
                        startMainActivity();
                        break;
                }
            }
        return START_STICKY;
    }

    private void startMainActivity() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
        Intent intent1 = new Intent(getBaseContext(), MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
    }

    private void exit() {
        sendSignal(Destination.Activity, new Signal(SignalType.Exit, null));
        stopForeground(true);
        stopSelf();
        if (timer != null)
            timer.cancel();
    }

    private void showNotification() {
        Notification notification = new NotificationCompat.Builder(this, Constants.BASE_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(createIntent(SignalType.StartMainActivity))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_close_black_24dp, Constants.CLOSE, createIntent(SignalType.Exit))
                .setContentTitle(Constants.NOTIFICATION_TITLE)
                .setContentText(Constants.NOTIFICATION_TEXT)
                .build();
        startForeground(100, notification);
    }

    private PendingIntent createIntent(SignalType command) {
        Intent intent = new Intent(this, BackgroundService.class);
        intent.setAction(command.toString());
        return PendingIntent.getService(this, 0, intent, 0);
    }
}
