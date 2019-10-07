package io.ubeac.app.features.home;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.Toast;
import com.blankj.utilcode.util.SPUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ubeac.app.R;
import io.ubeac.app.features.about.AboutActivity;
import io.ubeac.app.features.setting.SettingActivity;
import io.ubeac.app.hardware.HardwareStatus;
import io.ubeac.app.hardware.SensorService;
import io.ubeac.app.service.*;
import io.ubeac.app.service.listeners.SignalListener;
import io.ubeac.app.service.models.Destination;
import io.ubeac.app.service.models.Signal;
import io.ubeac.app.service.models.SignalType;
import io.ubeac.app.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {

    private SensorService sensorService = null;
    private LinearLayout sensorGroup;
    private DrawerLayout drawerLayout;
    private boolean hasLocationPermission = false;
    private Gson gson = new GsonBuilder().create();
    private AppCompatActivity mainView;
    private ConcurrentHashMap<Integer, SensorViewHolder> sensorViews = new ConcurrentHashMap<>();
    private HardwareStatus hardwareStatus = HardwareStatus.getInstance();
    private SensorViewHolder locationView;
    private BackgroundService bgService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorService = SensorService.getInstance(this);
        mainView = this;
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(Constants.APP_SUBTITLE);
        drawerLayout = findViewById(R.id.drawer_layout);
        sensorGroup = findViewById(R.id.hardware);
        bgService = new BackgroundService();
        setSupportActionBar(toolbar);
        setupNavigationDrawer(toolbar, drawerLayout);
        createLocationView();
        createSensorsView();
        requestLocationPermission();
    }

    private void createSensorsView() {
        List<Sensor> sensors = sensorService.getAvailable();
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
            case setting:
                intent = new Intent(getBaseContext(), SettingActivity.class);
                break;
            case about:
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
            items.add(new DrawerItem(Constants.MENU_SENSORS, R.drawable.ic_memory_24dp, 0, DrawerItemType.home));
            items.add(new DrawerItem(Constants.MENU_SETTING, R.drawable.ic_settings_24dp, 0, DrawerItemType.setting));
            items.add(new DrawerItem(Constants.MENU_ABOUT, R.drawable.ic_help_24dp, 0, DrawerItemType.about));
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

    private void createLocationView() {
        View view = View.inflate(getBaseContext(), R.layout.hardware_view, null);
        sensorGroup.addView(view);
        if (!SPUtils.getInstance(Constants.LOCATION).contains(Constants.ENABLED)) {
            SPUtils.getInstance(Constants.LOCATION).put(Constants.ENABLED, true);
        }
        final boolean enabled = hardwareStatus.getLocationEnabled();
        SensorViewHolder holder = new SensorViewHolder(view);
        holder.enabled.setChecked(enabled);
        holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                hardwareStatus.setLocationEnabled(isChecked);
                HashMap<String, Serializable> data = new HashMap<>();
                data.put(Constants.ENABLED, isChecked);
                bgService.sendSignal(Destination.Service, new Signal(SignalType.LocationEnabled, data));
            }
        });
        locationView = holder;
        holder.name.setText(Constants.LOCATION.toLowerCase());
    }

    private void createSensorView(final Sensor sensor) {
        View view = View.inflate(getBaseContext(), R.layout.hardware_view, null);
        sensorGroup.addView(view);
        if (!SPUtils.getInstance(Constants.SENSOR).contains(sensor.getName())) {
            SPUtils.getInstance(Constants.SENSOR).put(sensor.getName(), true);
        }
        final boolean enabled = hardwareStatus.getEnabled(sensor.getType());
        SensorViewHolder holder = new SensorViewHolder(view);
        holder.enabled.setChecked(enabled);
        holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                hardwareStatus.setEnabled(sensor.getType(), isChecked);
                HashMap<String, Serializable> data = new HashMap<>();
                data.put(Constants.SENSOR_TYPE, sensor.getType());
                data.put(Constants.ENABLED, isChecked);
                bgService.sendSignal(Destination.Service, new Signal(SignalType.SensorEnabled, data));
            }
        });
        sensorViews.put(sensor.getType(), holder);
        holder.name.setText(sensor.getName().replace(Constants.PSH, ""));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true;
                HashMap<String, Serializable> data = new HashMap<>();
                data.put(Constants.ENABLED, hardwareStatus.getLocationEnabled());
                bgService.sendSignal(Destination.Service, new Signal(SignalType.LocationEnabled, data));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bgService.unbind(getBaseContext());
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
        HashMap data = null;
        if (signal.getData() != null)
            data = (HashMap) signal.getData().get(Constants.DATA);
        switch (signal.getType()) {
            case Exit:
                finish();
                break;
            case Registered:
                onRegistered(data);
            case SensorData:
                onSensorData(data);
                break;
            case LocationData:
                onLocationData(data);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void onLocationData(HashMap data) {
        if (data == null)
            return;
        locationView.value.setText(Constants.LATITUDE + Constants.SEPARATOR + data.get(Constants.LATITUDE) +
                Constants.NEW_LINE + Constants.LONGITUDE + Constants.SEPARATOR + data.get(Constants.LONGITUDE) +
                Constants.NEW_LINE + Constants.ALTITUDE + Constants.SEPARATOR + data.get(Constants.ALTITUDE));

    }

    private void onSensorData(HashMap data) {
        if (data == null)
            return;
        for (Map.Entry<Integer, SensorViewHolder> holder : sensorViews.entrySet()) {
            float[] value = (float[]) data.get(holder.getKey());
            String stValue = value == null ? "" : gson.toJson(value);
            holder.getValue().value.setText(stValue);
        }
    }

    private void onRegistered(HashMap data) {
        bgService.sendSignal(Destination.Service, new Signal(SignalType.Setting, getSettings()));
        List<Sensor> items = sensorService.getAvailable();
        for (final Sensor sensor : items) {
            HashMap<String, Serializable> sensorData = new HashMap<>();
            sensorData.put(Constants.SENSOR_TYPE, sensor.getType());
            sensorData.put(Constants.ENABLED, hardwareStatus.getEnabled(sensor.getType()));
            bgService.sendSignal(Destination.Service, new Signal(SignalType.SensorEnabled, sensorData));
        }
        if (hasLocationPermission) {
            HashMap<String, Serializable> locationData = new HashMap<>();
            locationData.put(Constants.ENABLED, hardwareStatus.getLocationEnabled());
            bgService.sendSignal(Destination.Service, new Signal(SignalType.LocationEnabled, locationData));
        }
    }

    private HashMap<String, Serializable> getSettings() {
        HashMap<String, Serializable> setting = new HashMap<>();
        setting.put(Constants.SETTING_ENABLED, SPUtils.getInstance()
                .getBoolean(Constants.SETTING_ENABLED, false));
        setting.put(Constants.SETTING_TOKEN, SPUtils.getInstance()
                .getString(Constants.SETTING_TOKEN, null));
        setting.put(Constants.SETTING_PROTOCOL, SPUtils.getInstance()
                .getString(Constants.SETTING_PROTOCOL, Constants.HTTPS));
        setting.put(Constants.SETTING_SAMPLING_PERIOD, SPUtils.getInstance()
                .getInt(Constants.SETTING_SAMPLING_PERIOD, 1000));
        setting.put(Constants.SETTING_TRANSMISSION_PERIOD, SPUtils.getInstance()
                .getInt(Constants.SETTING_TRANSMISSION_PERIOD, 1000));
        return setting;
    }

}
