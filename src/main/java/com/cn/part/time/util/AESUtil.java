package com.cn.part.time.util;

import com.cn.part.time.exception.MiniZhipinException;
import org.apache.hc.client5.http.utils.Base64;
import org.springframework.http.HttpStatus;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class AESUtil {
    private static final String AES_KEY_PREFIX = "qP2$bG9";
    private static final String AES_IV_SUBFIX = "A0^uW0:";

    /**
     * 解密使用AES/CBC/PKCS5Padding模式加密并Base64编码的字符串
     *
     * @param nodeName 节点名称
     * @param data     加密后的Base64编码字符串
     * @return 解密后的原始对象
     */
    public static String decrypt(String nodeName, String data) {
        try {
            // 将字符串形式的密钥和IV转换为字节数组
            byte[] keyBytes = (AES_KEY_PREFIX + nodeName).getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = (nodeName + AES_IV_SUBFIX).getBytes(StandardCharsets.UTF_8);

            // 创建AES密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            // 创建初始化向量规范
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

            // 获取Cipher实例，指定使用AES/CBC/PKCS5Padding模式
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // 初始化Cipher为解密模式，传入密钥和IV
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            // 将Base64编码的加密字符串解码为字节数组
            byte[] encryptedBytes = Base64.decodeBase64(data);
            // 执行解密操作
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            // 将解密后的字节数组转换为字符串并返回
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "Decryption failed.", e);
        }
    }

    /**
     * 使用AES/CBC/PKCS5Padding模式加密字符串
     *
     * @param nodeName 节点名称
     * @param data     加密前的字符串
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(String nodeName, String data) {
        try {
            // 生成与解密方法一致的密钥和IV
            byte[] keyBytes = (AES_KEY_PREFIX + nodeName).getBytes(StandardCharsets.UTF_8);
            byte[] ivBytes = (nodeName + AES_IV_SUBFIX).getBytes(StandardCharsets.UTF_8);

            // 创建AES密钥规范和初始化向量
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);

            // 初始化Cipher为加密模式
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            // 执行加密操作
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 返回Base64编码的加密结果
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            throw new MiniZhipinException(HttpStatus.BAD_REQUEST, "Encryption failed.", e);
        }
    }
}
