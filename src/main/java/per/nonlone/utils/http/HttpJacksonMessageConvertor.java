package per.nonlone.utils.http;

import lombok.extern.slf4j.Slf4j;
import per.nonlone.utils.jackson.JacksonUtils;

import java.io.IOException;
import java.lang.reflect.Type;

@Slf4j
public class HttpJacksonMessageConvertor implements OkHttpClientUtils.MessageConvertor {

    @Override
    public <T> String serialize(T t) {
        return JacksonUtils.toJSONString(t);
    }

    @Override
    public <T> T deserialize(String responseBody, Class<T> classOfT) {
        try {
            JacksonUtils.stringToObject(responseBody,classOfT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public <T> T deserialize(String responseBody, Type type) {
        try {
            JacksonUtils.stringToObject(responseBody,type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
