package hasoffer.spider.task.service;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.dubbo.spider.task.api.ISkuTaskDubboService;
import hasoffer.spider.model.SpiderSkuTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpiderSkuTaskService {

    private static Logger logger = LoggerFactory.getLogger(SpiderSkuTaskService.class);

    //private final String Q_PTMCMPSKU_BYPRODUCTID = "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ORDER BY t.id ASC";


    @Resource
    private IDataBaseManager dbm;

    @Resource
    private ISkuTaskDubboService skuTaskDubboService;

    public void initFlipkartTask(String dataStr, String num) {
        initTask(dataStr, num, Website.FLIPKART);
    }

    public void initAmazonTask(String dataStr, String num) {
        initTask(dataStr, num, Website.AMAZON);
    }

    private void initTask(String dateStr, String num, Website webSite) {
        String countSql = "SELECT count(*) as countNum FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = :ymd AND psc.count > :sum AND sku.website = :website AND sku.`status` <> 'OFFSALE'";
        String pageSql = "SELECT sku.id,sku.url,sku.website,sku.productId as countNum FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = :ymd AND psc.count > :sum AND sku.website = :website AND sku.`status` <> 'OFFSALE' limit :begin,:end";
        int pageSize = 1000;
        int count = 0;

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("ymd", dateStr);
        paramsMap.put("sum", num);
        paramsMap.put("website", webSite.name());
        List list = dbm.queryBySql(countSql, paramsMap);
        logger.info("Query Sql:SELECT count(*) as countNum FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = {} AND psc.count > {} AND sku.website = '{}' AND sku.`status` <> 'OFFSALE'", dateStr, num, webSite.name());
        for (Object obj : list) {
            if (obj != null) {
                Map<String, Object> temp = (Map<String, Object>) obj;
                count = (int) temp.get("countNum");
            }
        }
        logger.info("Count Amazon:page count:{} and pageSize:1000", count);

        int countPage = count / pageSize;
        if (count % pageSize > 0) {
            countPage++;
        }
        for (int i = 0; i < countPage; i++) {
            int begin = i * pageSize;
            int end = pageSize + i * pageSize;
            paramsMap = new HashMap<>();
            paramsMap.put("ymd", dateStr);
            paramsMap.put("sum", num);
            paramsMap.put("website", webSite.toString());
            paramsMap.put("begin", begin);
            paramsMap.put("end", end);
            List detailList = dbm.queryBySql(pageSql, paramsMap);
            for (Object obj : detailList) {
                if (obj != null) {
                    Map<String, Object> temp = (Map<String, Object>) obj;
                    Object skuIdTmp = temp.get("id");
                    //Object productIdTmp = temp.get("productId");
                    Object urlTmp = temp.get("url");
                    if (skuIdTmp == null) {
                        continue;
                    }
                    if (urlTmp == null) {
                        continue;
                    }
                    String url = urlTmp.toString();
                    if (Website.SNAPDEAL.equals(webSite)) {
                        url = StringUtils.filterAndTrim(url, Collections.singletonList("/viewAllSellers"));
                    } else if (Website.AMAZON.equals(webSite)) {
                        url = urlTmp.toString().replace("gp/offer-listing", "dp");
                    }
                    try {
                        skuTaskDubboService.sendTask(new SpiderSkuTask(Long.valueOf(skuIdTmp.toString()), url, webSite, TaskLevel.LEVEL_3));
                        logger.info("Send Task success:{}", url);
                    } catch (Exception e) {
                        logger.error("Send Task error.", e);
                    }
                }
            }

        }

    }


    //public void initTask() {
    //    int page = 1;
    //    int pageSize = 1000;
    //
    //    String startDateString = TimeUtils.parse(TimeUtils.today() - TimeUtils.MILLISECONDS_OF_1_DAY * 1, "yyyyMMdd");
    //
    //    String Q_LOG_BYUPDATETIME = "SELECT t FROM SrmProductSearchCount t WHERE t.ymd = ?0 AND t.count > 5 ORDER BY t.count desc";
    //
    //    PageableResult<SrmProductSearchCount> pageableResult = dbm.queryPage(Q_LOG_BYUPDATETIME, page, pageSize, Arrays.asList(startDateString));
    //
    //    long totalPage = pageableResult.getTotalPage();
    //
    //    logger.info("totalPage :" + totalPage);
    //
    //    while (page <= totalPage) {
    //
    //        logger.info("current page:" + page);
    //
    //        if (page > 1) {
    //            pageableResult = dbm.queryPage(Q_LOG_BYUPDATETIME, page, pageSize, Collections.singletonList(startDateString));
    //        }
    //
    //        List<SrmProductSearchCount> dataList = pageableResult.getData();
    //
    //        if (ArrayUtils.hasObjs(dataList)) {
    //
    //            //暂时先拼凑一个srmsearchlog用于适配更新的接口
    //            for (SrmProductSearchCount log : dataList) {
    //
    //                long productId = log.getProductId();
    //
    //                List<PtmCmpSku> skuList = dbm.query(Q_PTMCMPSKU_BYPRODUCTID, Collections.singletonList(productId));
    //
    //                for (PtmCmpSku sku : skuList) {
    //                    //判断，如果该sku 当天更新过价格, 直接跳过
    //                    Date updateTime = sku.getUpdateTime();
    //                    if (updateTime != null) {
    //                        if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
    //                            continue;
    //                        }
    //                    }
    //
    //                    //offsale的不再更新
    //                    if (SkuStatus.OFFSALE.equals(sku.getStatus())) {
    //                        continue;
    //                    }
    //
    //                    Website website = sku.getWebsite();
    //
    //                    //暂时过滤掉myntra
    //                    if (Website.MYNTRA.equals(website)) {
    //                        continue;
    //                    }
    //
    //                    //高优先级的网站
    //                    if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {
    //
    //                        //过滤掉snapdeal中viewAllSeller的情况
    //                        if (Website.SNAPDEAL.equals(website)) {
    //                            String url = sku.getUrl();
    //                            url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
    //                            sku.setUrl(url);
    //                        }
    //                        //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
    //                        if (Website.AMAZON.equals(website)) {
    //                            String url = sku.getUrl();
    //                            url = url.replace("gp/offer-listing", "dp");
    //                            sku.setUrl(url);
    //                        }
    //
    //                    }
    //                    try {
    //                        skuTaskDubboService.sendTask(new SpiderSkuTask(sku.getId(), sku.getUrl(), sku.getWebsite(), TaskLevel.LEVEL_3));
    //                    } catch (Exception e) {
    //                        e.printStackTrace();
    //                    }
    //
    //                    logger.info("send url request succes for " + sku.getWebsite() + " sku id is [" + sku.getId() + "]");
    //                }
    //            }
    //        }
    //
    //        page++;
    //    }
    //
    //    logger.info("send url finish");
    //}


}
