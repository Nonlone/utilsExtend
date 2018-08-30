package com.feitai.utils.http;

import com.alibaba.fastjson.JSON;
import com.feitai.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class OkHttpClientUtils {

    public static final MediaType JSON_TYPE_UTF8
            = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType JSON_TYPE = MediaType.parse("application/json");

    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY_NOT_HEAD))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 以“application/json” post提交数据，提交对象为object
     *
     * @param url
     * @param object
     * @return
     */
    public static String postByBody(@NotNull String url,@NotNull Object object) throws IOException {
        return post(url, object).body().string();
    }

    /**
     * okhttpclient以“application/json” post提交数据
     *
     * @param url
     * @param object
     * @return Response
     * @throws IOException
     */
    public static Response post(@NotNull String url, @NotNull Object object) throws IOException {
        String json = JSON.toJSONString(object);
        RequestBody body = RequestBody.create(JSON_TYPE_UTF8, json);
        return post(url,body,null);
    }

    /**
     * okhttpclient通过formbody，post提交
     *
     * @param url
     * @param body
     * @return
     */
    public static String postByBody(@NotNull String url, FormBody body) throws IOException {
        return post(url,body,null).body().string();
    }

    /**
     * okhttpclient通过formbody，post提交,返回Response
     *
     * @param url
     * @param body
     * @return Response
     * @throws IOException
     */
    public static Response post(@NotNull String url, FormBody body) throws IOException {
        return post(url,body,null);
    }

    /**
     * OKhttpClient post提交方法
     * @param url
     * @param body
     * @param headers
     * @return
     * @throws IOException
     */
    public static Response post(@NotNull String url,RequestBody body,Headers headers)throws IOException{
        //这里获取url数据
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
        if (Objects.nonNull(body)){
            log.warn("[RequestBody not null , please use another method]");
            return null;
        }
        Request.Builder builder = new Request.Builder().url(httpUrlBuilder.build());
        if (Objects.nonNull(headers)){
            builder.headers(headers);
        }
        //这里RequestBody已经包含了MediaType
        builder.post(body);
        return client.newCall(builder.build()).execute();
    }

    /**
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static Response get(@NotNull String url, Map<String, String> params) throws IOException {
        return get(url,params,null);
    }

    /**
     * okhttpclient 通过url 和 params 合拼,get提交方法
     *
     * @param url
     * @param params 参数集
     * @return
     * @throws IOException
     */
    public static String getByBody(String url, Map<String, String> params) throws IOException {
        return get(url,params,null).body().string();
    }

    /**
     * okhttpclient通过url，get提交
     *
     * @param url 传送url和参数集
     * @return
     */
    public static String getByBody(@NotNull String url) throws IOException {
        return get(url,null,null).body().string();
    }

    /**
     * okhttpclient通过url，get提交，返回Response对象
     *
     * @param url
     * @return Response
     * @throws IOException
     */
    public static Response doGetReturnResonse(@NotNull String url) throws IOException {
        return get(url,null,null);
    }

    /**
     * okhttpclient通过设置Header与url，Get方法提交
     *
     * @param url
     * @param headers 请求头
     * @return
     */
    public static String getByBody(String url, Headers headers) throws IOException {
        return get(url,null,headers).body().string();
    }

    /**
     * okhttpclient通过设置url与header,get提交，返回Response对象
     *
     * @param url
     * @param headers
     * @return
     * @throws IOException
     */
    public static Response get(@NotNull String url, Headers headers) throws IOException {
        return get(url,null,headers);
    }

    /**
     * okhttpclient通过设置url、params、Header，Get方法提交
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static Response get(@NotNull String url, Map<String, String> params, Headers headers) throws IOException {
        log.warn("[params is null or empty,please use other method]");
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        if (headers != null) {
            requestBuilder.headers(headers);
        }
        return client.newCall(requestBuilder.build()).execute();
    }

    /**
     * okhttpclient 异步post提交object对象
     *
     * @param url
     * @param object
     * @param callback 如果Callback为空则默认只打日志
     * @return
     */
    public static Call asyncPostObject(String url, Object object, Callback callback) throws Exception {
        String json = JSON.toJSONString(object);
        return OkHttpClientUtils.asyncPostJson(url, json, callback);
    }


    /**
     * okhttpclient 异步post提交json对象
     *
     * @param url
     * @param json
     * @param callback 如果Callback为空则默认只打日志
     * @return
     */
    public static Call asyncPostJson(String url, String json, Callback callback) throws Exception {
        RequestBody body = RequestBody.create(JSON_TYPE_UTF8, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        if (callback == null) {
            callback = new LogCallBack();
        }
        call.enqueue(callback);

        log.info("Async request! Url:{}, FromBody:{}", url, body);
        return call;
    }

    /**
     * okhttpclient 异步post提交formbody对象
     *
     * @param url
     * @param body
     * @param callback 如果Callback为空则默认只打日志
     * @return
     */
    public static Call asyncPostFromBody(String url, FormBody body, Callback callback) throws IOException {
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = client.newCall(request);
        if (callback == null) {
            callback = new LogCallBack();
        }
        call.enqueue(callback);
        log.info("Async request! Url:{}, body:{}", url, body);
        return call;
    }

    @Slf4j
    public static class LogCallBack implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            //TODO 这body有空得看下是否正确
            log.error("Url:<" + call.request().url() + ">, body:<" + call.request().body() + ">", e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            InputStream is = response.body().byteStream();
            String result = IOUtils.toString(is, "UTF-8");
            log.info("Url:{}, FromBody:{}, response: {}", call.request().url(), call.request().body(), result);
        }
    }
}