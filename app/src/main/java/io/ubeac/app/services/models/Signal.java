package io.ubeac.app.services.models;

import java.io.Serializable;
import java.util.HashMap;

public class Signal implements Serializable {
    private SignalType type;
    private HashMap<String, Serializable> data;

    public Signal(SignalType command, HashMap<String, Serializable> data) {
        this.type = command;
        this.data = data;
    }

    public SignalType getType() {
        return type;
    }

    public void setType(SignalType type) {
        this.type = type;
    }

    public HashMap<String, Serializable> getData() {
        return data;
    }

    public void setData(HashMap<String, Serializable> data) {
        this.data = data;
    }
}
