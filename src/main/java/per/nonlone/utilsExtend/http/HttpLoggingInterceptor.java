package per.nonlone.utilsExtend.http;

import okhttp3.*;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang.RandomStringUtils;
import per.nonlone.utilsExtend.jackson.JacksonUtils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static okhttp3.internal.platform.Platform.INFO;

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

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY_WITH_HEADER,
        /**
         * Logs request and response lines and their respective bodies (if present).
         *
         * <p>Example:
         * <pre>{@code
         * -->
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY_NOT_HEAD

    }

    public interface Logger {

        void log(String message);

        /**
         * A {@link HttpLoggingInterceptor.Logger} defaults output appropriate for the current platform.
         */
        HttpLoggingInterceptor.Logger DEFAULT = new HttpLoggingInterceptor.Logger() {

            @Override
            public void log(String message) {
                Platform.get().log(INFO, message, null);
            }

        };
    }

    public HttpLoggingInterceptor() {
        this(HttpLoggingInterceptor.Logger.DEFAULT);
    }

    public HttpLoggingInterceptor(HttpLoggingInterceptor.Logger logger) {
        this.logger = logger;
    }

    private final HttpLoggingInterceptor.Logger logger;

    private volatile HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.NONE;

    /**
     * Change the level at which this interceptor logs.
     */
    public HttpLoggingInterceptor setLevel(HttpLoggingInterceptor.Level level) {
        if (level == null) {
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        }
        this.level = level;
        return this;
    }

    public HttpLoggingInterceptor.Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        String randomId = RandomStringUtils.randomAlphanumeric(5);

        HttpLoggingInterceptor.Level level = this.level;

        Request request = chain.request();
        if (level == HttpLoggingInterceptor.Level.NONE) {
            return chain.proceed(request);
        }

        boolean logBody = (level == HttpLoggingInterceptor.Level.BODY_WITH_HEADER || level == Level.BODY_NOT_HEAD);
        boolean logHeaders = (logBody && level != Level.BODY_NOT_HEAD) || level == HttpLoggingInterceptor.Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        String requestMessage = "-->"
                + " " + randomId
                + " " + request.method()
                + " " + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestMessage += " (" + requestBody.contentLength() + "-byte body)";
        }

        Map<String,String> headerMap = new HashMap<>();

        if (logHeaders) {
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
            requestMessage += String.format(" header<%s> ", JacksonUtils.toJSONString(headerMap));
        }

        if (!logBody || !hasRequestBody) {
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
                + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')';
        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                headerMap.put(headers.name(i),headers.value(i));
            }
            responseMessage += String.format(" header<%s>", JacksonUtils.toJSONString(headerMap));
        }

        if (!logBody || !HttpHeaders.hasBody(response)) {
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