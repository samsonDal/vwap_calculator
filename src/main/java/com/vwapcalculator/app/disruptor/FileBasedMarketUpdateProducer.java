package com.vwapcalculator.app.disruptor;

import com.vwapcalculator.app.api.EventProducer;
import com.vwapcalculator.app.api.Instrument;
import com.vwapcalculator.app.api.Market;
import com.vwapcalculator.app.api.State;
import com.vwapcalculator.app.api.vwap.MarketTwoWayPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Simple file based market update producer that reads market data from a file and pushes it to the next available sequence
 * in the disruptor's ring buffer.
 */
public class FileBasedMarketUpdateProducer implements EventProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedMarketUpdateProducer.class);
    private final MarketUpdateDisruptor disruptor;
    private final MarketUpdateEventTranslator translator;
    private final String fileName;

    public FileBasedMarketUpdateProducer(MarketUpdateDisruptor disruptor, MarketUpdateEventTranslator translator, String fileName) {
        this.disruptor = disruptor;
        this.translator = translator ;
        this.fileName = fileName;
    }

    @Override
    public void startProducing() {
        try {
            readMarketUpdates();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while trying to produce market updates ", e);
        }
    }

    private URL getResource(String resourceName) {
        URL resource = FileBasedMarketUpdateProducer.class.getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found");
        }
        return resource;
    }

    private void readMarketUpdates() throws Exception {
        URL resource = getResource(fileName);
        File file = Paths.get(resource.toURI()).toFile();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines()
                    .forEach(
                            this::parseAndPublish
                    );
        }
    }

    void parseAndPublish(String line) {
        try {
            if (StringUtils.isEmpty(line)) {
                return;
            }
            String[] split = line.split(",");
            if (split.length < 7) throw new IllegalArgumentException("Wrong file format " + line);
            Market market = Market.valueOf(split[0]);
            Instrument instrument = Instrument.valueOf(split[1]);
            State state = State.valueOf(split[2]);
            double bid = Double.parseDouble(split[3]);
            double bidAmount = Double.parseDouble(split[4]);
            double ask = Double.parseDouble(split[5]);
            double askAmount = Double.parseDouble(split[6]);

            MarketTwoWayPrice marketTwoWayPrice = new MarketTwoWayPrice(instrument, state, bid, bidAmount, ask, askAmount);
            translator.setValues(market, marketTwoWayPrice);
            disruptor.publish(translator);
        } catch (Exception ex) {
            LOGGER.error("Skipping publishing, error reading line " + line, ex);
        }

    }
}
