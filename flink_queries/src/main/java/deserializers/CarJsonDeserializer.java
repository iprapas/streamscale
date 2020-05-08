package deserializers;

import domain.Car;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Json;

import java.io.IOException;
import java.util.Objects;


public class CarJsonDeserializer implements DeserializationSchema<Car> {

    private static final Logger log = LoggerFactory.getLogger(ClickJsonDeserializer.class);

    @Override
    public Car deserialize(final byte[] bytes) throws IOException {
        if (Objects.isNull(bytes)) {
            return null;
        }
        Car car = null;
        try {
            String json = new String(bytes);
            car = Json.toObject(json, Car.class);
        } catch (final Exception ex) {
            log.error("", ex);
        }
        return car;
    }

    @Override
    public boolean isEndOfStream(final Car nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Car> getProducedType() {
        return PojoTypeInfo.of(Car.class);
    }
}
