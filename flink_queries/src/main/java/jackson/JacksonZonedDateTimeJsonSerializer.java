package jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class JacksonZonedDateTimeJsonSerializer extends JsonSerializer<ZonedDateTime> {
    @Override
    public void serialize(ZonedDateTime timestamp, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if(Objects.nonNull(timestamp)) {
            try {
                gen.writeString(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").format(timestamp)
                );
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
        }
    }
}
