package hasoffer.core.test;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.base.utils.http.MyHttpUtils;
import org.apache.http.HttpHost;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

    @Test
    public void testHttp2() throws Exception {
        String url = "http://localhost:8080/analysis/t";

        Map<String, Object> formMap = new HashMap<String, Object>();
        formMap.put("title", "");

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");

        HttpResponseModel responseModel = HttpUtils.post(url, formMap, headerMap);

        System.out.println(responseModel.getBodyString());
    }
}
