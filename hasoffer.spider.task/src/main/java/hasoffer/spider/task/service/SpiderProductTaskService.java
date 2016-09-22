package hasoffer.spider.task.service;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.spider.task.api.IProductTaskDubboService;
import hasoffer.fetch.model.WebFetchResult;
import hasoffer.site.helper.IndiaWebsiteHelper;
import hasoffer.spider.model.SpiderProductTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Transactional
public class SpiderProductTaskService {

    private final Logger logger = LoggerFactory.getLogger(SpiderProductTaskService.class);

    @Resource
    private SearchProductService searchProductService;

    @Resource
    private IDataBaseManager dbm;

    @Resource
    private IProductTaskDubboService productTaskDubboService;


    public void initTask() {
        String SQL_SEARCHLOG = "select t from SrmSearchLog t where t.updateTime >?0 order by t.updateTime ASC ";
        try {
            Date searchTime = new Date(TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_HOUR);
            PageableResult<SrmSearchLog> pagedSearchLog = dbm.queryPage(SQL_SEARCHLOG, 1, 1000, Collections.singletonList(searchTime));
            List<SrmSearchLog> searchLogs = pagedSearchLog.getData();
            if (ArrayUtils.hasObjs(searchLogs)) {
                for (SrmSearchLog searchLog : searchLogs) {
                    if (searchLog.getPrecise() == SearchPrecise.MANUALSET) {
                        continue;
                    }

                    if (searchLog.getPtmProductId() > 15 * 10000 && searchLog.getPtmProductId() <= 80 * 10000) {
                        searchLog.setPtmProductId(0);
                    }

                    SrmAutoSearchResult historyData = findSearchResult(searchLog);
                    sendProductTask(Website.AMAZON, historyData);
                    sendProductTask(Website.FLIPKART, historyData);
                    sendProductTask(Website.SNAPDEAL, historyData);
                    sendProductTask(Website.SHOPCLUES, historyData);
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private boolean checkWebsite(Website website, SrmAutoSearchResult historyData) {
        Map<Website, WebFetchResult> sitePros = historyData.getSitePros();
        Set<Website> websites = sitePros.keySet();
        if (!websites.contains(website)) {
            return true;
        }
        WebFetchResult webFetchResult = sitePros.get(website);
        // 判断最近更新是否超过24小时。
        if (new Date().getTime() - webFetchResult.getUpdateDate().getTime() > TimeUtils.MILLISECONDS_OF_1_DAY || ArrayUtils.isNullOrEmpty(webFetchResult.getProductList())) {
            return true;
        } else {
            return false;
        }
    }

    private void sendProductTask(Website website, SrmAutoSearchResult lastResult) {
        // 检查是否需要抓取该网站数据
        if (!checkWebsite(website, lastResult)) {
            return;
        }

        SpiderProductTask spiderProductTask = createProductTask(website, lastResult.getId(), lastResult.getTitle());
        if (spiderProductTask == null) {
            return;
        }
        logger.info("SpiderProductTaskService.sendProductTask(website,lastResult): website={}, url={}", website, spiderProductTask.getUrl());
        // 发送该任务到抓取服务器，分析抓取该对象；
        productTaskDubboService.sendTask(spiderProductTask);

    }

    private SpiderProductTask createProductTask(Website website, String proId, String title) {
        if (website == null || StringUtils.isEmpty(proId) || StringUtils.isEmpty(title)) {
            return null;
        }

        String url = IndiaWebsiteHelper.getSearchUrl(website, title);
        return new SpiderProductTask(proId, url, website);
    }

    private SrmAutoSearchResult findSearchResult(SrmSearchLog searchLog) {
        SrmAutoSearchResult autoSearchResult = searchProductService.getSearchResult(searchLog);
        // 去mongodb中查询是否已有该商品，如果有，则直接返回该主商品，如果没有，则创建并保存到mongodb中。后续抓取成功后，直接做更新操作。
        if (autoSearchResult == null) {
            autoSearchResult = new SrmAutoSearchResult(searchLog);
            searchProductService.saveSearchProducts(autoSearchResult);
        }
        return autoSearchResult;
    }

}
