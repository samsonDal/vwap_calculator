package com.vwapcalculator.app.disruptor;

import com.lmax.disruptor.EventTranslator;
import com.vwapcalculator.app.api.Market;
import com.vwapcalculator.app.api.TwoWayPrice;

/**
 * Populates the empty event object from the ring buffer with data so it can be published by the disruptor
 */
public class MarketUpdateEventTranslator implements EventTranslator<MarketUpdateEvent> {

    Market market;
    TwoWayPrice twoWayPrice;

    @Override
    public void translateTo(MarketUpdateEvent event, long sequence) {

        event.setValues(market, twoWayPrice);
        // clear this translator fields
        clear();
    }

    public void setValues(Market market, TwoWayPrice twoWayPrice) {
        this.market = market;
        this.twoWayPrice = twoWayPrice;
    }

    public void clear() {
        setValues(null, null);
    }
}
