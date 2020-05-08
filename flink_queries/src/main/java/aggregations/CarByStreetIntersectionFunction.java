package aggregations;

import domain.Car;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import util.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class CarByStreetIntersectionFunction implements WindowFunction<Car, AggregateCarByStreet, Integer, TimeWindow> {

    private int parallelism;

    public CarByStreetIntersectionFunction(int parallelism){
        this.parallelism = parallelism;
    }

    @Override
    public void apply(Integer key, TimeWindow window, Iterable<Car> cars, Collector<AggregateCarByStreet> out) throws Exception {
        final LocalDateTime start = DateTimeUtil.parseMillis(window.getStart());
        final LocalDateTime end = DateTimeUtil.parseMillis(window.getEnd());
        final ZonedDateTime nowProcessingTime = ZonedDateTime.now(ZoneId.of("UTC"));

        ZonedDateTime latest = DateTimeUtil.veryOldDate();

        int count = 0;
        for (final Car car : cars) {
            count++;
            if (car.getTimestamp().isAfter(latest)) {
                latest = car.getTimestamp();
            }
        }

        final AggregateCarByStreet agg = new AggregateCarByStreet();
        agg.setStreetId(String.valueOf(key));
        agg.setEndTime(end);
        agg.setStartTime(start);
        agg.setParallelism(parallelism);

        final long latency = latest.until(nowProcessingTime, ChronoUnit.MILLIS);

        agg.setLatency(latency);
        agg.setThroughput(count);
        out.collect(agg);
    }
}
