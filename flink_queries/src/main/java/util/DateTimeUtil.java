package util;

import java.time.*;

public class DateTimeUtil {

    public static LocalDateTime parseMillis(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }

    public static ZonedDateTime veryOldDate() {
        return ZonedDateTime.of(LocalDateTime.of(LocalDate.of(1971, 1, 1), LocalTime.of(1,1,1)), ZoneId.of("UTC"));
    }

}
