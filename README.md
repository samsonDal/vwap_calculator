# VWAP Calculator

VwapCalculator allows you to calculate the VWAP of a particular instrument through the current market data snapshot.
The assumption made here is that the market data updates and processing should be outside the VWAPCalculator to provide separation of concern
and efficiencies as the calculator doesn't necessarily have to react on every market data ticks.

Market data updates are processed using Disruptor pattern, which is used for the publishing and consuming market data this allows us to separate producer and consumer and provide non-blocking operation.
producer and consumer don't block or wait for each other this improves performance as no locking is required. The producer fetches the market data
from a file and pushes it to the ringbuffer, the consumer receives the market data update, validates it and provides a snapshot of the market data 
to the VwapCalculator. This snapshot is a read-only view.

# HOW TO RUN 
To run this project you'll need gradle and java 1.8