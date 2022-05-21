package com.vwapcalculator.app.disruptor;

import com.lmax.disruptor.EventHandler;
import com.vwapcalculator.app.api.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


@State(Scope.Benchmark)
public class MarketUpdateConsumer implements EventConsumer<MarketUpdateEvent> {

    private final EnumMap<Instrument, EnumMap<Market, TwoWayPrice>> marketUpdateCache =
            new EnumMap<>(Instrument.class);

    private final Map<Instrument, EnumMap<Market, TwoWayPrice>> snapshot =
            Collections.unmodifiableMap(marketUpdateCache);


    @Override
    public EventHandler<MarketUpdateEvent> getEventHandler() {
        return (event, sequence, endOfBatch) ->  onEvent(event);
    }

    void onEvent(MarketUpdateEvent event) throws MarketDataValidationFailedException {
        validate(event);
        updateCache(event);
    }

    private void updateCache(MarketUpdate marketUpdate) {
        Instrument instrument = marketUpdate.getTwoWayPrice().getInstrument();
        marketUpdateCache.putIfAbsent(instrument, new EnumMap<>(Market.class));
        EnumMap<Market, TwoWayPrice> twoWayPriceMap = marketUpdateCache.get(instrument);
        twoWayPriceMap.put(marketUpdate.getMarket(), marketUpdate.getTwoWayPrice());
    }

    private void validate(MarketUpdateEvent event) throws MarketDataValidationFailedException {
        if (event == null) {
            throw new MarketDataValidationFailedException("Received a null market data event");
        }
        if (event.getMarket() == null || event.getTwoWayPrice() == null || event.getTwoWayPrice().getInstrument() == null) {
            throw new MarketDataValidationFailedException("Corrupt or invalid market data event");
        }

        if(event.getTwoWayPrice().getBidPrice() <= 0.00 || event.getTwoWayPrice().getBidAmount() <=0.00 ||
                event.getTwoWayPrice().getOfferPrice() <= 0.00 || event.getTwoWayPrice().getOfferAmount() <= 0.00) {
            throw new MarketDataValidationFailedException("Market data contains invalid values: cannot contain zero or negative price or volume");
        }

        if (event.getTwoWayPrice().getBidPrice() >= event.getTwoWayPrice().getOfferPrice()) {
            throw new MarketDataValidationFailedException("Market data is invalid, bidPrice >= offerPrice");
        }
    }

    // clients can call this to get the current snapshot of the market data
    @Benchmark
    public Map<Instrument, EnumMap<Market, TwoWayPrice>> getMarketUpdateSnapshot() {
        return snapshot;
    }

}
