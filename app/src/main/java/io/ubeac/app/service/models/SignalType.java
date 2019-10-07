package io.ubeac.app.service.models;

import java.io.Serializable;

public enum SignalType implements Serializable {
    Register,
    StartMainActivity,
    Exit,
    SensorData,
    SensorEnabled,
    Registered,
    Setting,
    LocationData,
    LocationEnabled;
}
