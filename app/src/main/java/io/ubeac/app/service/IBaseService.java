package io.ubeac.app.service;

import io.ubeac.app.service.models.Signal;

public interface IBaseService {
    void onSignal(Signal signal);
}
