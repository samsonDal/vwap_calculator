package com.vwapcalculator.app.api;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.ProducerType;
import com.vwapcalculator.app.api.vwap.VWAPCalculator;
import com.vwapcalculator.app.disruptor.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.State;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@State(Scope.Benchmark)
public class TestVWAPCalculator {
    private ExceptionHandler<MarketUpdateEvent> exceptionHandler;
    private MarketUpdateDisruptor disruptor;
    private Calculator vwapCalculator;

    @Before
    @Setup(Level.Invocation)
    public void setUp(){
        MarketUpdateDisruptorFactory marketUpdateDisruptorFactory = new MarketUpdateDisruptorFactory("Test");

        MarketUpdateConsumer eventConsumer = new MarketUpdateConsumer();

        MarketUpdateEventTranslator translator = new MarketUpdateEventTranslator();

        exceptionHandler = mock(DefaultExceptionHandler.class);
        disruptor =
               marketUpdateDisruptorFactory.newInstance(new BusySpinWaitStrategy(), ProducerType.SINGLE, eventConsumer, exceptionHandler);

        EventProducer eventProducer = new FileBasedMarketUpdateProducer(disruptor, translator, "marketUpdates");

        vwapCalculator = new VWAPCalculator(eventConsumer);

        disruptor.start();
        if (disruptor.isStarted()) {
            eventProducer.startProducing();
        }

    }

    @Test
    public void testVwapCalculator() {
        MarketUpdate marketUpdate = mock(MarketUpdate.class);
        TwoWayPrice twoWayPrice = mock(TwoWayPrice.class);

        when(twoWayPrice.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(marketUpdate.getTwoWayPrice()).thenReturn(twoWayPrice);

        TwoWayPrice vwapPrice = vwapCalculator.applyMarketUpdate(marketUpdate);
        Assert.assertNotNull(vwapPrice);
        assertEquals(111.0, vwapPrice.getBidPrice(), 0.01);
        assertEquals(112.0, vwapPrice.getOfferPrice(), 0.01);
        assertEquals(com.vwapcalculator.app.api.State.FIRM, vwapPrice.getState());


    }

    @Test
    public void testVwapCalculatorIndicative() {
        MarketUpdate marketUpdate = mock(MarketUpdate.class);
        TwoWayPrice twoWayPrice = mock(TwoWayPrice.class);

        when(twoWayPrice.getInstrument()).thenReturn(Instrument.INSTRUMENT1);
        when(marketUpdate.getTwoWayPrice()).thenReturn(twoWayPrice);

        TwoWayPrice vwapPrice = vwapCalculator.applyMarketUpdate(marketUpdate);
        Assert.assertNotNull(vwapPrice);
        assertEquals(com.vwapcalculator.app.api.State.INDICATIVE, vwapPrice.getState());
    }


    @Test
    public void testThrownDuringMktDataProcessing() {
        MarketUpdate marketUpdate = mock(MarketUpdate.class);
        TwoWayPrice twoWayPrice = mock(TwoWayPrice.class);

        when(twoWayPrice.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(marketUpdate.getTwoWayPrice()).thenReturn(twoWayPrice);

        TwoWayPrice vwapPrice = vwapCalculator.applyMarketUpdate(marketUpdate);
        Assert.assertNotNull(vwapPrice);
        assertEquals(111.0, vwapPrice.getBidPrice(), 0.01);
        assertEquals(112.0, vwapPrice.getOfferPrice(), 0.01);
        // one of the market data entry in the file had a corrupt data, exception is thrown but processing continues,
        // corrupted mkt data is ignored
        verify(exceptionHandler, times(2)).handleEventException(any(Throwable.class), anyLong(), any(MarketUpdateEvent.class));

    }


    @Test
    public void testVwapCalculatorInstrumentNotPresentInFeed() {
        MarketUpdate marketUpdate = mock(MarketUpdate.class);
        TwoWayPrice twoWayPrice = mock(TwoWayPrice.class);

        when(twoWayPrice.getInstrument()).thenReturn(Instrument.INSTRUMENT5);
        when(marketUpdate.getTwoWayPrice()).thenReturn(twoWayPrice);

        TwoWayPrice vwapPrice = vwapCalculator.applyMarketUpdate(marketUpdate);
        Assert.assertNull(vwapPrice);
    }

    @Test
    public void testVwapCalculatorCorruptData() {
        MarketUpdate marketUpdate = mock(MarketUpdate.class);
        TwoWayPrice twoWayPrice = mock(TwoWayPrice.class);

        when(twoWayPrice.getInstrument()).thenReturn(null);
        when(marketUpdate.getTwoWayPrice()).thenReturn(twoWayPrice);

        TwoWayPrice vwapPrice = vwapCalculator.applyMarketUpdate(marketUpdate);
        Assert.assertNull(vwapPrice);
    }

    @Test
    public void testVwapCalculatorCorruptData2() {
        MarketUpdate marketUpdate = mock(MarketUpdate.class);
        TwoWayPrice twoWayPrice = mock(TwoWayPrice.class);

        when(twoWayPrice.getInstrument()).thenReturn(Instrument.INSTRUMENT1);
        when(marketUpdate.getTwoWayPrice()).thenReturn(null);

        TwoWayPrice vwapPrice = vwapCalculator.applyMarketUpdate(marketUpdate);
        Assert.assertNull(vwapPrice);
    }

    @After
    public void shutDown() {
        if (disruptor != null && !disruptor.isStopped()) {
            disruptor.stop();
        }
    }
}
