package timestamp;

import domain.Click;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;

import java.time.ZoneOffset;

public class ClickTimestampExtractor extends AscendingTimestampExtractor<Click> {

    @Override
    public long extractAscendingTimestamp(Click click) {
        return click.getTimestamp().toInstant().toEpochMilli();
    }
}
