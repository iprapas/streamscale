package timestamp;

import domain.Car;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;
import java.time.ZoneOffset;

public class CarTimestampExtractor implements AssignerWithPeriodicWatermarks<Car> {

    private long maxTimestamp = 0;
    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        return new Watermark(maxTimestamp);
    }

    @Override
    public long extractTimestamp(Car car, long previousElementTimestamp) {
        long current =  car.getTimestamp().toInstant().toEpochMilli();
        maxTimestamp = Math.max(current, maxTimestamp);
        return car.getTimestamp().toInstant().toEpochMilli();
    }
}
