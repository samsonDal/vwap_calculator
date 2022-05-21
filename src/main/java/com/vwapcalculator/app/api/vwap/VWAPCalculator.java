package com.vwapcalculator.app.api.vwap;

import com.vwapcalculator.app.api.*;
import com.vwapcalculator.app.disruptor.MarketUpdateConsumer;

import java.util.EnumMap;
import java.util.Map;

/**
 * The assumption here is that VWAPCalculator is used by an external process outside the event streaming of the market data
 * producer and consumer. Whenever the process wants to get the VWAP price of an instrument it takes the current snapshot
 * of the market data ( a read-only copy of the current view of the market).
 * **/
public class VWAPCalculator implements Calculator {


    private final MarketUpdateConsumer marketUpdateConsumer;

    public VWAPCalculator(MarketUpdateConsumer consumer) {
        this.marketUpdateConsumer = consumer;
    }

    @Override
    public TwoWayPrice applyMarketUpdate(MarketUpdate twoWayMarketPrice) {
        if (twoWayMarketPrice == null) {
            return null;
        }
        if (twoWayMarketPrice.getTwoWayPrice() == null || twoWayMarketPrice.getTwoWayPrice().getInstrument() == null) {
            return null;
        }

        Map<Instrument, EnumMap<Market, TwoWayPrice>> snapshot = marketUpdateConsumer.getMarketUpdateSnapshot();
        Instrument instrument = twoWayMarketPrice.getTwoWayPrice().getInstrument();
        EnumMap<Market, TwoWayPrice> pricesPerMarket = snapshot.get(instrument);
        if (pricesPerMarket != null) {
           return calcVWAPPrice(instrument, pricesPerMarket);
        }
        return null;
    }

    private TwoWayPrice calcVWAPPrice(Instrument instrument, EnumMap<Market, TwoWayPrice> pricesPerMarket) {

        double vwapBidPriceAmount = 0;
        double vwapBidAmount = 0;
        double vwapOfferPriceAmount = 0;
        double vwapOfferAmount = 0;
        State state = State.FIRM;

        for (TwoWayPrice twoWayPrice :pricesPerMarket.values()) {
            if(twoWayPrice.getBidAmount() > 0 && twoWayPrice.getBidPrice() > 0.0){
                vwapBidPriceAmount += (twoWayPrice.getBidAmount() * twoWayPrice.getBidPrice());
                vwapBidAmount += twoWayPrice.getBidAmount();
            }
            if (twoWayPrice.getState() == State.INDICATIVE) {
                state = State.INDICATIVE;
            }
            if(twoWayPrice.getOfferPrice() >0 && twoWayPrice.getOfferAmount() > 0){
                vwapOfferPriceAmount += (twoWayPrice.getOfferAmount() * twoWayPrice.getOfferPrice());
                vwapOfferAmount += twoWayPrice.getOfferAmount();
            }
        }
        return new VWAPTwoWayPrice(instrument,vwapBidPriceAmount/vwapBidAmount,vwapOfferPriceAmount/vwapOfferAmount, state);
    }
}
