package hasoffer.core.app.website;

import com.alibaba.fastjson.JSONObject;
import hasoffer.core.utils.Httphelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hs on 2017年03月09日.
 * Time 16:37
 */
public class ApiShopcluesHelper {
    //http://www.shopclues.com/search?q=iphone&z=1&sort_by=score&sort_order=desc
    private static final String baseUrl = "http://www.shopclues.com/search?q=iphone&z=1&sort_by=score&sort_order=desc";
    static Logger logger = LoggerFactory.getLogger(ApiShopcluesHelper.class);

    public static List getShopCluesSkuListByTitleSearch(String title) {
        List<JSONObject> skuList = new LinkedList();
        Html html = null;
        if (StringUtils.isNotEmpty(title)) {
            try {
                String bodyString = Httphelper.doGetWithHeaer(baseUrl, null);
                if (bodyString != null) {
                    html = new Html(bodyString);
                }
            } catch (Exception e) {
                logger.error("urlEncode to utf8 exception , ", e.getMessage());
            }
        }
        if (html == null) {
            //结束
            System.out.println("html null");
            return null;
        }
        List<Selectable> nodes = html.xpath("/html/body/div[@class='container']/div[@class='wrapper']/div[@class='cat_listing']/div[@class='prd_grd_pnl list column_layout']/div[@id='product_list']/div[@class='row']/div[@class='column col3']").nodes();
        JSONObject shopCluesJsonObj;
        for (Selectable selectable : nodes) {
            shopCluesJsonObj = new JSONObject();
            shopCluesJsonObj.put("title", selectable.xpath("//a/div[@class='img_section']/img/@title").get());
            shopCluesJsonObj.put("imgUrl", selectable.xpath("//a/div[@class='img_section']/img/@src").get());
            shopCluesJsonObj.put("deepLink", selectable.xpath("//a/@href").get());
            shopCluesJsonObj.put("price", selectable.xpath("//a/div[@class='prd_p_section']/div/span[@class='p_price']/text()").get());
            skuList.add(shopCluesJsonObj);
        }
        return skuList;
    }

    public static void main(String[] args) {
        getShopCluesSkuListByTitleSearch("iPhone 6s (16GB)");
    }
}
