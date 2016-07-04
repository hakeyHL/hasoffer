package hasoffer.core.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
        HttpHost proxy = new HttpHost("52.77.165.138", 8888, "http");

        HttpResponseModel responseModel = MyHttpUtils.getByProxy(url, proxy);

        System.out.println(responseModel.getBodyString());
    }

    @Test
    public void testHttp2() throws Exception {
        String url = "http://60.205.57.68:8888/analysis/t";

        Map<String, Object> formMap = new HashMap<String, Object>();
        formMap.put("title", "MapmyIndia ICENAV 301 IN-Dash AVN-Universal GPS Navigation Device");

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");

        HttpResponseModel responseModel = HttpUtils.post(url, formMap, headerMap);

        JSONObject jsObj = JSON.parseObject(responseModel.getBodyString());

        System.out.println(responseModel.getBodyString());
    }
}
