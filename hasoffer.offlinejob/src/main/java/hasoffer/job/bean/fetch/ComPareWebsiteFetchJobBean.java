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

import static hasoffer.base.utils.HtmlUtils.getSubNodesByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created on 2016/11/15.
 */
public class ComPareWebsiteFetchJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ComPareWebsiteFetchJobBean.class);
    public static final String WEBSITE_91MOBILE_URL_PREFIEX = "http://www.91mobiles.com";

    @Resource
    IFetchDubboService fetchDubboService;

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


        List<String> htmlReqUrlList = new ArrayList<>();
        htmlReqUrlList.add("http://www.91mobiles.com/mobile-memory-card-finder.php");
        htmlReqUrlList.add("http://www.91mobiles.com/mobile-power-bank-finder.php");
        htmlReqUrlList.add("http://www.91mobiles.com/smartwatchfinder.php");

        for (String cate : apiJsonCates) {
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
            try {
                jsonObject.put("startRow", 0);
                String postResultString = Httphelper.doPost(jsonReqUrlList, jsonObject.toJSONString());
                if (!StringUtils.isEmpty(postResultString)) {
                    JSONObject jsonResult = JSONObject.parseObject(postResultString);
                    Integer productCount = jsonResult.getInteger("productCount");
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
            }
            for (int i = 0; i < totalPageSize; i++) {
                jsonObject.put("startRow", i * jsonObject.getInteger("limit"));
                System.out.println(jsonReqUrlList + " _ " + cate + "  FETCH START");
                cate91Fetch(jsonReqUrlList, jsonObject);
                System.out.println(jsonReqUrlList + " _ " + cate + " FETCH END");
                num++;
            }
            System.out.println("total num " + num);
        }
        totalPageSize = 14;
        for (String htmlUrl : htmlReqUrlList) {
            num = 0;
            htmlUrl = htmlUrl + "?page=";
            for (int i = 1; i < totalPageSize + 1; i++) {
                String tempUrl = "";
                tempUrl = htmlUrl + i;
                System.out.println(tempUrl + " html  FETCH START");
                cate91FetchHtml(tempUrl);
                System.out.println(tempUrl + " html  FETCH END");
                num++;
            }
        }

    }

    private void cate91Fetch(String url, JSONObject jsonObject) {
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
                    String productUrl = product.getString("productUrl");
                    if (!StringUtils.isEmpty(productUrl)) {
                        productUrl = "http://www.91mobiles.com/" + productUrl;
                        System.out.println(productUrl);

                        fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_2, TimeUtils.MILLISECONDS_OF_1_HOUR * 10);
                    }
                }
            }
        } else {
            return;
        }

    }

    private void cate91FetchHtml(String url) {
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
            fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, productUrl, TaskLevel.LEVEL_2, TimeUtils.MILLISECONDS_OF_1_HOUR * 10);

        }
    }
}
