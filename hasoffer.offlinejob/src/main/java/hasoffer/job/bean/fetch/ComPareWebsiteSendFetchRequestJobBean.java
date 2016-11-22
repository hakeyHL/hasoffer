package hasoffer.job.bean.fetch;

import com.alibaba.fastjson.JSONObject;
import hasoffer.base.enums.TaskLevel;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.util.HtmlHelper;
import org.apache.commons.lang3.math.NumberUtils;
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
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ComPareWebsiteSendFetchRequestJobBean.class);
    @Resource
    IFetchDubboService fetchDubboService;
    int requestSendNumber = 0;//用来记录请求发送的个数

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//          mobile
        try {
            method2();//抓取91mobile数据mobile
        } catch (HttpFetchException e) {
            logger.info("fetch mobile category for 91mobile fail");
        }

//          camera
        try {
            String tabletProductListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.8606721456464186&requestType=2&listType=list&listType_v1=&selMobSort=relevance&amount=1000%3B70000&sCatName=phone&price_range_apply=0&search=camera&hidFrmSubFlag=1&page=";
            String tabletProductListUrlSuffix = "&category=camera&unique_sort=&hdnCategory=camera&user_search=camera";
            method2(tabletProductListUrlPrefix, tabletProductListUrlSuffix, 3182);
        } catch (HttpFetchException e) {
            logger.info("fetch tablet category for 91mobile fail");
        }

//          tablet
        try {
            String tabletProductListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.5817923555480047&requestType=1&listType=list&listType_v1=&selMobSort=relevance&amount=1000%3B45000&sCatName=phone&price_range_apply=0&search=tablet&hidFrmSubFlag=1&page=";
            String tabletProductListUrlSuffix = "&category=tablet&unique_sort=&hdnCategory=tablet&user_search=tablet";
            method2(tabletProductListUrlPrefix, tabletProductListUrlSuffix, 57);
        } catch (HttpFetchException e) {
            logger.info("fetch tablet category for 91mobile fail");
        }

//          tv
        try {
            String tabletProductListUrlPrefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.31939880350088856&requestType=2&listType=list&listType_v1=&selMobSort=relevance&amount=1000%3B70000&sCatName=phone&price_range_apply=0&search=television&hidFrmSubFlag=1&page=";
            String tabletProductListUrlSuffix = "&category=television&unique_sort=&hdnCategory=television&user_search=television";
            method2(tabletProductListUrlPrefix, tabletProductListUrlSuffix, 3100);
        } catch (HttpFetchException e) {
            logger.info("fetch tablet category for 91mobile fail");
        }

//          memory card
        try {
            String memoryCardRequestRaw = "listType=list&selMobSort=views&amount=100%3B10000&sCatName=memorycard&market_status%5B%5D=ava_stores&hidFrmSubFlag=1&category=memorycard&hdnCategory=memorycard&hdnPageType=list&resFormat=list&device_category=m&page=1";
            method3(memoryCardRequestRaw, 205);
        } catch (Exception e) {
            logger.info("fetch post memory card category for 91mobile fail");
        }
//          power bank
        try {
            String powerBankCardRequestRaw = "listType=list&selMobSort=views&amount=200%3B10000&sCatName=powerbank&market_status%5B%5D=ava_stores&q=&hidFrmSubFlag=1&category=powerbank&unique_sort=&hdnCategory=powerbank&hdnPageType=list&resFormat=list&device_category=m&page=1";
            method3(powerBankCardRequestRaw, 270);
        } catch (Exception e) {
            logger.info("fetch post power bank category for 91mobile fail");
        }
//          smart watch
        try {
            String smartWatchRequestRaw = "listType=list&selMobSort=views&amount=1000%3B25000&sCatName=smartwatch&market_status%5B%5D=all&q=&hidFrmSubFlag=1&category=smartwatch&unique_sort=&hdnCategory=smartwatch&hdnPageType=list&resFormat=list&device_category=m&page=1";
            method3(smartWatchRequestRaw, 102909);
        } catch (Exception e) {
            logger.info("fetch post smart watch category for 91mobile fail");
        }
    }

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
                requestRaw += i;
                responseHtml = HtmlHelper.doPostJsonWithHeader(url, requestRaw + 1, headerMap);
            }

            String[] subStr2 = responseHtml.split("hover_blue_link name\" href=\"");
            for (int j = 1; j < subStr2.length; j++) {
                String productUrl = WEBSITE_91MOBILE_URL_PREFIEX + subStr2[i].substring(0, subStr2[i].indexOf('"'));
                fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_1, categoryId);
                requestSendNumber++;
                logger.info("hava send " + requestSendNumber + " request");
            }
        }
    }

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
                }

            } catch (HttpFetchException e) {
                logger.info("HttpFetchException for page " + i);
            }
        }
    }

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
                }

            } catch (HttpFetchException e) {
                logger.info("HttpFetchException for page " + i);
            }
        }
    }
}
