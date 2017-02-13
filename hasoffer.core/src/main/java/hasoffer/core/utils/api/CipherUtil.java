package hasoffer.core.utils.api;

import java.security.MessageDigest;

/**
 * Created by hs on 2017年02月13日.
 * Time 12:12
 */
public class CipherUtil {
    private static final String SHA256ALGORITHM = "SHA-256";

    public static String encryptWithSHA256(String content) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(SHA256ALGORITHM);
            byte[] plainText = content.getBytes("UTF-8");
            md.update(plainText);
        } catch (Exception e) {

        }
        byte bytes[] = md.digest();
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xff);
            buff.append(hexString.length() == 2 ? hexString : "0" + hexString);
        }
        return buff.toString();
    }
}
