package com.vwapcalculator.app.disruptor;

import com.vwapcalculator.app.api.Market;
import com.vwapcalculator.app.api.TwoWayPrice;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TestFileBasedMarketUpdateProducer {

    private MarketUpdateDisruptor disruptor;
    private MarketUpdateEventTranslator translator;

    private FileBasedMarketUpdateProducer producer;

    @Before
    public void setUp() {
        disruptor = mock(MarketUpdateDisruptor.class);
        translator = mock(MarketUpdateEventTranslator.class);
        producer = new FileBasedMarketUpdateProducer(disruptor,  translator,"filename");
    }

    @Test
    public void testPublishEvent() {
        String line = "MARKET0,INSTRUMENT0,FIRM,111.00,20,112.00,25\n";
        producer.parseAndPublish(line);
        verify(translator).setValues(any(Market.class), any(TwoWayPrice.class));
        verify(disruptor).publish(translator);

    }

    @Test
    public void testPublishEventFailed() {
        String line = "MARKET0,INSTRUMENT0,,111.00,20,112.00,25\n";
        producer.parseAndPublish(line);
        verify(translator, never()).setValues(any(Market.class), any(TwoWayPrice.class));
        verify(disruptor, never()).publish(translator);

    }
}
