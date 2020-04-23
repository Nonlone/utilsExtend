package per.nonlone.utilsExtend.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import per.nonlone.utilsExtend.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class DateLongDeserializer extends StdDeserializer<Date> {
    public DateLongDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (Objects.nonNull(jsonParser) && StringUtils.isNotBlank(jsonParser.getText())) {
            Long timestamp = Long.parseLong(jsonParser.getText());
            return new Date(timestamp);
        }
        return null;
    }
}
