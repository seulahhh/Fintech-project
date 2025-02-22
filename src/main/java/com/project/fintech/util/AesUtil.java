package com.project.fintech.util;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

    private static final String ALGORITHM = "AES";

    /**
     * AES-256 암호화
     *
     * @param key   암호화에 사용할 secretkey
     * @param value 암호화 할 문자열
     * @return 암호화하여 만들어진 문자열
     * @throws Exception
     */
    public static String encrypt(String key, String value) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES-256 복호화
     *
     * @param key            복호화에 사용할 secretkey
     * @param encryptedValue 복호화 하고자 하는 암호화된 문자열
     * @return 복호화하여 만들어진 문자열
     * @throws Exception
     */
    public static String decrypt(String key, String encryptedValue) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue);
        return new String(cipher.doFinal(decodedBytes));
    }

}
