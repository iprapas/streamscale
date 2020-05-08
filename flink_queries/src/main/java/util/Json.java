package util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Ankush on 17/07/17.
 */
public final class Json {

    //Testing

    private static final Logger log = LoggerFactory.getLogger(Json.class);
    private Json(){}

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static{
        //Omit all fields that have null value
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T toObject(final String json, final Class<T> clazz ) {
        try {
            return objectMapper.readValue(json,clazz);
        }
        catch (final IOException ex) {
            System.out.println(ex);
            return null;
        }
    }

    public static String toJson(final Object object)  {

        try {
            return objectMapper.writer().writeValueAsString(object);
        }
        catch (final JsonProcessingException ex) {
            log.error("", ex);
            return "";
        }
    }
}
