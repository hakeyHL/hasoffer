package hasoffer.task.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.DaemonThreadFactory;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.analysis.ProductAnalysisService;
import hasoffer.core.bo.product.ProductBo;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.task.worker.UnmatchedSearchRecordListWorker;
import hasoffer.task.worker.UnmatchedSearchRecordListWorker2;
import hasoffer.task.worker.UnmatchedSearchRecordProcessWorker;
import hasoffer.task.worker.UnmatchedSearchRecordProcessWorker2;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/5/5
 * Function :
 */
@Controller
@RequestMapping(value = "/autosearch")
public class AutoSearchMatchController {

    @Resource
    IProductService productService;
    @Resource
    ISearchService searchService;
    @Resource
    SearchProductService searchProductService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    private Logger logger = LoggerFactory.getLogger(AutoSearchMatchController.class);

    //autosearch/start
    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public
    @ResponseBody
    String autosearch(@RequestParam(defaultValue = "1") String counts) {
        ExecutorService es = Executors.newCachedThreadPool();

        LinkedBlockingQueue<SrmSearchLog> searchLogQueue = new LinkedBlockingQueue<SrmSearchLog>();
        es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordListWorker(productService, searchService, searchLogQueue)));
        for (int i = 0; i < 50; i++) {
            es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordProcessWorker(productService, searchService, searchLogQueue)));
        }

        while (true) {
            try {
                TimeUnit.MINUTES.sleep(30);
                logger.debug("AutoSearchMatchController");
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/start2", method = RequestMethod.GET)
    @ResponseBody
    public String start2() {
        logger.debug("start2");

        ExecutorService es = Executors.newCachedThreadPool();

        LinkedBlockingQueue<SrmSearchLog> searchLogQueue = new LinkedBlockingQueue<SrmSearchLog>();
        es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordListWorker2(searchService, dbm, searchLogQueue)));
        for (int i = 0; i < 20; i++) {
            es.execute(DaemonThreadFactory.create(new UnmatchedSearchRecordProcessWorker2(searchProductService, searchService, searchLogQueue)));
        }

        while (true) {
            try {
                TimeUnit.MINUTES.sleep(30);
                logger.debug("AutoSearchMatchController-new fetcher");
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
    }

    @RequestMapping(value = "/start3", method = RequestMethod.GET)
    @ResponseBody
    public String start3() {
        logger.debug("start3");

        long startTime = TimeUtils.today();
        int size = 200;

        boolean hasNext = true;

        while (hasNext) {
            Query query = new Query(
                    Criteria.where("lUpdateTime").gt(startTime)
                            .andOperator(Criteria.where("relatedProId").is(0)
                                    .andOperator(Criteria.where("lRelateTime").is(0)))
            );

            query.with(new Sort(Sort.Direction.ASC, "lUpdateTime"));

            try {
                PageableResult<SrmAutoSearchResult> pageableResult = mdm.queryPage(SrmAutoSearchResult.class, query, 1, size);
                List<SrmAutoSearchResult> autoSearchResults = pageableResult.getData();

                if (ArrayUtils.hasObjs(autoSearchResults)) {

                    for (SrmAutoSearchResult autoSearchResult : autoSearchResults) {

                        ProductBo productBo = null;

                        String proIdStr = autoSearchResult.getId();

                        if (NumberUtils.isNumber(proIdStr)) {
                            long proId = Long.valueOf(proIdStr);
                            productBo = productService.getProductBo(proId);
                        }

                        ProductAnalysisService.analysisProducts(autoSearchResult, productBo);
                        searchService.relateUnmatchedSearchLogx(autoSearchResult);
                    }

                    startTime = autoSearchResults.get(autoSearchResults.size() - 1).getlUpdateTime();
                } else {
                    logger.debug("no fetched skus. time = [ " + TimeUtils.parse(startTime, "yyyy-MM-dd HH:mm:ss") + " ]");
                    TimeUnit.SECONDS.sleep(10);
                }
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }

        }

        return "ok";
    }

    @RequestMapping(value = "/start4", method = RequestMethod.GET)

    @ResponseBody
    public String start4() {
        logger.debug("start4");

        final long stime = TimeUtils.now() - TimeUtils.MILLISECONDS_OF_1_HOUR;

        ListAndProcessTask2<SrmAutoSearchResult> listAndProcessTask2 = new ListAndProcessTask2<SrmAutoSearchResult>(
                new IList() {

                    long startTime = stime;
                    boolean runForever = true;

                    @Override
                    public PageableResult getData(int page) {
                        Query query = new Query(
                                Criteria.where("lUpdateTime").gt(startTime)
//                                        .andOperator(Criteria.where("relatedProId").is(0)
//                                                .andOperator(Criteria.where("lRelateTime").is(0)))
                        );

                        query.with(new Sort(Sort.Direction.ASC, "lUpdateTime"));

                        PageableResult<SrmAutoSearchResult> pageableResult = mdm.queryPage(SrmAutoSearchResult.class, query, 1, 500);
                        List<SrmAutoSearchResult> datas = pageableResult.getData();
                        if (ArrayUtils.hasObjs(datas)) {
                            startTime = datas.get(datas.size() - 1).getlUpdateTime();
                        }

                        return pageableResult;
                    }

                    @Override
                    public boolean isRunForever() {
                        return runForever;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {
                        this.runForever = runForever;
                    }

                },
                new IProcess<SrmAutoSearchResult>() {
                    @Override
                    public void process(SrmAutoSearchResult asr) {
                        try {
                            searchService.analysisAndRelate(asr);
                        } catch (Exception e) {
                            logger.debug("[" + asr.getId() + "]" + e.getMessage());
                        }
                    }
                }
        );

        listAndProcessTask2.go();

        return "ok";
    }

    @RequestMapping(value = "/fixtimerset2", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixtimerset2() {
        List<SrmAutoSearchResult> autoSearchResults = mdm.query(SrmAutoSearchResult.class, new Query());

        for (SrmAutoSearchResult autoSearchResult : autoSearchResults) {

            SrmSearchLog searchLog = searchService.findSrmSearchLogById(autoSearchResult.getId());

            long productId = autoSearchResult.getRelatedProId();
            searchService.updateSrmSearchLogStatus(autoSearchResult.getId(), productId, SearchPrecise.TIMERSET2);

            logger.debug(String.format("log-proid[%d], auto-proid[%d]", searchLog.getPtmProductId(), productId));

            mdm.save(autoSearchResult);
        }

        return "ok";
    }

    @RequestMapping(value = "/debug/{logId}", method = RequestMethod.GET)
    public
    @ResponseBody
    String debug(@PathVariable String logId,
                 @RequestParam(defaultValue = "0") String rebuild) {
        try {

            SrmAutoSearchResult autoSearchResult = mdm.queryOne(SrmAutoSearchResult.class, logId);

            searchService.analysisAndRelate(autoSearchResult);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "ok";
    }
}
