package io.ubeac.app.network.models;

import java.util.Hashtable;

public class SensorDetail {
    public String id;
    public Long ts;
    public Integer type;
    public Integer unit;
    public Integer prefix;
    public Hashtable data;

    public SensorDetail(String id, Long ts, Integer type, Integer unit, Integer prefix, Hashtable data) {
        this.id = id;
        this.ts = ts;
        this.type = type;
        this.unit = unit;
        this.prefix = prefix;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public Integer getPrefix() {
        return prefix;
    }

    public void setPrefix(Integer prefix) {
        this.prefix = prefix;
    }

    public Hashtable getData() {
        return data;
    }

    public void setData(Hashtable data) {
        this.data = data;
    }
}
