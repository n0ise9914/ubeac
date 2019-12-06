package io.ubeac.app.network.models;

import java.util.List;

public class Packet {
    private String id;
    private List<SensorDetail> sensors;
    public Packet(String id, List<SensorDetail> sensors) {
        this.id = id;
        this.sensors = sensors;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SensorDetail> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorDetail> sensors) {
        this.sensors = sensors;
    }
}
