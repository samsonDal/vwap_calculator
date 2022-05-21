package com.vwapcalculator.app.api.vwap;

import com.vwapcalculator.app.api.Instrument;
import com.vwapcalculator.app.api.State;
import com.vwapcalculator.app.api.TwoWayPrice;

public class MarketTwoWayPrice implements TwoWayPrice {

    private final Instrument instrument;
    private final State state;
    private final double bidPrice;
    private final double bidAmount;
    private final double offerPrice;
    private final double offerAmount;

    public MarketTwoWayPrice(Instrument instrument, State state, double bidPrice, double bidAmount, double offerPrice, double offerAmount) {

        this.instrument = instrument;
        this.state = state;
        this.bidPrice = bidPrice;
        this.bidAmount = bidAmount;
        this.offerPrice = offerPrice;
        this.offerAmount = offerAmount;
    }
    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public double getBidPrice() {
        return bidPrice;
    }

    @Override
    public double getOfferAmount() {
        return offerAmount;
    }

    @Override
    public double getOfferPrice() {
        return offerPrice;
    }

    @Override
    public double getBidAmount() {
        return bidAmount;
    }
}
