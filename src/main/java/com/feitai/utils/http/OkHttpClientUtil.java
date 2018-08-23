package com.feitai.utils.http;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class OkHttpClientUtil {

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
    public static String postObject(String url, Object object) {
        String json = JSON.toJSONString(object);
        return OkHttpClientUtil.postJson(url, json);
    }

    /**
     * okhttpclient以“application/json” post提交数据，提交对象为json
     * @param url
     * @param json
     * @return
     */
    public static String postJson(String url, String json) {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String resopStr = response.body().string();
            //log.info("postJson url<{}> req <{}> resp <{}>", url, json, resopStr);
            return resopStr;
        } catch (IOException e) {
            log.error("Fail post url:[{}],Json:[{}]", url, json);
            return null;
        }
    }

    /**
     * okhttpclient通过url，get提交
     * @param url
     * @return
     */
    public static String getMethod(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String resopStr = response.body().string();
            //log.info("postJson url<{}> resp <{}>", url, resopStr);
            return resopStr;
        } catch (IOException e) {
            log.error("Fail get url:[{}]", url);
            return null;
        }
    }

    /**
     * okhttpclient通过设置Header与url，Get方法提交
     * @param headers
     * @param url
     * @return
     */
    public static String getMethodByHeaders(Headers headers, String url) {
        try {
            log.info("[]url:" + url + " headers:", headers);
            Request request = new Request.Builder().url(url)
                    .headers(headers)
                    .build();
            Response response = client.newCall(request).execute();
            String resopStr = response.body().string();
            //log.info("postJson url<{}> headers <{}> resp <{}>", url, headers.toString(), resopStr);
            return resopStr;
        } catch (IOException e) {
            log.error("");
            return null;
        }
    }

    /**
     * httpClient 通过formbody，post提交
     * @param body
     * @param url
     * @return
     */
    public static String postByFormBody(FormBody body, String url) {
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            String resopStr = response.body().string();
            //log.info("postJson url<{}> body <{}> resp <{}>", url, body.toString(), resopStr);
            return resopStr;
        } catch (IOException e) {
            log.error("url:" + url + " , body:" + body.toString() + " error:", e);
            return null;
        }
    }
}
