package hasoffer.core.app.website;

import hasoffer.core.utils.Httphelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.List;

/**
 * Created by hs on 2017年03月09日.
 * Time 16:37
 */
public class ApiSnapDealHelper {
    static Logger logger = LoggerFactory.getLogger(ApiSnapDealHelper.class);

    private static List getSnapDealSkuListByTitleSearch(String title) {
        String baseUrl = "https://www.snapdeal.com/search?keyword=iphone&santizedKeyword=&catId=&categoryId=0&suggested=false&vertical=&noOfResults=20&searchState=&clickSrc=go_header&lastKeyword=&prodCatId=&changeBackToAll=false&foundInAll=false&categoryIdSearched=&cityPageUrl=&categoryUrl=&url=&utmContent=&dealDetail=";
        Html html = null;
        if (StringUtils.isNotEmpty(title)) {
            try {
                String bodyString = Httphelper.doGetWithHeaer(baseUrl, null);
                if (bodyString != null) {
                    html = new Html(bodyString);
                }
            } catch (Exception e) {
                logger.error("urlEncode to utf8 exception  , ", e.getMessage());
            }
        }
        if (html == null) {
            //结束
            System.out.println("html null");
            return null;
        }

        List<Selectable> nodes = html.xpath("/html/body/div[@id='content_wrapper']/div[@class='col-xs-24 reset-padding marT22']/div[@class='col-xs-19 reset-padding ']/div[@class='comp comp-right-wrapper ref-freeze-reference-point clear']/div[@id='products']/").nodes();
        for (Selectable selectable : nodes) {
            System.out.println("1 " + selectable.xpath("//div[@class='product-tuple-image ']/a/@href"));
            System.out.println("2 " + selectable.xpath("//div[@class='product-tuple-image ']/a/picture/source/@srcset"));
            System.out.println("3 " + selectable.xpath("//div[@class='product-tuple-description ']/div[@class='product-desc-rating ']/a/@href"));
            System.out.println("4 " + selectable.xpath("//div[@class='product-tuple-description ']/div[@class='product-desc-rating ']/a/div/div/span[@class='lfloat product-price']/@display-price"));
        }


        return null;
    }

    public static void main(String[] args) {

        getSnapDealSkuListByTitleSearch("iPhone 6s (16GB)");
    }
}
