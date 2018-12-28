package com.feitai.utils.http;

import com.feitai.utils.ObjectUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * OkHttpClient 自定义拦截器，匹配 URL 正则 拦截执行
 */
@Slf4j
public class URLRegExpInterceptor implements Interceptor {

    /**
     * 拦截处理器
     */
    public interface InterceptorProcessor {

        Response process(Request request, Response response);

    }

    /**
     * 处理是否在Response之后
     */
    @Getter
    private static boolean afterResponseProcessed = true;

    /**
     * 正则表达式合集
     */
    @Getter
    private static String[] urlRegExps;

    /**
     * 命中处理链
     */
    private static List<InterceptorProcessor> processorList;

    /**
     * 简单构造器
     *
     * @param urlRegExps
     * @param processorList
     */
    public URLRegExpInterceptor(String[] urlRegExps, List<InterceptorProcessor> processorList) {
        this(urlRegExps, processorList, true);
    }

    /**
     * 完全构造处理
     *
     * @param urlRegExps
     * @param processorList
     * @param afterResponseProcessed
     */
    public URLRegExpInterceptor(String[] urlRegExps, List<InterceptorProcessor> processorList, boolean afterResponseProcessed) {
        URLRegExpInterceptor.urlRegExps = urlRegExps;
        URLRegExpInterceptor.processorList = processorList;
        URLRegExpInterceptor.afterResponseProcessed = afterResponseProcessed;
        Set<String> regExpSet = new HashSet<>();
        for (String regExp : urlRegExps) {
            if (!regExpSet.contains(regExp)) {
                PATTERN_SET.add(Pattern.compile(regExp));
                regExpSet.add(regExp);
            }
        }
    }


    private static final Set<Pattern> PATTERN_SET = new HashSet();

    private static final Map<String, Boolean> URL_MAP = new Hashtable<>();


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().toString();
        boolean isProcessed = false;
        // 缓存Map
        if (URL_MAP.containsKey(url)) {
            isProcessed = URL_MAP.get(url);
        } else {
            // 正则判断
            for (Pattern pattern : PATTERN_SET) {
                isProcessed = pattern.matcher(url).matches();
                if (isProcessed) {
                    break;
                }
            }
            // 放入缓存
            URL_MAP.put(url, isProcessed);
        }
        if (!isProcessed) {
            // 不记录日志
            try {
                return chain.proceed(request);
            } catch (Exception e) {
                log.error(String.format("http error %s", e.getMessage()), e);
                throw e;
            }
        }


        Response response = null;
        if (!isAfterResponseProcessed()) {
            for (InterceptorProcessor processor : processorList) {
                response = processor.process(request, response);
            }
            // 如果处理结果不是为空，则返回结果。
            if (response != null) {
                return response;
            }
        }
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log.error(String.format("http chain.proceed  error %s", e.getMessage()), e);
            throw e;
        }
        // response后处理
        if (isAfterResponseProcessed()) {
            for (InterceptorProcessor processor : processorList) {
                Response newResponse = processor.process(request, response);
                if (Objects.nonNull(newResponse)) {
                    response = newResponse;
                }
            }
        }

        return response;

    }
}
