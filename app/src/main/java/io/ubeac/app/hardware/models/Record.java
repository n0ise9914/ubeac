package io.ubeac.app.hardware.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Record implements Serializable {
    private String name;
    private List<Frame> frames = new ArrayList<>();

    public Record(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Frame> getFrames() {
        return frames;
    }

    public void setFrames(List<Frame> frames) {
        this.frames = frames;
    }
}
