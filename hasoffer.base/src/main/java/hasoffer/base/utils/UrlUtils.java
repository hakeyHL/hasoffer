package hasoffer.base.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Date : 2016/3/2
 * Function :
 */
public class UrlUtils {

    public static String getParam(String url, String q) {
        Map<String, String> querys = getParams(url);
        return querys.get(q);
    }

    public static Map<String, String> getParams(String url) {
        try {
            URL u = new URL(url);
            return getParamsByQuery(u.getQuery());
        } catch (Exception e) {
        }
        return new HashMap<String, String>();
    }

    public static Map<String, String> getParamsByQuery(String query) {
        query = StringUtils.urlDecode(query);
        Map<String, String> querys = new HashMap<String, String>();
        String[] qs = query.split("&");
        for (String qu : qs) {
            int index = qu.indexOf("=");
            if (index > 0) {
                querys.put(qu.substring(0, index), qu.substring(index + 1));
            }
        }
        return querys;
    }

    public static void main(String[] args) {
        String url = "http://mysmartprice.go2cloud.org/aff_c?offer_id=61&aff_id=2&aff_sub=electronics&aff_sub2=2016032325294&url=https://shopmonk.com/product/iphone-6s?variant=2504%26utm_source%3Dmysmartprice%26utm_medium%3Daffiliate";
        System.out.println(getParam(url, "url"));
    }

}
