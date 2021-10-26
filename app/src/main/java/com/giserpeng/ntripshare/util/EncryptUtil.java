package com.giserpeng.ntripshare.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * 3DES加密工具类
 *
 * @author shijing
 * @DATE 2020/9/15 11:22
 */
public class EncryptUtil {
    /**
     * 随意定一个私钥（长度必须为24位）
     */
    public static final String SECRET_KEY = "NTRIPSHARE20201234567890";
    /**
     * 加密
     *
     * @param inStr     需要加密的内容
     * @return 加密后的数据
     */
    public static String encrypt(String inStr) {
        SecretKey deskey = new SecretKeySpec(SECRET_KEY.getBytes(), "DESede");
        Cipher cipher;
        String outStr = null;
        try {
            cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, deskey);
            outStr = byte2hex(cipher.doFinal(inStr.getBytes()));
        } catch (Exception e) {
            System.err.println("3DES加密异常"+ e.getMessage());
        }
        System.out.println("3DES加密后字符串：" + outStr);
        return outStr;
    }

    /**
     * 解密
     *
     * @param inStr     需要解密的内容
     * @return 解密后的数据
     */

    public static String decrypt(String inStr) {
        SecretKey deskey = new SecretKeySpec(SECRET_KEY.getBytes(), "DESede");
        Cipher cipher;
        String outStr = null;

        try {
            cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            outStr = new String(cipher.doFinal(hex2byte(inStr)));
        } catch (Exception e) {
            System.err.println("3DES解密异常"+e.getMessage());
        }
        System.out.println("3DES解密后数据：" + outStr);
        return outStr;
    }

    /**
     * 转化为16进制字符串方法
     *
     * @param digest 需要转换的字节组
     * @return 转换后的字符串
     */

    private static String byte2hex(byte[] digest) {
        StringBuffer hs = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < digest.length; n++) {
            stmp = Integer.toHexString(digest[n] & 0XFF);
            if (stmp.length() == 1) {
                hs.append("0" + stmp);
            } else {
                hs.append(stmp);
            }
        }
        return hs.toString().toUpperCase();
    }

    /**
     * 十六进转二进制
     *
     * @param hexStr 待转换16进制字符串
     * @return 二进制字节组
     */
    public static byte[] hex2byte(String hexStr) {
        if (hexStr == null)
            return null;
        hexStr = hexStr.trim();
        int len = hexStr.length();
        if (len == 0 || len % 2 == 1)
            return null;
        byte[] digest = new byte[len / 2];
        try {
            for (int i = 0; i < hexStr.length(); i += 2) {
                digest[i / 2] = (byte) Integer.decode("0x" + hexStr.substring(i, i + 2)).intValue();
            }
            return digest;
        } catch (Exception e) {
            return null;
        }
    }
}