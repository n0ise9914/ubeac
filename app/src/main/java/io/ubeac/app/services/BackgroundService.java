package io.ubeac.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.webkit.URLUtil;
import io.ubeac.app.R;
import io.ubeac.app.activities.MainActivity;
import io.ubeac.app.hardware.models.Frame;
import io.ubeac.app.hardware.models.Record;
import io.ubeac.app.hardware.HardwareManager;
import io.ubeac.app.network.Api;
import io.ubeac.app.network.models.Packet;
import io.ubeac.app.network.models.SensorDetail;
import io.ubeac.app.services.models.Destination;
import io.ubeac.app.services.models.Signal;
import io.ubeac.app.services.models.SignalType;
import io.ubeac.app.utils.Constants;

import java.io.Serializable;
import java.util.*;

public class BackgroundService extends BaseService {
    private HardwareManager sensorService = null;
    private Api api = null;
    private Timer transmitterTimer = null, uiTimer = null;
    private String deviceId = null;

    @Override
    public void onSignal(Signal signal) {
        if (signal == null)
            return;
        SignalType signalType = signal.getType();
        HashMap<String, Serializable> data = signal.getData();
        switch (signalType) {
            case Exit:
                forceStop();
                break;
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
        }
    }

    private void onSettingChanged(HashMap<String, Serializable> data) {
        deviceId = (String) data.get(Constants.SETTING_DEVICE);
        String url = (String) data.get(Constants.SETTING_URL);
        boolean enabled = (boolean) data.get(Constants.SETTING_ENABLED);
        int samplingPeriod = (int) data.get(Constants.SETTING_SAMPLING_PERIOD);
        int transmissionPeriod = (int) data.get(Constants.SETTING_TRANSMISSION_PERIOD);
        sensorService.setSamplingPeriod(samplingPeriod);
        api = Api.getInstance(url);
        boolean isTransmitting = false;
        if (enabled && URLUtil.isValidUrl(url) && deviceId != null) {
            isTransmitting = true;
            restartTransmitterTimer(transmissionPeriod);
        } else
            stopTimer(transmitterTimer);
        restartUiTimer(isTransmitting);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();
        sensorService = HardwareManager.getInstance(this);
    }

    private void stopTimer(Timer timer) {
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopTimer(uiTimer);
        return super.onUnbind(intent);
    }

    private void restartUiTimer(final boolean isTransmitting) {
        stopTimer(uiTimer);
        uiTimer = new Timer();
        uiTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (subscriber != null) {
                    updateUi(sensorService.getData());
                    if (!isTransmitting)
                        sensorService.clearFrames();
                }
            }
        }, Constants.UPDATE_UI_PERIOD, Constants.UPDATE_UI_PERIOD);
    }

    private void restartTransmitterTimer(int transmissionPeriod) {
        stopTimer(transmitterTimer);
        transmitterTimer = new Timer();
        transmitterTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Hashtable<Integer, Record> data = sensorService.getData();
                sensorService.clearOldFrames();
                transmitData(data);
            }
        }, transmissionPeriod, transmissionPeriod);
    }

    private void transmitData(Hashtable<Integer, Record> items) {
        List<SensorDetail> sensors = new ArrayList<>();
        String name;
        for (Map.Entry<Integer, Record> item : items.entrySet()) {
            name = item.getValue().getName();
            for (Frame frame : item.getValue().getFrames()) {
                sensors.add(new SensorDetail(name, frame.getTimestamp(), frame.getType(), frame.getUnit(), frame.getPrefix(), frame.getData()));
            }
        }
        api.sendData(new Packet(deviceId, sensors));
    }

    private void updateUi(Hashtable<Integer, Record> items) {
        Hashtable<Integer, Serializable> result = new Hashtable<>();
        List<Frame> dataItems;
        for (Map.Entry<Integer, Record> item : items.entrySet()) {
            dataItems = item.getValue().getFrames();
            if (dataItems.size() != 0)
                result.put(item.getKey(), dataItems.get(dataItems.size() - 1).getData());
        }
        HashMap<String, Serializable> signalData = new HashMap<>();
        signalData.put(Constants.DATA, result);
        sendSignal(Destination.Activity, new Signal(SignalType.Data, signalData));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            if (intent.getAction() != null) {
                SignalType type = SignalType.valueOf(intent.getAction());
                switch (type) {
                    case Exit:
                        forceStop();
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

    private void forceStop() {
        stopForeground(true);
        sensorService.unbindAll();
        stopTimer(transmitterTimer);
        stopTimer(uiTimer);
        sendSignal(Destination.Activity, new Signal(SignalType.Exit, null));
        stopSelf();
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, "Background Service", NotificationManager.IMPORTANCE_DEFAULT));
        }
        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
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
