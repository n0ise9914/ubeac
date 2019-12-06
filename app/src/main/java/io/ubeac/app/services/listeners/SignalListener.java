package io.ubeac.app.services.listeners;

import io.ubeac.app.services.models.Signal;

public interface SignalListener {
    void onSignal(Signal message);
}
