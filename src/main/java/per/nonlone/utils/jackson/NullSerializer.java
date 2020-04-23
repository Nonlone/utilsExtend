package per.nonlone.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;
import per.nonlone.utils.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

@Slf4j
public class NullSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        Class<?> clazz = jsonGenerator.getCurrentValue().getClass();
        String name = jsonGenerator.getOutputContext().getCurrentName();
        Field field = ObjectUtils.getAccessibleField(clazz, name);
        if (Objects.isNull(field)) {
            log.warn(String.format("field not exist class<%s> key<%s>", clazz.getName(), name));
            jsonGenerator.writeString("");
        } else {
            Class<?> fieldClass = field.getType();
            if (fieldClass.isArray() || Collection.class.isAssignableFrom(fieldClass)) {
                jsonGenerator.writeStartArray();
                jsonGenerator.writeEndArray();
            } else {
                jsonGenerator.writeString("");
            }
        }
    }
}
