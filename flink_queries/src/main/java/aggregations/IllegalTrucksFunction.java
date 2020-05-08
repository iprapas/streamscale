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

public class IllegalTrucksFunction implements WindowFunction<Car, AggregateIllegalTrunks, Integer, TimeWindow> {

    private int parallelism;

    public IllegalTrucksFunction(int parallelism){
        this.parallelism = parallelism;
    }

    @Override
    public void apply(Integer streetId, TimeWindow window, Iterable<Car> cars, Collector<AggregateIllegalTrunks> out) throws Exception {
        final LocalDateTime start = DateTimeUtil.parseMillis(window.getStart());
        final LocalDateTime end = DateTimeUtil.parseMillis(window.getEnd());
        final ZonedDateTime nowProcessingTime = ZonedDateTime.now(ZoneId.of("UTC"));
        //System.out.println("Now Processing time :" + nowProcessingTime);

        ZonedDateTime latest = DateTimeUtil.veryOldDate();

        int numEvents = 0;
        StringBuilder builder = new StringBuilder(streetId + "||"); //default size for worst case
        for (final Car car : cars) {
            numEvents++;
            builder.append(car.getCarId()).append('|');

            if (car.getTimestamp().isAfter(latest)) {
                latest = car.getTimestamp();
            }
        }

        final AggregateIllegalTrunks agg = new AggregateIllegalTrunks();
        agg.setCarIds(builder.toString());
        agg.setEndTime(end);
        agg.setStartTime(start);
        agg.setParallelism(parallelism);

        final long latency = latest.until(nowProcessingTime, ChronoUnit.MILLIS);

        agg.setLatency(latency);
        agg.setThroughput(numEvents);
        out.collect(agg);
    }
}
