package com.feitai.utils.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
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
    public static String doPost(String url, Object object) throws IOException {
        return doPostReturnResponse(url,object).body().string();
    }

    /**
     * okhttpclient以“application/json” post提交数据
     * @param url
     * @param object
     * @return Response
     * @throws IOException
     */
    public static Response doPostReturnResponse(String url,Object object) throws IOException{
        String json = JSON.toJSONString(object);
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    /**
     * okhttpclient通过formbody，post提交
     * @param url
     * @param body
     * @return
     */
    public static String doPostByFormBody(String url,FormBody body) throws IOException {
        return doPostByFormBodyReturnResponse(url,body).body().string();
    }

    /**
     * okhttpclient通过formbody，post提交,返回Response
     * @param url
     * @param body
     * @return Response
     * @throws IOException
     */
    public static Response doPostByFormBodyReturnResponse(String url,FormBody body) throws IOException{
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response;
    }

    /**
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static Response doGetReturnResponse(String url,Map<String,String> params) throws IOException{
        if (Objects.nonNull(params) && !params.isEmpty()){
            URLBuilder urlBuilder = new URLBuilder();
            urlBuilder.appendPath(url);
            for (Map.Entry<String,String> entry:params.entrySet()){
                urlBuilder.appendParam(entry.getKey(),entry.getValue());
            }
            return doGetReturnResonse(urlBuilder.toString());
        }
        log.warn("[params is null or empty,please use other method]");
        return null;
    }

    /**
     * okhttpclient 通过url 和 params 合拼,get提交方法
     * @param url
     * @param params 参数集
     * @return
     * @throws IOException
     */
    public static String doGet(String url, Map<String,String> params) throws IOException{

        if (Objects.nonNull(params) && !params.isEmpty()){
            URLBuilder urlBuilder = new URLBuilder();
            urlBuilder.appendPath(url);
            for (Map.Entry<String,String> entry:params.entrySet()){
                urlBuilder.appendParam(entry.getKey(),entry.getValue());
            }
            return doGet(urlBuilder.toString());
        }
        log.info("[params is null or empty,please use other method]");
        return null;
    }

    /**
     * okhttpclient通过url，get提交
     * @param url 传送url和参数集
     * @return
     */
    public static String doGet(String url) throws IOException {
        return doGetReturnResonse(url).body().string();
    }

    /**
     * okhttpclient通过url，get提交，返回Response对象
     * @param url
     * @return Response
     * @throws IOException
     */
    public static Response doGetReturnResonse(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    /**
     * okhttpclient通过设置Header与url，Get方法提交
     * @param url
     * @param headers 请求头
     * @return
     */
    public static String doGetByHeaders(String url,Headers headers) throws IOException {
        log.info("[]url:" + url + " headers:", headers);
        return doGetWithHeadersReturnResponse(url,headers).body().string();
    }

    /**
     * okhttpclient通过设置url与header,get提交，返回Response对象
     * @param url
     * @param headers
     * @return
     * @throws IOException
     */
    public static Response doGetWithHeadersReturnResponse(String url,Headers headers) throws IOException{
        Request request = new Request.Builder().url(url)
                .headers(headers)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    /**
     * okhttpclient通过设置url、params、Header，Get方法提交
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doGetWithHeaders(String url,Map<String,String> params,Headers headers) throws IOException{

        if (Objects.nonNull(params) && !params.isEmpty()){
            URLBuilder urlBuilder = new URLBuilder();
            urlBuilder.appendPath(url);
            for (Map.Entry<String,String> entry:params.entrySet()){
                urlBuilder.appendParam(entry.getKey(),entry.getValue());
            }
            return doGetByHeaders(urlBuilder.toString(),headers);
        }
        log.warn("[params is null or empty,please use other method]");
        return null;
    }

    /**
     * okhttpclient通过设置url、params、Header，Get方法提交，返回Response对象
     * @param url
     * @param params
     * @param headers
     * @return Response
     * @throws IOException
     */
    public static Response doGetWithHeadersReturnResponse(String url,Map<String,String> params,Headers headers) throws IOException{
        if (Objects.nonNull(params) && !params.isEmpty()){
            URLBuilder urlBuilder = new URLBuilder();
            urlBuilder.appendPath(url);
            for (Map.Entry<String,String> entry:params.entrySet()){
                urlBuilder.appendParam(entry.getKey(),entry.getValue());
            }
            return doGetWithHeadersReturnResponse(urlBuilder.toString(),headers);
        }
        log.warn("[params is null or empty,please use other method]");
        return null;
    }

    /**
     * okhttpclient 异步post提交object对象
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
     * @param url
     * @param json
     * @param callback 如果Callback为空则默认只打日志
     * @return
     */
    public static Call asyncPostJson(String url, String json, Callback callback) throws Exception {
        RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);
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
