package com.vwapcalculator.app.disruptor;

import com.vwapcalculator.app.api.Instrument;
import com.vwapcalculator.app.api.Market;
import com.vwapcalculator.app.api.MarketDataValidationFailedException;
import com.vwapcalculator.app.api.TwoWayPrice;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMarketUpdateConsumer {

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataNullMarketInEvent() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        when(event.getMarket()).thenReturn(null);
        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataNullPrice() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(null);
        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataNullInstrument() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(null);
        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataInvalidBid() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(price.getBidPrice()).thenReturn(0D);
        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataInvalidAsk() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(price.getBidPrice()).thenReturn(10D);
        when(price.getOfferPrice()).thenReturn(0D);
        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataInvalidBidAmount() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(price.getBidPrice()).thenReturn(10D);
        when(price.getOfferPrice()).thenReturn(11D);
        when(price.getBidAmount()).thenReturn(0D);
        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptDataInvalidAskAmount() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(price.getBidPrice()).thenReturn(10D);
        when(price.getOfferPrice()).thenReturn(11D);
        when(price.getBidAmount()).thenReturn(200D);
        when(price.getOfferAmount()).thenReturn(0D);

        consumer.onEvent(event);
        fail();
    }

    @Test(expected = MarketDataValidationFailedException.class)
    public void testCorruptCrossedMarket() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(price.getBidPrice()).thenReturn(10D);
        when(price.getOfferPrice()).thenReturn(8D);
        when(price.getBidAmount()).thenReturn(200D);
        when(price.getOfferAmount()).thenReturn(100D);
        consumer.onEvent(event);
        fail();
    }

    @Test
    public void testValidEventAddedToCache() throws MarketDataValidationFailedException {
        MarketUpdateConsumer consumer = new MarketUpdateConsumer();
        MarketUpdateEvent event = mock(MarketUpdateEvent.class);
        TwoWayPrice price = mock(TwoWayPrice.class);
        when(event.getMarket()).thenReturn(Market.MARKET0);
        when(event.getTwoWayPrice()).thenReturn(price);
        when(price.getInstrument()).thenReturn(Instrument.INSTRUMENT0);
        when(price.getBidPrice()).thenReturn(10D);
        when(price.getOfferPrice()).thenReturn(11D);
        when(price.getBidAmount()).thenReturn(200D);
        when(price.getOfferAmount()).thenReturn(300D);

        consumer.onEvent(event);
        assertEquals(1, consumer.getMarketUpdateSnapshot().size());
    }
}
