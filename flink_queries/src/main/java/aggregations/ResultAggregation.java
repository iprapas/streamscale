package aggregations;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ResultAggregation implements AllWindowFunction<Tuple5<Integer, String, LocalDateTime, LocalDateTime, Double>, Tuple4<Integer, LocalDateTime, LocalDateTime, Double>, TimeWindow> {

    /**
     * Evaluates the window and outputs none or several elements.
     *
     * @param window The window that is being evaluated.
     * @param values The elements in the window being evaluated.
     * @param out    A collector for emitting elements.
     * @throws Exception The function may throw exceptions to fail the program and trigger recovery.
     */
    @Override
    public void apply(TimeWindow window, Iterable<Tuple5<Integer, String, LocalDateTime, LocalDateTime, Double>> values, Collector<Tuple4<Integer, LocalDateTime, LocalDateTime, Double>> out) throws Exception {

        final Iterator<Tuple5<Integer, String, LocalDateTime, LocalDateTime, Double>> iterator = values.iterator();

        Integer parallelism = 0;
        Map<Integer, Double> querySums = new HashMap<>();
        querySums.put(1, 0D);
        querySums.put(2, 0D);
        Integer countQuery1 = 0;
        Integer countQuery2 = 0;

        while (iterator.hasNext()) {
            final Tuple5<Integer, String, LocalDateTime, LocalDateTime, Double> record = iterator.next();

            if (record.f1.endsWith("1")) {
                Double currentValue = querySums.get(1);
                Double newValue = currentValue + record.f4;
                querySums.put(1, newValue);
                countQuery1++;
            } else {
                Double currentValue = querySums.get(2);
                Double newValue = currentValue + record.f4;
                querySums.put(2, newValue);
                countQuery2++;
            }

            parallelism = record.f0; // parallelism will take the last record's value
        }

        Double avg1 = 0D;
        Double avg2 = 0D;

        if(countQuery1!=0) {
            avg1 = querySums.get(1) / countQuery1;
        }

        if(countQuery2!=0) {
            avg2 = querySums.get(2) / countQuery2;
        }

//        Double avgLatency = (avg1 + avg2) / 2;
        Double avgLatency = Math.max(avg1, avg2); // max of the two
        LocalDateTime winStart = DateTimeUtil.parseMillis(window.getStart());
        LocalDateTime winEnd = DateTimeUtil.parseMillis(window.getEnd());

        out.collect(new Tuple4<>(parallelism, winStart, winEnd, avgLatency));

    }
}