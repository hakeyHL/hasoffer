package hasoffer.core.test;

import hasoffer.base.model.Website;
import hasoffer.fetch.helper.WebsiteHelper;
import org.junit.Test;

/**
 * Created on 2016/6/27.
 */
public class UrlTest {

    @Test
    public void test1() {

        String url = "http://www.flipkart.com/lemon-b423/p/itmea6fpc94pt7v3?pid=MOBEA6FPNFX4PRED";
        Website webSite = WebsiteHelper.getWebSite(url);
        System.out.println(webSite);

    }

}
