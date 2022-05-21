package com.vwapcalculator.app.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.vwapcalculator.app.api.EventConsumer;
import com.vwapcalculator.app.api.Lifecycle;
import com.vwapcalculator.app.api.VwapThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper class for MarketUpdatDisruptor and handles the mechanics for working with LMAX Disruptor
 * this disruptor will be created by the MarketUpdateDisruptorFactory under a given context
 */
public class MarketUpdateDisruptor implements Lifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketUpdateDisruptor.class);
    private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 200;
    private static final int SLEEP_MILLIS_BETWEEN_DRAIN_ATTEMPTS = 50;
    private static final int TIMEOUT_SHUTDOWN_SECONDS = 2;
    private final String contextName;
    private final EventConsumer<MarketUpdateEvent> eventConsumer;
    private final WaitStrategy waitStrategy;
    private final ProducerType producerType;
    private final ExceptionHandler<MarketUpdateEvent> exceptionHandler;
    private volatile Disruptor<MarketUpdateEvent> disruptor;
    private volatile State lifeCycleState = State.INITIALIZED;

    MarketUpdateDisruptor(String contextName, EventConsumer<MarketUpdateEvent> eventConsumer,
                          WaitStrategy waitStrategy, ProducerType producerType, ExceptionHandler<MarketUpdateEvent> exceptionHandler) {

        this.contextName = contextName;
        this.eventConsumer = eventConsumer;
        this.waitStrategy = waitStrategy;
        this.producerType = producerType;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void initialise() {

    }

    @Override
    public synchronized void start() {
        if (disruptor != null) {
            LOGGER.warn("Not creating a new instance of disruptor for the current context " + contextName);
            return;
        }
        LOGGER.info("Creating new disruptor for this context " + contextName);
        final ThreadFactory threadFactory = new VwapThreadFactory("MarketUpdator[" + contextName + "]", true);
        disruptor = new Disruptor<>(MarketUpdateEvent.FACTORY, (int) Math.pow(2, 12), threadFactory,
                producerType, waitStrategy);
        disruptor.setDefaultExceptionHandler(exceptionHandler);
        disruptor.handleEventsWith(eventConsumer.getEventHandler());
        LOGGER.debug("[{}] Starting MarketUpdate disruptor for this context with ringBufferSize={}, waitStrategy={}, "
                , contextName, disruptor.getRingBuffer().getBufferSize(), waitStrategy
                        .getClass().getSimpleName());

        disruptor.start();
        lifeCycleState = State.STARTED;

    }

    public void publish(EventTranslator<MarketUpdateEvent> translator) {
        disruptor.publishEvent(translator);
    }

    private static boolean hasQueuedEvents(final Disruptor<?> theDisruptor) {
        final RingBuffer<?> ringBuffer = theDisruptor.getRingBuffer();
        return !ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize());
    }

    Disruptor<MarketUpdateEvent> getDisruptor() {
        return disruptor;
    }

    @Override
    public synchronized void stop() {
        final Disruptor<MarketUpdateEvent> temp = getDisruptor();
        if (temp == null) {
            LOGGER.info("[{}] MarketUpdateDisruptor: disruptor for this context already shut down.", contextName);
            return; // disruptor was already shut down by another thread
        }
        LOGGER.info("[{}] MarketUpdateDisruptor: shutting down disruptor for this context.", contextName);

        // We must guarantee that publishing to the RingBuffer has stopped before we call disruptor.shutdown().
        disruptor = null; // so clients will fail if they try to call actions on disruptor during shutdown

        try {
            // busy-spins until all events currently in the disruptor have been processed, or timeout
            temp.shutdown(TIMEOUT_SHUTDOWN_SECONDS, TimeUnit.SECONDS);
        } catch (final TimeoutException e) {
            LOGGER.warn("[{}] AsyncLoggerDisruptor: shutdown timed out after {} {}", contextName, TIMEOUT_SHUTDOWN_SECONDS, TimeUnit.SECONDS);
            temp.halt(); // give up on remaining market data events, if any
        }

        lifeCycleState = State.STOPPED;
    }

    @Override
    public boolean isStarted() {
        return lifeCycleState == State.STARTED;
    }

    @Override
    public boolean isStopped() {
        return lifeCycleState == State.STOPPED;
    }
}
