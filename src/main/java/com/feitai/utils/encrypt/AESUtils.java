package com.feitai.utils.encrypt;

import com.feitai.utils.encode.Base64Utils;
import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;

/**
 * AES 加密类
 */
@Slf4j
@SuppressWarnings("AlibabaRemoveCommentedCode")
public abstract class AESUtils {

    /**
     * 默认加密Key
     */
    public final static String DEFAULT_KEY = "!@#$%^&*(";

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * 获得密钥
     *
     * @param secretKey
     * @return
     * @throws Exception
     */
    private static SecretKey generateKey(String secretKey) throws NoSuchAlgorithmException {
        //防止linux下 随机生成key
        Provider p = Security.getProvider("SUN");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", p);
        secureRandom.setSeed(secretKey.getBytes());
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(secureRandom);
        // 生成密钥
        return kg.generateKey();
    }


    /**
     * AES 加密
     *
     * @param content
     * @param key
     * @return
     */
    public static byte[] aesEncrypt(String content, String key) throws GeneralSecurityException, UnsupportedEncodingException {
        if (StringUtils.isBlank(key)) {
            key = DEFAULT_KEY;
        }
        SecretKey secretKey = generateKey(key);
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        byte[] byteContent = content.getBytes("utf-8");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher.doFinal(byteContent);
    }

    /**
     * AES 加密 Base64 编码
     *
     * @param content
     * @param key
     * @return
     * @throws GeneralSecurityException
     * @throws UnsupportedEncodingException
     */
    public static String aesEncryptToBase64(String content, String key) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] aesEncrypt = aesEncrypt(content, key);
        if(log.isDebugEnabled()) {
            log.debug("aesEncrypt String:" + Arrays.toString(aesEncrypt));
        }
        String base64EncodeStr = Base64Utils.encodeFromByteArray(aesEncrypt);
        if(log.isDebugEnabled()) {
            log.debug("aesEncryptToBase64 base64EncodeStr:" + base64EncodeStr);
        }
        return base64EncodeStr;
    }

    /**
     * AES 解密
     *
     * @param content
     * @param key
     * @return
     * @throws GeneralSecurityException
     */
    public static byte[] aesDecrypt(byte[] content, String key) throws GeneralSecurityException {
        if (StringUtils.isBlank(key)) {
            key = DEFAULT_KEY;
        }
        SecretKey secretKey = generateKey(key);
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(content);
    }

    public static byte[] aesEncryptWithECBAndPKCS7(String content, String pkey) throws UnsupportedEncodingException, GeneralSecurityException {
        SecretKeySpec key = new SecretKeySpec(pkey.getBytes(), "AES");
        // "算法/模式/补码方式"
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        //IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
        // 加密
        return encrypted;
    }

    public static String aesEncryptWithECBAndPKCS7ToBase64(String content, String pkey) throws UnsupportedEncodingException, GeneralSecurityException {
        byte[] aesEncrypt = aesEncryptWithECBAndPKCS7(content, pkey);
        if(log.isDebugEnabled()) {
            log.debug("aesEncryptWithECBAndPKCS7 String:" + Arrays.toString(aesEncrypt));
        }
        String base64EncodeStr = BaseEncoding.base64().encode(aesEncrypt);
        if(log.isDebugEnabled()) {
            log.debug("aesEncryptWithECBAndPKCS7ToBase64 base64EncodeStr:" + base64EncodeStr);
        }
        return base64EncodeStr;
    }


    /**
     * 解密 128位
     *
     * @param content 待解密内容
     * @param pkey    解密密钥
     * @return
     */
    public static byte[] aesDecryptWithECBAndPKCS7(byte[] content, String pkey) throws UnsupportedEncodingException, GeneralSecurityException {
        SecretKeySpec key = new SecretKeySpec(pkey.getBytes(), "AES");
        // 创建密码器
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
        // 初始化
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] result = cipher.doFinal(content);
        // 解密
        return result;
    }

    /**
     * @param content base64处理过的字符串
     * @param pkey
     * @return String    返回类型
     * @throws Exception
     * @throws
     * @Title: aesDecodeStr
     * @Description: 解密 失败将返回NULL
     */
    public static String aesDecryptWithECBAndPKCS7FromBase64(String content, String pkey) throws UnsupportedEncodingException, GeneralSecurityException {
        byte[] base64DecodeStr = BaseEncoding.base64().decode(content);
        if(log.isDebugEnabled()) {
            log.debug("aesDecryptWithECBAndPKCS7 String:" + Arrays.toString(base64DecodeStr));
        }
        byte[] aesDecode = aesDecryptWithECBAndPKCS7(base64DecodeStr, pkey);
        if (aesDecode == null) {
            return null;
        }
        String result = new String(aesDecode, "UTF-8");
        if(log.isDebugEnabled()) {
            log.debug("aesDecryptWithECBAndPKCS7FromBase64 result:" + result);
        }
        return result;
    }

}
