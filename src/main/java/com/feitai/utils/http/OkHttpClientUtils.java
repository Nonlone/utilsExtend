package com.feitai.utils.http;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class OkHttpClientUtils {

    public static final MediaType JSON_TYPE_UTF_8 = MediaType.parse("application/json; charset=utf-8");

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
    public static String postObject(String url, Object object) throws IOException {
        String json = JSON.toJSONString(object);
        return OkHttpClientUtils.postJson(url, json);
    }

    /**
     * okhttpclient以“application/json” post提交数据，提交对象为json
     *
     * @param url
     * @param json
     * @return
     */
    public static String postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON_TYPE_UTF_8, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String resopStr = response.body().string();
        //log.info("postJson url<{}> req <{}> resp <{}>", url, json, resopStr);
        return resopStr;
    }

    /**
     * okhttpclient通过url，get提交
     *
     * @param url
     * @return
     */
    public static String getMethod(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String resopStr = response.body().string();
        //log.info("postJson url<{}> resp <{}>", url, resopStr);
        return resopStr;
    }

    /**
     * okhttpclient通过设置Header与url，Get方法提交
     *
     * @param headers
     * @param url
     * @return
     */
    public static String getMethodByHeaders(String url, Headers headers) throws IOException {
        log.info("[]url:" + url + " headers:", headers);
        Request request = new Request.Builder().url(url)
                .headers(headers)
                .build();
        Response response = client.newCall(request).execute();
        String resopStr = response.body().string();
        //log.info("postJson url<{}> headers <{}> resp <{}>", url, headers.toString(), resopStr);
        return resopStr;
    }

    /**
     * httpClient 通过formbody，post提交
     *
     * @param body
     * @param url
     * @return
     */
    public static String postByFormBody(String url, FormBody body) throws IOException {
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        String resopStr = response.body().string();
        //log.info("postJson url<{}> body <{}> resp <{}>", url, body.toString(), resopStr);
        return resopStr;
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
        RequestBody body = RequestBody.create(JSON_TYPE_UTF_8, json);
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
    static class LogCallBack implements Callback {
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
