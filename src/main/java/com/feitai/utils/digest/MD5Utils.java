package com.feitai.utils.digest;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;

/**
 * MD5摘要算法
 */
@Slf4j
public abstract  class MD5Utils {

    /**
     * 生成MD5
     *
     * @param data
     * @return
     */
    public static String generate(String data) {
        String md5 = "";
        try {
            // 创建一个md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageByte = data.getBytes("UTF-8");
            // 获得MD5字节数组,16*8=128位
            byte[] md5Byte = md.digest(messageByte);
            // 转换为16进制字符串
            md5 = bytesToHex(md5Byte);
        } catch (Exception e) {
            log.error(String.format("md5 error %s", e.getMessage()));
        }
        return md5;
    }

    // 二进制转十六进制
    protected static String bytesToHex(byte[] bytes) {
        StringBuffer hexStr = new StringBuffer();
        int num;
        for (int i = 0; i < bytes.length; i++) {
            num = bytes[i];
            if (num < 0) {
                num += 256;
            }
            if (num < 16) {
                hexStr.append("0");
            }
            hexStr.append(Integer.toHexString(num));
        }
        return hexStr.toString().toUpperCase();
    }

}
