package io.ubeac.app.hardware.models;

import java.io.Serializable;
import java.util.Hashtable;

public class Frame implements Serializable{
    private Long timestamp;
    private Hashtable data;
    private Integer type;
    private Integer unit;
    private Integer prefix;

    public Frame(long timestamp, Hashtable data) {
        this.timestamp = timestamp;
        this.data = data;
    }

    public Frame(long timestamp, Hashtable data, Integer type, Integer unit, Integer prefix) {
        this.timestamp = timestamp;
        this.data = data;
        this.type = type;
        this.unit = unit;
        this.prefix = prefix;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Hashtable getData() {
        return data;
    }

    public void setData(Hashtable data) {
        this.data = data;
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
}
