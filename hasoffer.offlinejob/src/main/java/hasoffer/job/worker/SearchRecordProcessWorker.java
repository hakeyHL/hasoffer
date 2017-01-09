package hasoffer.job.worker;

import hasoffer.base.config.AppConfig;
import hasoffer.base.enums.HasofferRegion;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.search.SearchProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.model.WebFetchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function :
 */
public class SearchRecordProcessWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SearchRecordProcessWorker.class);

    private LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue;
    //private SearchProductService searchProductService;
    private IFetchDubboService fetchService;
    //private ISearchService searchService;

    //private TaskScheduleReqClient scheduleReqClient;

    public SearchRecordProcessWorker(SearchProductService searchProductService, IFetchDubboService flipkartFetchService, LinkedBlockingQueue<SrmAutoSearchResult> searchLogQueue) {
        //this.searchProductService = searchProductService;
        this.searchLogQueue = searchLogQueue;
        this.fetchService = flipkartFetchService;
        //this.searchService = SpringContextHolder.getBean(SearchServiceImpl.class);
    }

    @Override
    public void run() {

        while (true) {
            try {
                SrmAutoSearchResult autoSearchResult = searchLogQueue.poll();
                if (autoSearchResult == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("SearchRecordProcessWorker. search-log-queue is null. go to sleep!");
                    }
                    TimeUnit.SECONDS.sleep(5);
                    continue;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("SearchRecordProcessWorker. search keyword {}. begin", autoSearchResult);
                }

                // 获取mongo 中存储的数据并转换成java对象。
                String serRegion = AppConfig.get(AppConfig.SER_REGION);
                if (HasofferRegion.INDIA.toString().equals(serRegion)) {
                    fetchForIndia(autoSearchResult);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 1.判断是否需要更新该网站（website）的该商品（keyword）<br>
     * 2.需要的话，则加入更新队列。并返回一个实体。如果不需要，则返回空。
     *
     *
     * @param id
     * @param website
     * @param keyword
     * @param sitePros
     * @return
     */
    private void sendTask(String id, Website website, String keyword, Map<Website, WebFetchResult> sitePros) {
        WebFetchResult fetchResult = sitePros.get(website);
        long updateCycle = TimeUtils.MILLISECONDS_OF_1_HOUR * 12;
        //判断是否需要更新该网站（website）的该商品（keyword）
        boolean isFetch = fetchResult == null || System.currentTimeMillis() - fetchResult.getlUpdateDate() > updateCycle;
        // 需要的话，则加入更新队列。并返回一个实体。如果不需要，这返回空
        if (isFetch) {
            fetchService.sendKeyWordTask(id, website, keyword);
        }
        //return null;
    }

    private void fetchForIndia(SrmAutoSearchResult autoSearchResult) {
        String keyword = StringUtils.getCleanWordString(autoSearchResult.getTitle());
        Map<Website, WebFetchResult> sitePros = autoSearchResult.getSitePros();
        sendTask(autoSearchResult.getId(), Website.FLIPKART, keyword, sitePros);
        sendTask(autoSearchResult.getId(), Website.AMAZON, keyword, sitePros);
        sendTask(autoSearchResult.getId(), Website.SNAPDEAL, keyword, sitePros);
        sendTask(autoSearchResult.getId(), Website.SHOPCLUES, keyword, sitePros);
    }

}
