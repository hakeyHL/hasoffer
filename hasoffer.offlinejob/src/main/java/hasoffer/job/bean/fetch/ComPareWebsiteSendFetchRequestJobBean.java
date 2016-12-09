package hasoffer.job.bean.fetch;

import com.alibaba.fastjson.JSONObject;
import hasoffer.base.enums.TaskLevel;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.http.XPathUtils;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.util.HtmlHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/11/15.
 */
public class ComPareWebsiteSendFetchRequestJobBean extends QuartzJobBean {

    public static final String WEBSITE_91MOBILE_URL_PREFIEX = "http://www.91mobiles.com";
    public static final String WEBSITE_IDEALO_URL_PREFIEX = "http://www.91mobiles.com";
    public static final String WEBSITE_IDEALO_PAGE_URL_PREFIEX = "http://www.idealo.in/mvc/CategoryResultList/category/";
    public static final String WEBSITE_IDEALO_PAGE_URL_MIDDLE = "/filters/none/start/";
    public static final String WEBSITE_IDEALO_PAGE_URL_SUFFIX = "/sort/none";


    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ComPareWebsiteSendFetchRequestJobBean.class);
    @Resource
    IFetchDubboService fetchDubboService;
    int requestSendNumber = 0;//用来记录请求发送的个数

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {


/**------------------------------------91mobile-----start--------------------------------------------------------------------*/
//          mobile
//        try {
//            method2();//抓取91mobile数据mobile
//        } catch (HttpFetchException e) {
//            logger.info("fetch mobile category for 91mobile fail");
//        }

//          camera
//        try {
//            String tabletProductListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.8606721456464186&requestType=2&listType=list&listType_v1=&selMobSort=relevance&amount=1000%3B70000&sCatName=phone&price_range_apply=0&search=camera&hidFrmSubFlag=1&page=";
//            String tabletProductListUrlSuffix = "&category=camera&unique_sort=&hdnCategory=camera&user_search=camera";
//            method2(tabletProductListUrlPrefix, tabletProductListUrlSuffix, 3182);
//        } catch (HttpFetchException e) {
//            logger.info("fetch tablet category for 91mobile fail");
//        }

//          tablet
//        try {
//            String tabletProductListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.5817923555480047&requestType=1&listType=list&listType_v1=&selMobSort=relevance&amount=1000%3B45000&sCatName=phone&price_range_apply=0&search=tablet&hidFrmSubFlag=1&page=";
//            String tabletProductListUrlSuffix = "&category=tablet&unique_sort=&hdnCategory=tablet&user_search=tablet";
//            method2(tabletProductListUrlPrefix, tabletProductListUrlSuffix, 57);
//        } catch (HttpFetchException e) {
//            logger.info("fetch tablet category for 91mobile fail");
//        }

//          tv
//        try {
//            String tabletProductListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.31939880350088856&requestType=2&listType=list&listType_v1=&selMobSort=relevance&amount=1000%3B70000&sCatName=phone&price_range_apply=0&search=television&hidFrmSubFlag=1&page=";
//            String tabletProductListUrlSuffix = "&category=television&unique_sort=&hdnCategory=television&user_search=television";
//            method2(tabletProductListUrlPrefix, tabletProductListUrlSuffix, 3100);
//        } catch (HttpFetchException e) {
//            logger.info("fetch tablet category for 91mobile fail");
//        }

//          memory card
//        try {
//            String memoryCardRequestRaw = "listType=list&selMobSort=views&amount=100%3B10000&sCatName=memorycard&market_status%5B%5D=ava_stores&hidFrmSubFlag=1&category=memorycard&hdnCategory=memorycard&hdnPageType=list&resFormat=list&device_category=m&page=";
//            method3(memoryCardRequestRaw, 205);
//        } catch (Exception e) {
//            logger.info("fetch post memory card category for 91mobile fail");
//        }
//          power bank
//        try {
//            String powerBankCardRequestRaw = "listType=list&selMobSort=views&amount=200%3B10000&sCatName=powerbank&market_status%5B%5D=ava_stores&q=&hidFrmSubFlag=1&category=powerbank&unique_sort=&hdnCategory=powerbank&hdnPageType=list&resFormat=list&device_category=m&page=";
//            method3(powerBankCardRequestRaw, 270);
//        } catch (Exception e) {
//            logger.info("fetch post power bank category for 91mobile fail");
//        }
//          smart watch
//        try {
//            String smartWatchRequestRaw = "listType=list&selMobSort=views&amount=1000%3B25000&sCatName=smartwatch&market_status%5B%5D=all&q=&hidFrmSubFlag=1&category=smartwatch&unique_sort=&hdnCategory=smartwatch&hdnPageType=list&resFormat=list&device_category=m&page=";
//            method3(smartWatchRequestRaw, 102909);
//        } catch (Exception e) {
//            logger.info("fetch post smart watch category for 91mobile fail");
//        }
/**------------------------------------91mobile-----end--------------------------------------------------------------------*/


/**------------------------------------idealo-----start--------------------------------------------------------------------*/

        Map<Integer, Integer> categoryRelationShipMap = new HashMap<>();

//Men's Fashion
        //--Jeans for Men
        categoryRelationShipMap.put(26151, 0);
//Shoes
        //--Running Shoes
        categoryRelationShipMap.put(22875, 0);
        //--Football Shoes
        categoryRelationShipMap.put(21363, 0);
        //--Sneakers
        categoryRelationShipMap.put(18817, 0);
        //--Sports Shoes
        categoryRelationShipMap.put(18855, 0);
        //--Outdoor Shoes
        categoryRelationShipMap.put(18854, 0);
        //--Cycling Shoes
        categoryRelationShipMap.put(19160, 0);
        //--Ladies Shoes
        categoryRelationShipMap.put(11593, 0);
        //--Formal Shoes for Men
        categoryRelationShipMap.put(26147, 0);
        //--Men's Boots
        categoryRelationShipMap.put(26146, 0);
        //--Sandals for Men
        categoryRelationShipMap.put(26145, 0);
        //--Ladies Boots
        categoryRelationShipMap.put(22698, 0);
        //--Ankle Boots
        categoryRelationShipMap.put(22595, 0);
        //--Men's Shoes
        categoryRelationShipMap.put(11594, 0);
//Timepieces
        //--Watches
        categoryRelationShipMap.put(5292, 0);
        //--Ladies Watches
        categoryRelationShipMap.put(19040, 0);
        //--Sports Watches
        categoryRelationShipMap.put(12272, 0);
        //--Men's Watches
        categoryRelationShipMap.put(19041, 0);
        //--Chronographs
        categoryRelationShipMap.put(19043, 0);
//Kids & Baby
        //--Lego
        categoryRelationShipMap.put(9552, 0);
        //--Toy & Action Figures
        categoryRelationShipMap.put(28012, 0);
        //--Toy Vehicles
        categoryRelationShipMap.put(11473, 0);
        //--Children's Fancy Dress
        categoryRelationShipMap.put(18366, 0);
        //--Games
        categoryRelationShipMap.put(7174, 0);
        //--Barbies
        categoryRelationShipMap.put(18415, 0);
        //--Baby Toys
        categoryRelationShipMap.put(11492, 0);
        //--Dolls
        categoryRelationShipMap.put(7193, 0);
        //--Ride Ons
        categoryRelationShipMap.put(5932, 0);
        //--Electronic Toys
        categoryRelationShipMap.put(7194, 0);
        //--Children's Musical Instruments
        categoryRelationShipMap.put(18423, 0);
        //--Educational Computers
        categoryRelationShipMap.put(15065, 0);
        //--Playmobil
        categoryRelationShipMap.put(9553, 0);
        //--Puzzles
        categoryRelationShipMap.put(10032, 0);
        //--Science Kits
        categoryRelationShipMap.put(25962, 0);
        //--Scooters
        categoryRelationShipMap.put(18315, 0);
        //--Soft Toys
        categoryRelationShipMap.put(17135, 0);
//Leisure & Books
        //--Cricket
        categoryRelationShipMap.put(27041, 0);
        //--Tennis Rackets
        categoryRelationShipMap.put(6754, 0);
        //--Squash
        categoryRelationShipMap.put(13293, 0);
        //--Badminton Rackets
        categoryRelationShipMap.put(13292, 0);
        //--Table Tennis Bats
        categoryRelationShipMap.put(19017, 0);
        //--Yoga & Pilates
        categoryRelationShipMap.put(14995, 0);
        //--Swimming
        categoryRelationShipMap.put(21115, 0);
        //--Gymnastics & Aerobics
        categoryRelationShipMap.put(16895, 0);
        //--Footballs
        categoryRelationShipMap.put(8832, 0);
        //--Climbing Gear
        categoryRelationShipMap.put(23402, 0);
        categoryRelationShipMap.put(23401, 0);
        categoryRelationShipMap.put(23398, 0);
        categoryRelationShipMap.put(23397, 0);
        //--Backpacks
        categoryRelationShipMap.put(9153, 0);
        //--Camera Bags
        categoryRelationShipMap.put(4972, 0);
        //--Laptop Bags
        categoryRelationShipMap.put(6193, 0);
        //--Suitcases & Bags
        categoryRelationShipMap.put(9113, 0);
        //--Wallets & Purses
        categoryRelationShipMap.put(15673, 0);
        //--Tablet Cases
        categoryRelationShipMap.put(22506, 0);
        //--School Bags & Satchels
        categoryRelationShipMap.put(9232, 0);
        //--Handbags
        categoryRelationShipMap.put(11512, 0);
//Beauty & Grooming
        //--Trimmers
        categoryRelationShipMap.put(3232, 0);
        //--Perfumes for Women
        categoryRelationShipMap.put(3972, 0);
        //--Perfumes for Men
        categoryRelationShipMap.put(8694, 0);
        //--Perfume & Beauty Gift Sets
        categoryRelationShipMap.put(14673, 0);
        //--Hair
        categoryRelationShipMap.put(4413, 0);
        categoryRelationShipMap.put(3234, 0);
        categoryRelationShipMap.put(10872, 0);
        categoryRelationShipMap.put(21096, 0);
        categoryRelationShipMap.put(27342, 0);
        //--Cosmetics
        categoryRelationShipMap.put(10812, 0);
        categoryRelationShipMap.put(10832, 0);
        categoryRelationShipMap.put(10852, 0);
        categoryRelationShipMap.put(18481, 0);
        categoryRelationShipMap.put(18454, 0);
        categoryRelationShipMap.put(11613, 0);
        //--Valentine's Day Gifts
        categoryRelationShipMap.put(28481, 0);
        //--Electric Shavers
        categoryRelationShipMap.put(3236, 0);
        //--Contact Lenses
        categoryRelationShipMap.put(5472, 0);
        //--Personal Weighing Scales
        categoryRelationShipMap.put(3933, 0);
        //--Epilators & Lady Shavers
        categoryRelationShipMap.put(3566, 0);
//Small Appliances
        //--Vacuum Cleaners
        categoryRelationShipMap.put(2925, 0);
        //--Mixers & Blenders
        categoryRelationShipMap.put(3515, 0);
        //--Food Processors
        categoryRelationShipMap.put(3512, 0);
        //--Microwaves
        categoryRelationShipMap.put(3508, 0);
        //--Rice Cookers
        categoryRelationShipMap.put(10634, 0);
        //--Juicers
        categoryRelationShipMap.put(4512, 0);
        //--Roti Makers
        categoryRelationShipMap.put(28281, 0);
        //--Deep Fryers
        categoryRelationShipMap.put(3513, 0);
        //--Waffle Makers & Sandwich Toasters
        categoryRelationShipMap.put(12132, 0);
        //--Toasters
        categoryRelationShipMap.put(3507, 0);
        //--Kitchen Scales
        categoryRelationShipMap.put(3511, 0);
        //--Air Conditioners
        categoryRelationShipMap.put(2860, 0);
        //--Irons
        categoryRelationShipMap.put(2926, 0);
        //--Fans
        categoryRelationShipMap.put(5673, 0);
        //--Stoves
        categoryRelationShipMap.put(3446, 0);
        //--Refrigerators
        categoryRelationShipMap.put(2800, 0);
        //--Washing Machines
        categoryRelationShipMap.put(1941, 0);
        //--Water Purification
        categoryRelationShipMap.put(15197, 0);
//Home Furnishings
        //--Living Room Furniture
        categoryRelationShipMap.put(22156, 0);
        //--Exhaust Fans
        categoryRelationShipMap.put(28660, 0);
        //--Wall Clocks
        categoryRelationShipMap.put(8653, 0);
        //--Kettles
        categoryRelationShipMap.put(3509, 0);
        //--Kitchen Accessories
        categoryRelationShipMap.put(5677, 0);
        //--Baking Accessories
        categoryRelationShipMap.put(12393, 0);
        //--Bins
        categoryRelationShipMap.put(18306, 0);
        //--Kitchen Knives
        categoryRelationShipMap.put(8972, 0);
        //--Saucepans & Casserole Dishes
        categoryRelationShipMap.put(4272, 0);
//Smartwatches
        //--Smartwatches
        categoryRelationShipMap.put(25496, 0);
        //--Headphones
        categoryRelationShipMap.put(2520, 0);
        //--Memory Cards
        categoryRelationShipMap.put(4734, 0);
        //--Pen Drives
        categoryRelationShipMap.put(4312, 0);
        //--External Hard Drives
        categoryRelationShipMap.put(7712, 0);
        //--Laptops
        categoryRelationShipMap.put(3751, 0);

        for (Map.Entry<Integer, Integer> entry : categoryRelationShipMap.entrySet()) {

            Integer sourceCategoryId = entry.getKey();
            Integer hasofferCategoryId = entry.getValue();

            System.out.println("start fetch " + sourceCategoryId);

            a:
            for (int i = 0; ; i++) {

                try {

                    TagNode root = HtmlUtils.getUrlRootTagNode(WEBSITE_IDEALO_PAGE_URL_PREFIEX + sourceCategoryId + WEBSITE_IDEALO_PAGE_URL_MIDDLE + i + WEBSITE_IDEALO_PAGE_URL_SUFFIX);
                    List<TagNode> productUrlListNode = XPathUtils.getSubNodesByXPath(root, "//div[@data-offer='false']/a", null);

                    if (productUrlListNode == null && productUrlListNode.size() == 0) {
                        System.out.println("productUrlListNode is null or size = 0 current page is " + i + " break");
                        break a;
                    }

                    for (TagNode node : productUrlListNode) {

                        String productUrl = node.getAttributeByName("href");
                        productUrl = WEBSITE_IDEALO_URL_PREFIEX + productUrl;

                        fetchDubboService.sendCompareWebsiteFetchTask(Website.IDEALO, productUrl, TaskLevel.LEVEL_1, hasofferCategoryId);
                        System.out.println("send url request success _" + productUrl);

                    }
                } catch (Exception e) {
                    System.out.println("fetch error for " + sourceCategoryId + " at page " + i);
                }
            }

            System.out.println("start finish " + sourceCategoryId);
        }


/**------------------------------------idealo-----end----------------------------------------------------------------------*/


    }

    /**
     * for mobile91 fetch: analysis list page get info page url and then send info page url request
     *
     * @param requestRaw
     * @param categoryId
     * @throws Exception
     */
    private void method3(String requestRaw, long categoryId) throws Exception {

        String url = "http://www.91mobiles.com/template/finder_new/finder_ajax.php?search=&listview=list&fieldname=undefined";

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        String responseHtml = HtmlHelper.doPostJsonWithHeader(url, requestRaw + 1, headerMap);

        String[] subStr1 = responseHtml.split("return submitPage\\('last', '");

        int totalPage = 1;
        String totalPageString = subStr1[1].substring(0, subStr1[1].indexOf("'"));
        if (NumberUtils.isNumber(totalPageString)) {
            totalPage = Integer.parseInt(totalPageString);
        }

        for (int i = 1; i <= totalPage; i++) {

            if (i > 1) {
                responseHtml = HtmlHelper.doPostJsonWithHeader(url, requestRaw + i, headerMap);
            }

            String[] subStr2 = responseHtml.split("hover_blue_link name\" href=\"");
            for (int j = 1; j < subStr2.length; j++) {
                String productUrl = WEBSITE_91MOBILE_URL_PREFIEX + subStr2[j].substring(0, subStr2[j].indexOf('"'));
                fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_1, categoryId);
                requestSendNumber++;
                logger.info("hava send " + requestSendNumber + " request ");
                logger.info("current url _" + productUrl);
            }
        }
    }

    /**
     * for mobile91 fetch: analysis list page get info page url and then send info page url request
     *
     * @param productListUrlPrefix
     * @param productListUrlSuffix
     * @param cateogyid
     * @throws Exception
     */
    private void method2(String productListUrlPrefix, String productListUrlSuffix, long cateogyid) throws HttpFetchException {

        String html = HtmlUtils.getUrlHtml(productListUrlPrefix + 1 + productListUrlSuffix);

        JSONObject rootJsonObject = JSONObject.parseObject(html);
        String response = rootJsonObject.getString("response");
        int totalPages = rootJsonObject.getIntValue("totalPages");

        logger.info("91mobile mobile category fetch totalPage is " + totalPages);

        for (int i = 1; i <= totalPages; i++) {

            try {

                if (i > 1) {
                    html = HtmlUtils.getUrlHtml(productListUrlPrefix + i + productListUrlSuffix);
                    rootJsonObject = JSONObject.parseObject(html);
                    response = rootJsonObject.getString("response");
                }

                String[] subStr = response.split("hover_blue_link name gaclick\\\" data-type='name' href=\\\"");
                List<String> productUrlList = new ArrayList<>();

                for (int j = 1; j < subStr.length; j++) {
                    String productUrlSuffix = subStr[j].substring(0, subStr[j].indexOf('\"'));
                    productUrlList.add(WEBSITE_91MOBILE_URL_PREFIEX + productUrlSuffix);
                }

                logger.info("query page " + i + " get " + productUrlList.size() + " productUrl");
                for (String productUrl : productUrlList) {
                    fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_1, cateogyid);
                    requestSendNumber++;
                    logger.info("hava send " + requestSendNumber + " request");
                    logger.info("current url _" + productUrl);
                }

            } catch (HttpFetchException e) {
                logger.info("HttpFetchException for page " + i);
            }
        }
    }

    /**
     * for mobile91 fetch: analysis list page get info page url and then send info page url request
     *
     * @throws Exception
     */
    private void method2() throws HttpFetchException {

        String productListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?show_next=1&ord=0.2768255707779246&requestType=2&listType=list&listType_v1=list&selMobSort=relevance&amount=1000%3B45000&sCatName=phone&price_range_apply=0&search=mobiles&hidFrmSubFlag=1&page=";
        String productListUrlSuffix = "&category=mobile&unique_sort=&hdnCategory=mobile&user_search=mobiles&url_feat_rule=&hdndprice=10001-15000%2C15001-20000&hdndprice1=5001-15000";

        String html = HtmlUtils.getUrlHtml(productListUrlPrefix + 1 + productListUrlSuffix);

        JSONObject rootJsonObject = JSONObject.parseObject(html);
        String response = rootJsonObject.getString("response");
        int totalPages = rootJsonObject.getIntValue("totalPages");

        logger.info("91mobile mobile category fetch totalPage is " + totalPages);

        for (int i = 1; i <= totalPages; i++) {

            try {

                if (i > 1) {
                    html = HtmlUtils.getUrlHtml(productListUrlPrefix + i + productListUrlSuffix);
                    rootJsonObject = JSONObject.parseObject(html);
                    response = rootJsonObject.getString("response");
                }

                String[] subStr = response.split("hover_blue_link name gaclick\\\" data-type='name' href=\\\"");
                List<String> productUrlList = new ArrayList<>();

                for (int j = 1; j < subStr.length; j++) {
                    String productUrlSuffix = subStr[j].substring(0, subStr[j].indexOf('\"'));
                    productUrlList.add(WEBSITE_91MOBILE_URL_PREFIEX + productUrlSuffix);
                }

                logger.info("query page " + i + " get " + productUrlList.size() + " productUrl");
                for (String productUrl : productUrlList) {
                    fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_1, 5);
                    requestSendNumber++;
                    logger.info("hava send " + requestSendNumber + " request");
                    logger.info("current url _" + productUrl);
                }

            } catch (HttpFetchException e) {
                logger.info("HttpFetchException for page " + i);
            }
        }
    }
}
