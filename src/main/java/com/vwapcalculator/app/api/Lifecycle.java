package com.vwapcalculator.app.api;

public interface Lifecycle {

    enum State {
        INITIALIZED,
        STARTED,
        STOPPED
    }
    void initialise();

    void start();

    void stop();

    boolean isStarted();

    boolean isStopped();
}
