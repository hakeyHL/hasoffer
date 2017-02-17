package hasoffer.core.utils.api;

import hasoffer.base.enums.MarketChannel;
import org.apache.commons.lang3.StringUtils;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2017年02月13日.
 * Time 12:12
 */
public class CipherUtil {
    private static final String SHA256ALGORITHM = "SHA-256";
    public static Map<MarketChannel, String> channelDefaultKeyMap = new HashMap<>();

    static {
        channelDefaultKeyMap.put(MarketChannel.GMOBI, "HRGI");
        channelDefaultKeyMap.put(MarketChannel.GMOBI_B, "HRGI");
    }

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

    /**
     * 验证key是否正确
     *
     * @param marketChannel
     * @param key
     * @param timeStamp
     * @return
     */
    public static boolean validationWithSHA256(MarketChannel marketChannel, String key, String timeStamp) {
        boolean isRight = false;
        if (marketChannel == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(timeStamp)) {
            return isRight;
        }
        //时间戳的有效期为5分钟
        try {
            if (StringUtils.isNumericSpace(timeStamp) && (new Date().getTime() - new SimpleDateFormat("yyyyMMddHHmmss").parse(timeStamp).getTime()) > 1000 * 60 * 5) {
                return isRight;
            }
        } catch (ParseException e) {

        }
        StringBuilder sb = new StringBuilder();
        sb.append(marketChannel.name()).append(timeStamp).append(channelDefaultKeyMap.get(marketChannel));
        if (key.equals(encryptWithSHA256(sb.toString()))) {
            isRight = true;
        }
        return isRight;
    }

    public static void main(String[] args) {
        String key = "GMOBI_B20170217093521HRGI";
        String s = encryptWithSHA256(key);
        System.out.println(s);
    }
}
