package io.ubeac.app.services.models;

import java.io.Serializable;

public enum SignalType implements Serializable {
    Register,
    StartMainActivity,
    Exit,
    Data,
    SensorEnabled,
    Registered,
    Setting
}
