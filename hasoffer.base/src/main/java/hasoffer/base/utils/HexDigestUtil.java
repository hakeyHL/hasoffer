package hasoffer.base.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Date : 2016/1/7
 * Function :
 */
public class HexDigestUtil {

    public final static String md5(String s) {
        return new String(Hex.encodeHex(DigestUtils.md5(s)));
    }

    public final static String sha1(String s) {
        return new String(Hex.encodeHex(DigestUtils.sha1(s)));
    }

}
