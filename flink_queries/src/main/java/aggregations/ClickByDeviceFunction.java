package aggregations;

import domain.Click;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import util.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ClickByDeviceFunction implements WindowFunction<Click, AggregateClickByDevice, String, TimeWindow> {

    private int parallelism;

    public ClickByDeviceFunction(int parallelism){
        this.parallelism = parallelism;
    }

    @Override
    public void apply(
            String device,
            TimeWindow window,
            Iterable<Click> clicks,
            final Collector<AggregateClickByDevice> out) throws Exception
    {
        final LocalDateTime start = DateTimeUtil.parseMillis(window.getStart());
        final LocalDateTime end = DateTimeUtil.parseMillis(window.getEnd());
        final ZonedDateTime nowProcessingTime = ZonedDateTime.now(ZoneId.of("UTC"));

        Map<String, Long> map = new HashMap<>();
        ZonedDateTime latest = DateTimeUtil.veryOldDate();
        int numEvents =0;
        for (final Click click : clicks) {
            numEvents++;
            String dev = click.getDevice();
            if (map.containsKey(dev)) {
                map.put(dev, map.get(dev) + 1);
            } else {
                map.put(dev, 1L);
            }

            if (click.getTimestamp().isAfter(latest)) {
                latest = click.getTimestamp();
            }
        }

        final String maxDevice = map.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

        final AggregateClickByDevice agg = new AggregateClickByDevice();
        agg.setDevice(maxDevice);
        agg.setEndTime(end);
        agg.setStartTime(start);
        agg.setParallelism(parallelism);

        final long latency = latest.until(nowProcessingTime, ChronoUnit.MILLIS);
        agg.setLatency(latency);
        agg.setThroughput(numEvents);
        out.collect(agg);
    }

}
