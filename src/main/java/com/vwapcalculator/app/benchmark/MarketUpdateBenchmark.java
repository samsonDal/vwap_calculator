package com.vwapcalculator.app.benchmark;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.ProducerType;
import com.vwapcalculator.app.api.*;
import com.vwapcalculator.app.api.vwap.MarketTwoWayPrice;
import com.vwapcalculator.app.api.vwap.VWAPCalculator;
import com.vwapcalculator.app.disruptor.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;


import java.util.concurrent.TimeUnit;


@Fork(value = 2)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 20, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class MarketUpdateBenchmark {


    private MarketUpdateConsumer eventConsumer;

    private Calculator vwapCalculator;

    @Setup
    public void setUp() {
        MarketUpdateDisruptorFactory marketUpdateDisruptorFactory = new MarketUpdateDisruptorFactory("Benchmark");

        eventConsumer = new MarketUpdateConsumer();

        ExceptionHandler<MarketUpdateEvent> exceptionHandler = new DefaultExceptionHandler();
        MarketUpdateDisruptor disruptor = marketUpdateDisruptorFactory.newInstance(new BusySpinWaitStrategy(), ProducerType.SINGLE, eventConsumer, exceptionHandler);

        EventProducer eventProducer = new FileBasedMarketUpdateProducer(disruptor,  new MarketUpdateEventTranslator(), "benchMarkData");

        vwapCalculator = new VWAPCalculator(eventConsumer);

        disruptor.start();
        if (disruptor.isStarted()) {
            eventProducer.startProducing();
        }
    }

    @Benchmark
    public void getMarketUpdateSnapshot(Blackhole blackhole) {
        blackhole.consume(eventConsumer.getMarketUpdateSnapshot());
    }

    @Benchmark
    public void calculateVwap(Blackhole blackhole) {
        MarketUpdateEvent marketUpdate = new MarketUpdateEvent();
        marketUpdate.setMarket(Market.MARKET0);
        marketUpdate.setTwoWayPrice(new MarketTwoWayPrice(Instrument.INSTRUMENT0, com.vwapcalculator.app.api.State.FIRM, 10d, 20d,
                12d, 45d));
        blackhole.consume(vwapCalculator.applyMarketUpdate(marketUpdate));
    }



}
