package per.nonlone.utilsExtend.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import per.nonlone.utilsExtend.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 时间 以秒为单位 返序列化
 */
public class DateSecondStdDeserializer extends StdDeserializer<Date> {


    public DateSecondStdDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        if (Objects.nonNull(p) && StringUtils.isNotBlank(p.getText())) {
            return new Date(TimeUnit.MILLISECONDS.convert(Long.parseLong(p.getText()), TimeUnit.SECONDS));
        }
        return null;
    }
}
