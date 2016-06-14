package hasoffer.core.test;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.http.MyHttpUtils;
import org.apache.http.HttpHost;
import org.junit.Test;

/**
 * Date : 2016/5/31
 * Function :
 */
public class HttpTest {
    @Test
    public void testHttp() throws Exception {
        String url = "https://www.snapdeal.com/product/samsung-galaxy-s5-shimmery-white/922485437";

//        HttpHost proxy = new HttpHost("175.142.198.36", 80, "http");
        HttpHost proxy = new HttpHost("110.78.145.199", 8080, "http");

        HttpResponseModel responseModel = MyHttpUtils.getByProxy(url, proxy);

        System.out.println(responseModel.getBodyString());
    }
}
