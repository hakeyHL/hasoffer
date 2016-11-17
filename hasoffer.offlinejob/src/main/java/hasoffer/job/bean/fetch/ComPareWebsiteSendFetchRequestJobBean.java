package hasoffer.job.bean.fetch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.enums.TaskLevel;
import hasoffer.base.exception.ContentParseException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.utils.Httphelper;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static hasoffer.base.utils.HtmlUtils.getSubNodesByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

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


        int num = 0;

        int totalPageSize = 0;
        int limitSize = 0;
        String jsonReqUrlList = "http://api.91mobiles.com:8080/nm-community/api/searchPage/web";

        List<String> apiJsonCates = new ArrayList<>();
        apiJsonCates.add("camera");
        apiJsonCates.add("tablet");
        apiJsonCates.add("tv");
        List<Integer> apiJsonCateCategorys = new ArrayList<>();
        apiJsonCateCategorys.add(3182);
        apiJsonCateCategorys.add(57);
        apiJsonCateCategorys.add(3100);


        List<String> htmlReqUrlList = new ArrayList<>();
        htmlReqUrlList.add("http://www.91mobiles.com/mobile-memory-card-finder.php");
        htmlReqUrlList.add("http://www.91mobiles.com/mobile-power-bank-finder.php");
        htmlReqUrlList.add("http://www.91mobiles.com/smartwatchfinder.php");
        List<Integer> htmlReqUrlCategoryList = new ArrayList<>();
        htmlReqUrlCategoryList.add(205);
        htmlReqUrlCategoryList.add(270);
        htmlReqUrlCategoryList.add(102909);

        for (int i = 0; i < apiJsonCates.size(); i++) {

            String cate = apiJsonCates.get(i);
            Integer categoryId = apiJsonCateCategorys.get(i);
//        }
//        for (String cate : apiJsonCates) {
            JSONObject jsonObject = new JSONObject();
            //t 当前时间戳
            jsonObject.put("t", new Date().getTime());
            //q:搜索关键字
            jsonObject.put("q", cate);
            //srtBy:score
            jsonObject.put("srtBy", "score");
            //srtType:desc
            jsonObject.put("srtType", "desc");
            //limit 20
            if (limitSize < 1) {
                limitSize = 20;
            }
            jsonObject.put("limit", limitSize);
            //startRow 0
            //get total page
            int filterCategoryId = 0;//表示类目id
            int filterCategoryCount = 0;//表示对应类目下商品商品数量

            try {
                jsonObject.put("startRow", 0);
                String postResultString = Httphelper.doPost(jsonReqUrlList, jsonObject.toJSONString());
                if (!StringUtils.isEmpty(postResultString)) {
                    JSONObject jsonResult = JSONObject.parseObject(postResultString);
                    Integer productCount = jsonResult.getInteger("productCount");

                    JSONArray categoryArray = jsonResult.getJSONArray("categoryFilter");
                    for (int k = 0; k < categoryArray.size(); k++) {

                        JSONObject jsonCategoryInfo = categoryArray.getJSONObject(k);

                        if (k == 0) {
                            Integer count = jsonCategoryInfo.getInteger("count");
                            Integer countCategoryId = jsonCategoryInfo.getInteger("categoryId");
                            filterCategoryCount = count;
                            filterCategoryId = countCategoryId;
                        }

                        if (k > 0) {
                            Integer count = jsonCategoryInfo.getInteger("count");
                            Integer countCategoryId = jsonCategoryInfo.getInteger("categoryId");
                            if (count > filterCategoryCount) {
                                filterCategoryId = countCategoryId;
                                filterCategoryCount = count;
                            }
                        }
                    }

                    if (productCount > 0) {
                        if (productCount % limitSize != 0) {
                            //+1
                            totalPageSize = (productCount / limitSize) + 1;
                        } else {
                            totalPageSize = productCount / limitSize;
                        }
                    }
                }
            } catch (Exception e) {
                logger.info(" calculate totalPage exception {}", e.getMessage());
                e.printStackTrace();
            }
            for (int j = 0; j < totalPageSize; j++) {
                jsonObject.put("startRow", j * jsonObject.getInteger("limit"));
                System.out.println(jsonReqUrlList + " _ " + cate + "  FETCH START");
                cate91Fetch(jsonReqUrlList, jsonObject, categoryId, filterCategoryId);
                System.out.println(jsonReqUrlList + " _ " + cate + " FETCH END");
                num++;
            }
            System.out.println("total num " + num);
        }

        totalPageSize = 14;
        for (int i = 0; i < htmlReqUrlList.size(); i++) {

            String htmlUrl = htmlReqUrlList.get(i);
            Integer categoryId = htmlReqUrlCategoryList.get(i);

            num = 0;
            htmlUrl = htmlUrl + "?page=";
            for (int j = 1; j < totalPageSize + 1; j++) {
                String tempUrl = "";
                tempUrl = htmlUrl + j;
                System.out.println(tempUrl + " html  FETCH START");
                cate91FetchHtml(tempUrl, categoryId);
                System.out.println(tempUrl + " html  FETCH END");
                num++;
            }
        }

    }

    private void cate91Fetch(String url, JSONObject jsonObject, long categoryId, long filterCategoryId) {
        String jsonString = null;
        try {
            jsonString = Httphelper.doPost(url, jsonObject.toJSONString());
        } catch (Exception e) {
            System.out.println("parse exception for " + url);
        }
        //get Products
        if (!StringUtils.isEmpty(jsonString)) {
            JSONObject object = JSONObject.parseObject(jsonString);
            if (object != null) {
                JSONArray products = object.getJSONArray("products");
                Iterator<Object> iterator = products.iterator();
                while (iterator.hasNext()) {
                    JSONObject product = (JSONObject) iterator.next();

                    int groupCategoryId = jsonObject.getIntValue("productGroupCategoryId");

                    if (groupCategoryId != filterCategoryId) {
                        continue;
                    }

                    String productUrl = product.getString("productUrl");
                    if (!StringUtils.isEmpty(productUrl)) {
                        productUrl = "http://www.91mobiles.com/" + productUrl;
                        System.out.println(productUrl);

                        fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_2, TimeUtils.MILLISECONDS_OF_1_HOUR * 10, categoryId);

                        requestSendNumber++;
                        if (requestSendNumber % 20 == 0) {
                            logger.info("hava send " + requestSendNumber + " request");
                        }

                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }
        } else {
            return;
        }

    }

    private void cate91FetchHtml(String url, long categoryId) {
        String html = null;
        List<TagNode> productListNode = null;
        try {
            html = HtmlUtils.getUrlHtml(url);

            TagNode root = new HtmlCleaner().clean(html);

            productListNode = getSubNodesByXPath(root, "//div[@class='filter filer_finder']");

        } catch (Exception e) {
            System.out.println("parse exception for " + url);
        }

        for (TagNode productNode : productListNode) {

            TagNode productUrlNode = null;
            try {
                productUrlNode = getSubNodeByXPath(productNode, "//a[@target='_blank']", null);
            } catch (ContentParseException e) {
                System.out.println("content parse exception");
                continue;
            }

            String productUrl = productUrlNode.getAttributeByName("href");
            if (productUrl != null) {
                productUrl = WEBSITE_91MOBILE_URL_PREFIEX + productUrl;
            }

            System.out.println(productUrl);
            fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_2, TimeUtils.MILLISECONDS_OF_1_HOUR * 10, categoryId);

            requestSendNumber++;
            if (requestSendNumber % 20 == 0) {
                logger.info("hava send " + requestSendNumber + " request");
            }

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {

            }
        }
    }
}
