package io.ubeac.app.services;

import io.ubeac.app.services.models.Signal;

public interface IBaseService {
    void onSignal(Signal signal);
}
