package aggregations;

import domain.Click;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import util.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MILLIS;

public class ClickByCountryFunction implements WindowFunction<Click, AggregateClickByCountry, String, TimeWindow> {

    private int parallelism;

    public ClickByCountryFunction(int parallelism){
        this.parallelism = parallelism;
    }

    @Override
    public void apply(
            String country,
            TimeWindow window,
            Iterable<Click> clicks,
            final Collector<AggregateClickByCountry> out) throws Exception
    {
        final LocalDateTime start = DateTimeUtil.parseMillis(window.getStart());
        final LocalDateTime end = DateTimeUtil.parseMillis(window.getEnd());
        //12:00:01 00:01
        final ZonedDateTime nowProcessingTime = ZonedDateTime.now(ZoneId.of("UTC"));

        int count = 0;
        ZonedDateTime latest = DateTimeUtil.veryOldDate();
        for(final Click click : clicks) {
            count ++;
            if(click.getTimestamp().isAfter(latest)) {
                latest = click.getTimestamp();
            }
        }

        //Latest 11:59:59 19:19
        final AggregateClickByCountry agg = new AggregateClickByCountry();
        agg.setCount(count);
        agg.setCountry(country);
        agg.setEndTime(end);
        agg.setStartTime(start);
        agg.setParallelism(parallelism);

        final long latency = latest.until(nowProcessingTime, MILLIS);
        agg.setLatency(latency);
        agg.setThroughput(count);
        out.collect(agg);
    }
}
