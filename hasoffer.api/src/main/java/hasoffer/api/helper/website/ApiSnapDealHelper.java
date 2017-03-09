package hasoffer.api.helper.website;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.HtmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by hs on 2017年03月09日.
 * Time 16:37
 */
public class ApiSnapDealHelper {
    static Logger logger = LoggerFactory.getLogger(ApiSnapDealHelper.class);

    private static List getFlipKartSkuListByTitleSearch(String title) {
        String baseUrl = "https://www.snapdeal.com/search?keyword=%s&sort=rlvncy";
        Html html = null;
        if (StringUtils.isNotEmpty(title)) {
            try {
                title = URLEncoder.encode(title, "utf-8");
                HttpResponseModel response = HtmlUtils.getResponse(String.format(baseUrl, title), 2);
                if (response != null && StringUtils.isNotEmpty(response.getBodyString())) {
                    System.out.println(response.getBodyString());
                    html = new Html(response.getBodyString());
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
        Selectable xpath = html.xpath("/html/body/div[@id='content_wrapper']/div[@class='col-xs-24 reset-padding marT22']/div[@class='col-xs-19 reset-padding ']/div[@class='comp comp-right-wrapper ref-freeze-reference-point clear']/div[@id='products']/section[@class='js-section clearfix dp-widget dp-fired'][1]/div[@id='663413326062']/div[@class='product-tuple-image ']/a[@class='dp-widget-link']/@href");
        System.out.println(xpath);
        return null;
    }

    public static void main(String[] args) {

        getFlipKartSkuListByTitleSearch("iPhone 6s (16GB)");
    }
}
