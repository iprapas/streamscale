package deserializers;


import domain.Click;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Json;

import java.io.IOException;
import java.util.Objects;


public class ClickJsonDeserializer implements DeserializationSchema<Click> {

    private static final Logger log = LoggerFactory.getLogger(ClickJsonDeserializer.class);

    @Override
    public Click deserialize(final byte[] bytes) throws IOException {
        if (Objects.isNull(bytes)) {
            return null;
        }
        Click click = null;
        try {
            String json = new String(bytes);
            click = Json.toObject(json, Click.class);
        } catch (final Exception ex) {
            log.error("", ex);
        }
        return click;
    }

    @Override
    public boolean isEndOfStream(final Click nextElement) {
        return false;
    }

    @Override
    public TypeInformation<Click> getProducedType() {
        return PojoTypeInfo.of(Click.class);
    }
}
