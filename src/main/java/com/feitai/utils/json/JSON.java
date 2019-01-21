package com.feitai.utils.json;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class JSON extends com.alibaba.fastjson.JSON {

    /**
     * 递归序列化JSON
     *
     * @param data
     * @param classOfT
     * @param <T>
     * @return
     */
    public static <T> T parseObjectRecurrence(String data, Class<T> classOfT) {
        JSONObject jsonObject = (JSONObject) JSON.parse(data);
        Map<String, Object> jsonMap = parseRecurrence(jsonObject);
        return JSON.parseObject(JSON.toJSONString(jsonMap), classOfT);
    }

    /**
     * 递归尝试序列化JSON
     *
     * @param jsonMap
     * @return
     */
    private static Map<String, Object> parseRecurrence(Map<String, Object> jsonMap) {
        Map<String, Object> resultMap = new HashMap<>(jsonMap.size());
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            if (entry.getValue() instanceof String && ((String) entry.getValue()).startsWith("{")) {
                // 尝试判断是否为JSON
                try {
                    JSONObject tryJSONObject = (JSONObject) JSON.parse((String) entry.getValue());
                    Map<String, Object> tryJsonMap = parseRecurrence(tryJSONObject);
                    resultMap.put(entry.getKey(), tryJsonMap);
                } catch (JSONException jsone) {
                    log.debug("json parse error {} key<{}> value<{}>", jsone.getMessage(), entry.getKey(), entry.getValue());
                    // 回写原数据
                    resultMap.put(entry.getKey(), entry.getValue());
                }
            } else {
                resultMap.put(entry.getKey(), entry.getValue());
            }
        }
        return resultMap;
    }


}
