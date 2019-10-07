package io.ubeac.app.service.listeners;

import io.ubeac.app.service.models.Signal;

public interface SignalListener {
    void onSignal(Signal message);
}
