package com.vwapcalculator.app.disruptor;

import com.lmax.disruptor.EventFactory;
import com.vwapcalculator.app.api.Market;
import com.vwapcalculator.app.api.MarketUpdate;
import com.vwapcalculator.app.api.TwoWayPrice;

/**
 * The MarketUpdateEvent is what gets published to the disruptor's ring buffer and consumed by a consumer
 */
public class MarketUpdateEvent implements MarketUpdate {

    //this factory is used by disruptor to populate all buckets with MarketUpdateEvents during initialisation phase, and objects reused
    // this makes it GC friendly and avoid creating new objects everytime an event is generated
    public static final EventFactory<MarketUpdateEvent> FACTORY = MarketUpdateEvent::new;
    private Market market;
    private TwoWayPrice twoWayPrice;

    public void setValues(Market market, TwoWayPrice twoWayPrice) {
        setMarket(market);
        setTwoWayPrice(twoWayPrice);
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public void setTwoWayPrice(TwoWayPrice twoWayPrice) {
        this.twoWayPrice = twoWayPrice;
    }

    @Override
    public Market getMarket() {
        return market;
    }

    @Override
    public TwoWayPrice getTwoWayPrice() {
        return twoWayPrice;
    }

    @Override
    public String toString() {
        return market.name() + "[bid: " + twoWayPrice.getBidPrice() + ",bidAmount: " + getTwoWayPrice().getBidAmount()
                + ",ask: " + getTwoWayPrice().getOfferPrice() + ",askAmount: " + getTwoWayPrice().getOfferAmount() + "]";
    }
}
