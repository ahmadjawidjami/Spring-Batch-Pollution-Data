package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

public class PollutionDataItemProcessor implements ItemProcessor<PollutionData, PollutionData> {

    private static final Logger log = LoggerFactory.getLogger(PollutionDataItemProcessor.class);

    @Override
    public PollutionData process(final PollutionData pollutionData) throws Exception {

        final PollutionData transformedPollutionData = new PollutionData();
        transformedPollutionData.setStationName(pollutionData.getStationName().toLowerCase());

        //log.info("Converting (" + pollutionData + ") into (" + transformedPollutionData + ")");

        return pollutionData;
    }

}
