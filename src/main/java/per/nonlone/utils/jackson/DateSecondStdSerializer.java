package per.nonlone.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 时间以秒为单位序列化
 */
public class DateSecondStdSerializer extends StdSerializer<Date> {

    public DateSecondStdSerializer() {
        this(null);
    }

    public DateSecondStdSerializer(Class<Date> t) {
        super(t);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (Objects.nonNull(value)) {
            gen.writeObject(TimeUnit.SECONDS.convert(value.getTime(), TimeUnit.MILLISECONDS));
        }
    }
}
