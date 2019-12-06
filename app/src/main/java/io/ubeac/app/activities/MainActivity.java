package io.ubeac.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import com.blankj.utilcode.util.SPUtils;
import io.ubeac.app.R;
import io.ubeac.app.adapters.DrawerAdapter;
import io.ubeac.app.hardware.HardwareManager;
import io.ubeac.app.hardware.StatusManager;
import io.ubeac.app.helpers.StringHelper;
import io.ubeac.app.listeners.DrawerClickListener;
import io.ubeac.app.models.DrawerItem;
import io.ubeac.app.models.DrawerItemType;
import io.ubeac.app.models.SensorViewHolder;
import io.ubeac.app.services.BackgroundService;
import io.ubeac.app.services.listeners.SignalListener;
import io.ubeac.app.services.models.Destination;
import io.ubeac.app.services.models.Signal;
import io.ubeac.app.services.models.SignalType;
import io.ubeac.app.utils.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {

    private HardwareManager sensorService = null;
    private LinearLayout sensorGroup;
    private DrawerLayout drawerLayout;
    private boolean hasLocationPermission = false;
    private AppCompatActivity mainView;
    private ConcurrentHashMap<Integer, SensorViewHolder> sensorViews = new ConcurrentHashMap<>();
    private StatusManager hardwareStatus = StatusManager.getInstance();
    private BackgroundService bgService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorService = HardwareManager.getInstance(this);
        mainView = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(Constants.APP_SUBTITLE);
        drawerLayout = findViewById(R.id.drawer_layout);
        sensorGroup = findViewById(R.id.hardware);
        bgService = new BackgroundService();
        setSupportActionBar(toolbar);
        setupNavigationDrawer(toolbar, drawerLayout);
        createSensorView(Constants.LOCATION_TYPE, Constants.LOCATION_TEXT);
        createSensorsView();
        requestLocationPermission();
    }

    private void createSensorsView() {
        List<Sensor> sensors = sensorService.getAvailableSensors();
        for (Sensor sensor : sensors)
            createSensorView(sensor);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_LOCATION);
        } else {
            hasLocationPermission = true;
        }
    }

    private void onMenuItemClicked(DrawerItemType type) {
        drawerLayout.closeDrawer(GravityCompat.START);
        Intent intent = null;
        switch (type) {
            case Setting:
                intent = new Intent(getBaseContext(), SettingActivity.class);
                break;
            case About:
                intent = new Intent(getBaseContext(), AboutActivity.class);
                break;
        }
        if (intent != null)
            startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void setupNavigationDrawer(Toolbar toolbar, DrawerLayout drawerLayout) {
        try {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                    mainView,
                    drawerLayout,
                    toolbar,
                    R.string.drawer_open,
                    R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    mainView.invalidateOptionsMenu();
                    syncState();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    mainView.invalidateOptionsMenu();
                    syncState();
                }
            };
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            actionBarDrawerToggle.syncState();
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            DrawerAdapter adapter = new DrawerAdapter(this);
            List<DrawerItem> items = new ArrayList<>();
            items.add(new DrawerItem(Constants.MENU_SENSORS, R.drawable.ic_memory_24dp, 0, DrawerItemType.Home));
            items.add(new DrawerItem(Constants.MENU_SETTING, R.drawable.ic_settings_24dp, 0, DrawerItemType.Setting));
            items.add(new DrawerItem(Constants.MENU_ABOUT, R.drawable.ic_help_24dp, 0, DrawerItemType.About));
            adapter.setItems(items);
            adapter.setDrawerClickListener(new DrawerClickListener() {
                @Override
                public void OnClick(DrawerItemType type) {
                    onMenuItemClicked(type);
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        } catch (Exception ex) {
            ex.getStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void createSensorView(final Integer type, String title) {
        View view = View.inflate(getBaseContext(), R.layout.hardware_view, null);
        sensorGroup.addView(view);
        final boolean enabled = hardwareStatus.getEnabled(type);
        SensorViewHolder holder = new SensorViewHolder(view);
        holder.enabled.setChecked(enabled);
        holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                hardwareStatus.setEnabled(type, isChecked);
                HashMap<String, Serializable> data = new HashMap<>();
                data.put(Constants.SENSOR_TYPE, type);
                data.put(Constants.ENABLED, isChecked);
                bgService.sendSignal(Destination.Service, new Signal(SignalType.SensorEnabled, data));
            }
        });
        sensorViews.put(type, holder);
        holder.name.setText(title);
    }

    private void createSensorView(final Sensor sensor) {
        createSensorView(sensor.getType(), StringHelper.getTitleCaseName(sensor.getStringType()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true;
                HashMap<String, Serializable> data = new HashMap<>();
                boolean locationEnabled = hardwareStatus.getEnabled(Constants.LOCATION_TYPE);
                data.put(Constants.ENABLED, locationEnabled);
                bgService.sendSignal(Destination.Service, new Signal(SignalType.SensorEnabled, data));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bgService.unbind(getBaseContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!SPUtils.getInstance().getBoolean(Constants.SETTING_BG_SERVICE_ENABLED, false)) {
            bgService.sendSignal(Destination.Service, new Signal(SignalType.Exit, null));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START, false);
        }
        bgService.setSignalListener(new SignalListener() {
            @Override
            public void onSignal(Signal signal) {
                onIncomingSignal(signal);
            }
        });
        bgService.bind(getBaseContext(), BackgroundService.class);
    }

    private void onIncomingSignal(Signal signal) {
        Map data = null;
        if (signal.getData() != null)
            data = (Map) signal.getData().get(Constants.DATA);
        switch (signal.getType()) {
            case Exit:
                finish();
                break;
            case Registered:
                onRegistered();
            case Data:
                onSensorData(data);
                break;
        }
    }

    private void onSensorData(Map data) {
        if (data == null)
            return;
        StringBuilder text = new StringBuilder();
        for (Map.Entry<Integer, SensorViewHolder> holder : sensorViews.entrySet()) {
            Hashtable<String, Serializable> value = (Hashtable<String, Serializable>) data.get(holder.getKey());
            if (value != null) {
                for (Map.Entry<String, Serializable> item : value.entrySet()) {
                    text.append(item.getKey()).append(Constants.SEPARATOR).append(item.getValue())
                            .append(Constants.NEW_LINE);
                }
                holder.getValue().value.setText(text);
                text.setLength(0);
            }
        }
    }

    private void onRegistered() {
        bgService.sendSignal(Destination.Service, new Signal(SignalType.Setting, getSettings()));
        List<Sensor> items = sensorService.getAvailableSensors();
        for (final Sensor sensor : items) {
            HashMap<String, Serializable> sensorData = new HashMap<>();
            sensorData.put(Constants.SENSOR_TYPE, sensor.getType());
            sensorData.put(Constants.ENABLED, hardwareStatus.getEnabled(sensor.getType()));
            bgService.sendSignal(Destination.Service, new Signal(SignalType.SensorEnabled, sensorData));
        }
        if (hasLocationPermission) {
            HashMap<String, Serializable> locationData = new HashMap<>();
            locationData.put(Constants.SENSOR_TYPE, Constants.LOCATION_TYPE);
            locationData.put(Constants.ENABLED, hardwareStatus.getEnabled(Constants.LOCATION_TYPE));
            bgService.sendSignal(Destination.Service, new Signal(SignalType.SensorEnabled, locationData));
        }
    }

    private HashMap<String, Serializable> getSettings() {
        HashMap<String, Serializable> setting = new HashMap<>();
        setting.put(Constants.SETTING_ENABLED, SPUtils.getInstance()
                .getBoolean(Constants.SETTING_ENABLED, false));
        setting.put(Constants.SETTING_URL, SPUtils.getInstance()
                .getString(Constants.SETTING_URL, null));
        setting.put(Constants.SETTING_DEVICE, SPUtils.getInstance()
                .getString(Constants.SETTING_DEVICE, null));
        setting.put(Constants.SETTING_SAMPLING_PERIOD, SPUtils.getInstance()
                .getInt(Constants.SETTING_SAMPLING_PERIOD, 1000));
        setting.put(Constants.SETTING_TRANSMISSION_PERIOD, SPUtils.getInstance()
                .getInt(Constants.SETTING_TRANSMISSION_PERIOD, 1000));
        return setting;
    }

}
