package hasoffer.core.app.website;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import hasoffer.core.utils.Httphelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2017年03月09日.
 * Time 16:37
 */
public class ApiFlipkartHelper {
    private static final String FLIPKARTBASEURL = "https://www.flipkart.com/api/1/product/smart-browse";
    private static final String requestUrl = "https://www.flipkart.com/api/3/page/dynamic/product";
    static Logger logger = LoggerFactory.getLogger(ApiFlipkartHelper.class);

    public static List getFlipKartSkuListByTitleSearch(String title) {
        List<JSONObject> skuList = new LinkedList<>();
        JSONObject postJsonObj = new JSONObject();
        postJsonObj.put("store", "search.flipkart.com");
        postJsonObj.put("start", "0");
        postJsonObj.put("disableProductData", true);
        postJsonObj.put("count", 10);
        postJsonObj.put("q", title);
        Map dataMap = new HashMap();
        dataMap.put("requestContext", postJsonObj);
        try {
            HashMap headers = new HashMap<>();
            headers.put("x-user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36 FKUA/website/41/website/Desktop");
            headers.put("content-type", "application/json");
            System.out.println(JSON.toJSONString(dataMap, SerializerFeature.PrettyFormat));
            String bodyString = Httphelper.doPostJsonWithHeader(FLIPKARTBASEURL, JSON.toJSONString(dataMap), headers);
            if (bodyString != null) {
                JSONObject jsonObject = JSONObject.parseObject(bodyString);
                //获取的属性有title,link,price,imageUrl(240*240 质量50)
                JSONObject responseObj = jsonObject.getJSONObject("RESPONSE");
                JSONArray productList = responseObj.getJSONObject("pageContext").getJSONObject("searchMetaData").getJSONObject("storeSearchResult").getJSONObject("tyy").getJSONArray("productList");
                String[] idStringList = productList.toArray(new String[]{});
                for (int i = 0; i < idStringList.length; i++) {
                    String id = idStringList[i];
                    //根据id获取数据
                    skuList.add(getSkuByIdFromFlipkart(id));
                }
            }
        } catch (Exception e) {
            logger.error("error occured while do post 2 flipkart product interface.{}", e.getMessage());
            e.printStackTrace();
        }
        System.out.println(JSON.toJSONString(skuList, SerializerFeature.PrettyFormat));
        return skuList;
    }

    /**
     * @param flipkartId sku在flipkart的id
     * @return
     */
    private static JSONObject getSkuByIdFromFlipkart(String flipkartId) throws Exception {
        JSONObject jsonObject = new JSONObject();
        //链接,图片,title,价格
        String json = "{\"requestContext\":{\"productId\":\"" + flipkartId + "\"}}";

        Map<String, String> header = new HashMap<>();

        header.put("x-user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 FKUA/website/41/website/Desktop");

        String response = Httphelper.doPostJsonWithHeader(requestUrl, json, header);

        JSONObject product_summary_1 = JSONObject.parseObject(response.trim()).getJSONObject("RESPONSE").getJSONObject("data").getJSONObject("product_summary_1");

        if (product_summary_1 != null) {
            JSONObject data = product_summary_1.getJSONArray("data").getJSONObject(0);
            String url = data.getJSONObject("action").getString("url");
            jsonObject.put("url", "www.flipkart.com" + url);
            JSONObject value = data.getJSONObject("value");
            //imageUrl : "https://rukminim1.flixcart.com/image/{@width}/{@height}/mobile/2/j/5/apple-iphone-6-a1586-original-imaerm2zzfh6ahps.jpeg?q={@quality}"
            String imageUrl = value.getString("imageUrl");
            if (StringUtils.isNotEmpty(imageUrl)) {
                imageUrl = imageUrl.replace("{@width}", "240").replace("{@height}", "240").replace("{@quality}", "50");
                jsonObject.put("imageUrl", imageUrl);
            }
            float floatValue = value.getJSONObject("pricing").getJSONObject("finalPrice").getFloatValue("value");
            jsonObject.put("price", floatValue);
            String title = value.getString("title");
            jsonObject.put("title", title);
        }
        return jsonObject;
    }

    public static void main(String[] args) {
        getFlipKartSkuListByTitleSearch("iphone 6s (16GB)");
    }


}
