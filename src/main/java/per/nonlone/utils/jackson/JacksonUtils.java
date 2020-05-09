package per.nonlone.utils.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import per.nonlone.utils.ObjectUtils;
import per.nonlone.utils.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jackson 工具类
 */
@Slf4j
public abstract class JacksonUtils {

    private static final ConcurrentHashMap<String, ObjectMapper> objectMapperRepository = new ConcurrentHashMap<>();

    public static ObjectMapper newInstance() {
        return new ObjectMapper();
    }

    public static ObjectMapper buildCacheInstance(ObjectMapperBuilder objectMapperBuilder) {
        return buildCacheInstance(ObjectUtils.getCallMethodName(3), objectMapperBuilder);
    }

    /**
     * 构建缓存ObjectMapper
     *
     * @param key
     * @param objectMapperBuilder
     * @return
     */
    public static ObjectMapper buildCacheInstance(String key, ObjectMapperBuilder objectMapperBuilder) {
        if (objectMapperRepository.containsKey(key)) {
            return objectMapperRepository.get(key);
        } else if (Objects.isNull(objectMapperRepository.putIfAbsent(key, objectMapperBuilder.build()))) {
            return objectMapperRepository.get(key);
        } else {
            return objectMapperRepository.get(key);
        }
    }

    /**
     * 设置defaultObjectMapper
     * @param objectMapperBuilder
     * @return
     */
    public static ObjectMapper setCachedDefaultInstance(ObjectMapperBuilder objectMapperBuilder){
        return buildCacheInstance(objectMapperBuilder);
    }

    /**
     * 获取缓存 普通ObjectMapper 实例
     *
     * @return
     */
    public static ObjectMapper getCachedDefaultInstance() {
        return buildCacheInstance(() -> getDefaultInstance());
    }

    /**
     * 获取 普通ObjectMapper 实例
     *
     * @return
     */
    public static ObjectMapper getDefaultInstance() {
        return new ObjectMapper() {{
            // 序列化忽略非空
            this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            // 只是反序列化提供下划线转驼峰，序列化还是驼峰
            this.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            // 处理空字符为空对象
            this.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            // 处理数组为空对象
            this.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
            // 忽略额外 json 结构
            this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }};
    }

    /**
     * 通过指定ObjectMapper json 转换
     *
     * @param objectMapper
     * @param map
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> T mapToObject(@NonNull ObjectMapper objectMapper, Map<String, Object> map, Class<T> classOfT) {
        return objectMapper.convertValue(map, classOfT);
    }

    /**
     * 通过指定 objectMapper json 转换
     *
     * @param objectMapper
     * @param map
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T mapToObject(@NonNull ObjectMapper objectMapper, Map<String, Object> map, Type type) {
        return objectMapper.convertValue(map, objectMapper.constructType(type));
    }

    /**
     * 使用 defaultObjectMapper json 转换
     *
     * @param map
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> classOfT) {
        return mapToObject(getCachedDefaultInstance(), map, classOfT);
    }

    /**
     * 使用 defaultObjectMapper json 转换
     *
     * @param map
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T mapToObject(Map<String, Object> map, Type type) {
        return mapToObject(getCachedDefaultInstance(), map, type);
    }


    /**
     * 使用 defaultObjectMapper json 转换
     *
     * @param jsonString
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> T stringToObject(String jsonString, Class<T> classOfT) throws IOException {
        return stringToObject(getCachedDefaultInstance(), jsonString, classOfT);
    }

    /**
     * json字符串转换
     *
     * @param objectMapper
     * @param jsonString
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> T stringToObject(ObjectMapper objectMapper, String jsonString, Class<T> classOfT) throws IOException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        return objectMapper.readValue(jsonString, classOfT);
    }

    /**
     * 使用 defaultObjectMapper json 转换
     *
     * @param jsonString
     * @param type
     * @return
     */
    public static <T> T stringToObject(String jsonString,Type type) throws IOException {
        return stringToObject(getCachedDefaultInstance(), jsonString, type);
    }

    public static <T> T stringToObject(ObjectMapper objectMapper, String jsonString, Type type) throws IOException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        return objectMapper.readValue(jsonString, objectMapper.constructType(type));
    }

    public static Map<String, Object> toJSONMap(Object object) {
        return toJSONMap(JacksonUtils.getCachedDefaultInstance(), object);
    }

    public static Map<String, Object> toJSONMap(@NotNull ObjectMapper objectMapper, Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    public static Map<String, Object> toJSONMap(String jsonString) throws IOException {
        return toJSONMap(JacksonUtils.getCachedDefaultInstance(), jsonString);
    }

    public static Map<String, Object> toJSONMap(@NonNull ObjectMapper objectMapper, String jsonString) throws IOException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        return objectMapper.readValue(jsonString, Map.class);
    }

    public static List<Object> toJSONArray(Object object) {
        return toJSONArray(JacksonUtils.getCachedDefaultInstance(), object);
    }

    public static List<Object> toJSONArray(@NotNull ObjectMapper objectMapper, Object object) {
        return objectMapper.convertValue(object, List.class);
    }

    public static List<Object> toJSONArray(String jsonString) throws IOException {
        return toJSONArray(JacksonUtils.getCachedDefaultInstance(), jsonString);
    }

    public static List<Object> toJSONArray(@NonNull ObjectMapper objectMapper, String jsonString) throws IOException {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        return objectMapper.readValue(jsonString, List.class);
    }

    public static String toJSONString(Object object) {
        return toJSONString(JacksonUtils.getCachedDefaultInstance(), object);
    }

    public static String toJSONString(@NonNull ObjectMapper objectMapper, Object object) {
        if (Objects.isNull(object)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException jpe) {
            log.error(String.format(" toJSONString error %s", jpe.getMessage()), jpe);
        }
        return null;
    }

    /**
     * 内部构建器
     */
    public interface ObjectMapperBuilder {
        ObjectMapper build();
    }


}
