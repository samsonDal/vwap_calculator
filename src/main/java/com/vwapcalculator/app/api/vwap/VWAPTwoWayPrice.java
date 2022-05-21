package com.vwapcalculator.app.api.vwap;

import com.vwapcalculator.app.api.Instrument;
import com.vwapcalculator.app.api.State;
import com.vwapcalculator.app.api.TwoWayPrice;

public class VWAPTwoWayPrice implements TwoWayPrice {
    private final Instrument instrument;
    private final double bidPrice;
    private final double offerPrice;

    private final State state;

    public VWAPTwoWayPrice(Instrument instrument, double bidPrice, double offerPrice, State state) {
        this.instrument = instrument;
        this.bidPrice = bidPrice;
        this.offerPrice = offerPrice;
        this.state = state;
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
        return 0;
    }

    @Override
    public double getOfferPrice() {
        return offerPrice;
    }

    @Override
    public double getBidAmount() {
        return 0;
    }
}
