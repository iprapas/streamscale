package keys;

import domain.Click;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ClicksByCountryKeySelector implements KeySelector<Click, Tuple2<String, LocalDateTime>> {

    @Override
    public Tuple2<String, LocalDateTime> getKey(final Click click) throws Exception {
        final LocalDateTime minute = LocalDateTime.of(
                click.getTimestamp().toLocalDate(),
                LocalTime.of(
                    click.getTimestamp().getHour(),
                    click.getTimestamp().getMinute(),
                    0
                )
        );
        return new Tuple2<>(click.getCountry(), minute);
    }

}
