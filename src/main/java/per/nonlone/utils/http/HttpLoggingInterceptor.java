package per.nonlone.utils.http;

import lombok.Setter;
import okhttp3.*;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang.RandomStringUtils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 修改自官方的HttpLoggingInterceptor
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * {@linkplain OkHttpClient#interceptors() application interceptor} or as a {@linkplain
 * OkHttpClient#networkInterceptors() network interceptor}. <p> The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
public final class HttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final okhttp3.logging.HttpLoggingInterceptor.Logger logger;

    private volatile okhttp3.logging.HttpLoggingInterceptor.Level level = okhttp3.logging.HttpLoggingInterceptor.Level.NONE;

    @Setter
    private LogBeanSerializer logBeanSerializer = Object::toString;

    /**
     * json 序列化器
     */
    public interface LogBeanSerializer {
        <T> String serialize(T t);
    }

    public HttpLoggingInterceptor() {
        this(okhttp3.logging.HttpLoggingInterceptor.Logger.DEFAULT);
    }

    public HttpLoggingInterceptor(okhttp3.logging.HttpLoggingInterceptor.Logger logger) {
        this.logger = logger;
    }



    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLoggingInterceptor setLevel(okhttp3.logging.HttpLoggingInterceptor.Level level) {
        if (level == null) {
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        }
        this.level = level;
        return this;
    }

    public okhttp3.logging.HttpLoggingInterceptor.Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        String randomId = RandomStringUtils.randomAlphanumeric(5);

        okhttp3.logging.HttpLoggingInterceptor.Level level = this.level;

        Request request = chain.request();
        if (level == okhttp3.logging.HttpLoggingInterceptor.Level.NONE) {
            return chain.proceed(request);
        }

        boolean isLogBody = (level == okhttp3.logging.HttpLoggingInterceptor.Level.BODY );
        boolean isLogHeader = (isLogBody && level != okhttp3.logging.HttpLoggingInterceptor.Level.BODY) || level == okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        String requestMessage = "-->"
                + " " + randomId
                + " " + request.method()
                + " " + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!isLogHeader && hasRequestBody) {
            requestMessage += " (" + requestBody.contentLength() + "-byte body)";
        }

        Map<String,String> headerMap = new HashMap<>();

        if (isLogHeader) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    requestMessage += " Content-Type: " + requestBody.contentType();
                }
                if (requestBody.contentLength() != -1) {
                    requestMessage += " Content-Length: " + requestBody.contentLength();
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    headerMap.put(name,headers.value(i));
                }
            }
            requestMessage += String.format(" header<%s> ", logBeanSerializer.serialize(headerMap));
        }

        if (!isLogBody || !hasRequestBody) {
            requestMessage += " >> END " + request.method();
        } else if (bodyEncoded(request.headers())) {
            requestMessage += " >> END " + request.method() + " (encoded body omitted)";
        } else {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            //logger.log("");
            if (isPlaintext(buffer)) {
                //最多只读10240个字节
                String resp = buffer.readString(charset);
                if (resp.length() > 10240) {
                    resp = resp.substring(0, 10240) + "...";
                }
                requestMessage += " body<"+resp+">";
                requestMessage += " >> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)";
            } else {
                requestMessage += " >> END " + request.method() + " (binary "+ requestBody.contentLength() + "-byte body omitted)";
            }
        }

        // 输出日志
        logger.log(requestMessage);


        long startNs = System.nanoTime();
        String responseMessage = "";
        headerMap.clear();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("<-- "+randomId+" HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        responseMessage += "<--"
                + " " + randomId
                + " " + response.code()
                + (response.message().isEmpty() ? "" : " " + response.message())
                + " " + response.request().url()
                + " (" + tookMs + "ms" + (!isLogHeader ? ", " + bodySize + " body" : "") + ')';
        if (isLogHeader) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                headerMap.put(headers.name(i),headers.value(i));
            }
            responseMessage += String.format(" header<%s>", logBeanSerializer.serialize(headerMap));
        }

        if (!isLogBody || !HttpHeaders.hasBody(response)) {
            responseMessage += " << END HTTP";
        } else if (bodyEncoded(response.headers())) {
            requestMessage += " << END HTTP (encode`d body omitted)";
        } else {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (!isPlaintext(buffer)) {
                responseMessage += " << END HTTP (binary " + buffer.size() + "-byte body omitted)";
                logger.log(responseMessage);
                return response;
            }

            if (contentLength != 0) {
                //logger.log("");
                //最多只读1024个字节
                String resp = buffer.clone().readString(charset);
                if (resp.length() > 10240) {
                    resp = resp.substring(0, 10240) + "...";
                }

                responseMessage += " body<"+resp+">";
            }
            responseMessage += " << END HTTP (" + buffer.size() + "-byte body)";
        }

        logger.log(responseMessage);
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }
}