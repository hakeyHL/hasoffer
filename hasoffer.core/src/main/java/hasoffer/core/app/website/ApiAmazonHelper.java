package hasoffer.core.app.website;

import hasoffer.core.utils.Httphelper;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

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
                    html = new Html(responseString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (html != null) {
                List<Selectable> nodes = html.xpath("/html[@class='a-ws a-js a-audio a-video a-canvas a-svg a-drag-drop a-geolocation a-history a-webworker a-autofocus a-input-placeholder a-textarea-placeholder a-local-storage a-gradients a-transform3d a-touch-scrolling a-text-shadow a-text-stroke a-box-shadow a-border-radius a-border-image a-opacity a-transform a-transition']/body[@class='a-auix_ember_92249-c a-auix_ember_92250-c a-auix_ember_92251-c a-auix_ember_92302-c a-aui_51279-c a-aui_51744-c a-aui_57326-c a-aui_58736-t1 a-aui_72554-c a-aui_83815-c a-aui_86171-t1 a-aui_accessibility_49860-c a-aui_attr_validations_1_51371-c a-aui_bolt_62845-c a-aui_noopener_84118-t2 a-aui_ux_59374-c a-aui_ux_60000-c a-meter-animate']/div[@id='a-page']/div[@id='search-main-wrapper']/div[@id='main']/div[@id='searchTemplate']/div[@id='rightContainerATF']/div[@id='rightResultsATF']/div[@id='resultsCol']/div[@id='centerMinus']/div[@id='atfResults']/ul[@id='s-results-list-atf']/li[@id='result_1']/div[@class='s-item-container']/div[@class='a-row a-spacing-mini']").nodes();
                System.out.println(nodes.size());
            }

        }
        return skuList;
    }

    public static void main(String[] args) {
        getFlipKartSkuListByTitleSearch("iphone");
    }
}
