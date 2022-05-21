package com.vwapcalculator.app.api;

import com.lmax.disruptor.EventHandler;

public interface EventConsumer<T> {

    EventHandler<T> getEventHandler();
}
