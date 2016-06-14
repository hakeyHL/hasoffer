package hasoffer.base.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Created by glx on 2015/5/22.
 */
public class SystemUtil {
    private static final Logger logger = LoggerFactory.getLogger(SystemUtil.class);
    private static Random random = new Random();

    public static String getIpAsHex(byte[] address) {
        if (address == null) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        for (byte b : address) {
            String hex = Integer.toHexString(b);
            logger.debug(hex);
            if (hex.length() == 1) {
                buffer.append("0" + hex);
            } else {
                buffer.append(hex.substring(hex.length() - 2));
            }
        }

        return buffer.toString().toUpperCase();
    }

    public static String getLocalAddressAsHex() {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (InetAddress address : addresses) {
                if (address.isSiteLocalAddress()) {
                    return getIpAsHex(address.getAddress());
                }
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return "";
    }

    public static String generateSecurityCode(int length) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append(random.nextInt(10));
        }

        return buffer.toString();
    }

    public static String getAppEnv() {
        String appEnv = System.getenv("APP_ENV");

        if (StringUtils.isBlank(appEnv)) {
            appEnv = StringUtils.stripToEmpty(System.getProperty("APP_ENV"));
        }

        if (StringUtils.isNotEmpty(appEnv)) {
            logger.info("find APP_ENV=" + appEnv);
            return appEnv;
        } else {
            logger.info("do not find APP_ENV, use \"default\"");
            return "default";
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(getLocalAddressAsHex());

    }

}
