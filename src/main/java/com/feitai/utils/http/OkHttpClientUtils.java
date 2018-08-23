package com.feitai.utils.http;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class OkHttpClientUtils {

    public static final MediaType JSON_MEDIA_TYPE
            = MediaType.parse("application/json; charset=utf-8");

    public static final MediaType JSON_TYPE = MediaType.parse("application/json");

    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY_NOT_HEAD))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();


    /**
     * 以“application/json” post提交数据，提交对象为object
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
     * @param url
     * @param json
     * @return
     */
    public static String postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);
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
     * @param headers
     * @param url
     * @return
     */
    public static String getMethodByHeaders(String url,Headers headers) throws IOException {
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
     * @param body
     * @param url
     * @return
     */
    public static String postByFormBody(String url,FormBody body) throws IOException {
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        String resopStr = response.body().string();
        //log.info("postJson url<{}> body <{}> resp <{}>", url, body.toString(), resopStr);
        return resopStr;
    }
}
