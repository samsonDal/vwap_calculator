package com.vwapcalculator.app.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import com.vwapcalculator.app.api.EventConsumer;

public class MarketUpdateDisruptorFactory {

    private final String context;

    public MarketUpdateDisruptorFactory(String context) {
        this.context = context;
    }

    public MarketUpdateDisruptor newInstance(WaitStrategy waitStrategy, ProducerType producerType, EventConsumer<MarketUpdateEvent> consumer,
                                             ExceptionHandler<MarketUpdateEvent> exceptionHandler) {
        return new MarketUpdateDisruptor(context, consumer, waitStrategy, producerType, exceptionHandler);
    }
}
