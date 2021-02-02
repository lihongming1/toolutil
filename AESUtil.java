package com.sftc.isc.tsproduct.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES工具
 * 第三方工具
 * compile group: 'com.github.shalousun', name: 'common-util', version: '2.0.2'
 */
public class AESUtil {

    private static final String AES_KEY = "3xWTkhWb5MnMIVHZwREevUEb4c3Ny1EM";

    private static final String AES_IV = "cVpFeWlrWGR6Sk54Y3RjelNVZEROUT09";

    private static final int AES_KEY_SIZE = 32;

    private static final int AES_IV_SIZE = 16;


    public static void main(String[] args) throws Exception {

        String text = "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";

        String val = decryptShalousun(AES_KEY, AES_IV, text);

        System.out.println(val);

    }

    /**
     * 生成key,iv
     *
     * @return
     */
    public static String keyGenerator() {
        try {
            // 生成KEY
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGenerator.init(128, random);
            // 产生密钥key
            String key = new String(Base64.getEncoder().encode(keyGenerator.generateKey().getEncoded()));
            return key;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 加密
     *
     * @param key
     * @param iv
     * @param encryptStr
     * @return
     */
    public static String encrypt(String key, String iv, String encryptStr) throws Exception {
        byte[] keyByte = Base64.getDecoder().decode(key.getBytes());
        byte[] ivByte = Base64.getDecoder().decode(iv.getBytes());

        byte[] realKey = subBytes(keyByte, 0, AES_KEY_SIZE);
        byte[] realIv = subBytes(ivByte, 0, AES_IV_SIZE);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(realKey, "AES"), new IvParameterSpec(realIv));
        String encryptedVal = new String(Base64.getEncoder().encode(cipher.doFinal(encryptStr.getBytes())));
        return encryptedVal;
    }

    /**
     * shalousun工具加密
     *
     * @param key
     * @param iv
     * @param encryptStr
     * @return
     * @throws Exception
     */
    public static String encryptShalousun(String key, String iv, String encryptStr) throws Exception {
        byte[] keyByte = Base64.getDecoder().decode(key.getBytes());
        byte[] ivByte = Base64.getDecoder().decode(iv.getBytes());

        byte[] realKey = subBytes(keyByte, 0, AES_KEY_SIZE);
        byte[] realIv = subBytes(ivByte, 0, AES_IV_SIZE);

        byte[] data = encryptStr.getBytes();
        byte[] encrypt = com.power.common.util.AESUtil.encryptByCBC(data, realKey, realIv);
        String encryptedVal = new String(Base64.getEncoder().encode(encrypt));
        return encryptedVal;
    }

    /**
     * 解密
     *
     * @param key
     * @param iv
     * @param decryptStr
     * @return
     * @throws Exception
     */
    public static String decrypt(String key, String iv, String decryptStr) throws Exception {
        byte[] keyByte = Base64.getDecoder().decode(key.getBytes());
        byte[] ivByte = Base64.getDecoder().decode(iv.getBytes());

        byte[] realKey = subBytes(keyByte, 0, AES_KEY_SIZE);
        byte[] realIv = subBytes(ivByte, 0, AES_IV_SIZE);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(realKey, "AES"), new IvParameterSpec(realIv));
        byte[] data = Base64.getDecoder().decode(decryptStr);
        String decreptText = new String(cipher.doFinal(data));
        return decreptText;
    }

    /**
     * shalousun工具解密
     *
     * @param key
     * @param iv
     * @param decryptStr
     * @return
     * @throws Exception
     */
    public static String decryptShalousun(String key, String iv, String decryptStr) throws Exception {
        byte[] keyByte = Base64.getDecoder().decode(key.getBytes());
        byte[] ivByte = Base64.getDecoder().decode(iv.getBytes());

        byte[] realKey = subBytes(keyByte, 0, AES_KEY_SIZE);
        byte[] realIv = subBytes(ivByte, 0, AES_IV_SIZE);

        byte[] data = Base64.getDecoder().decode(decryptStr);
        byte[] decrypted = com.power.common.util.AESUtil.decryptByCBC(data, realKey, realIv);
        String decreptText = new String(decrypted);
        return decreptText;
    }

    /**
     * 截取数组，自动补0
     *
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < count; ++i) {
            if (i < src.length) {
                bs[i] = src[i];
            } else {
                bs[i] = 0;
            }
        }
        return bs;
    }

}

