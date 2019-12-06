package io.ubeac.app.hardware.models;

import java.util.LinkedHashMap;
import java.util.SortedMap;

public class SensorMapping {
    private LinkedHashMap<Integer, String> values;
    private Integer type;
    private Integer unit;
    private Integer prefix;

    public LinkedHashMap<Integer, String> getValues() {
        return values;
    }

    public void setValues(LinkedHashMap<Integer, String> values) {
        this.values = values;
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
