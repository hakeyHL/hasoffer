package hasoffer.core.app.website;

import hasoffer.core.utils.Httphelper;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.selector.Html;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2017年03月09日.
 * Time 16:37
 */
public class ApiAmazonHelper {
    private static List getFlipKartSkuListByTitleSearch(String title) {
        List skuList = new LinkedList();
        String searchUrl = "http://www.amazon.in/s/ref=nb_sb_noss_2?url=search-alias%3Daps&field-keywords=";
        Html html = null;
        if (StringUtils.isNotEmpty(title)) {
            try {
                Map headers = new HashMap();
                headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
                title = URLEncoder.encode(title, "utf-8");
                String responseString = Httphelper.doGetWithHeaer(searchUrl + title, null);
                if (responseString != null) {
                    System.out.println("get");
                    html = new Html(responseString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (html != null) {

            }

        }
        return skuList;
    }

    public static void main(String[] args) {
        getFlipKartSkuListByTitleSearch("iphone");
    }
}
