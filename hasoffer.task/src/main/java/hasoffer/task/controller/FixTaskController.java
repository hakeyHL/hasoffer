package hasoffer.task.controller;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.TimeUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PriceNode;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import hasoffer.core.task.worker.impl.ListProcessWorkerStatus;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.task.worker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date : 2016/4/14
 * Function :
 */
@Controller
@RequestMapping(value = "/fixtask")
public class FixTaskController {

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    ISearchService searchService;
    @Resource
    IProductService productService;
    @Resource
    IFetchDubboService fetchDubboService;

    private Logger logger = LoggerFactory.getLogger(FixTaskController.class);

    private void print(String str) {
        System.out.println(str);
    }

    /**
     * 转换价格历史数据格式
     */
    @RequestMapping(value = "/convert_price_log", method = RequestMethod.GET)
    @ResponseBody
    public String convert_price_log(@RequestParam String start, @RequestParam String end) {
        final Date END_DATE = TimeUtils.stringToDate(end, "yyyyMMdd");

        Date startD = TimeUtils.stringToDate(start, "yyyyMMdd");
        Date endD = TimeUtils.addDay(startD, 1);
        final ProcessDate pd = new ProcessDate(startD, endD);

        final Set<Long> idSet = new HashSet<>();
        final AtomicInteger count = new AtomicInteger(0);

        final ConcurrentHashMap<Long, ConcurrentHashMap<String, PriceNode>> historyPriceMap = new ConcurrentHashMap<>();

        ListProcessTask<PtmCmpSkuLog> listAndProcessTask2 = new ListProcessTask<>(
                new ILister<PtmCmpSkuLog>() {
                    @Override
                    public PageableResult<PtmCmpSkuLog> getData(int page) {
                        print("date=" + TimeUtils.parse(pd.startDate, "yyyyMMdd") + ", page=" + page + ", count=" + count.get() + ", id set=" + idSet.size());
                        Query query = new Query(Criteria.where("priceTime").gt(pd.getStartDate()).lte(pd.getEndDate()));
                        return mdm.queryPage(PtmCmpSkuLog.class, query, page, 2000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcessor<PtmCmpSkuLog>() {
                    @Override
                    public void process(PtmCmpSkuLog o) {
                        count.addAndGet(1);

                        long sid = o.getPcsId();
                        Date priceTime = o.getPriceTime();
                        String ymd = TimeUtils.parse(priceTime, "yyyyMMdd");

                        ConcurrentHashMap<String, PriceNode> priceNodeMap = historyPriceMap.get(sid);
                        if (priceNodeMap == null) {
                            priceNodeMap = new ConcurrentHashMap<>();
                            historyPriceMap.put(sid, priceNodeMap);
                        }

                        PriceNode pn = priceNodeMap.get(ymd);
                        if (pn == null) {
                            pn = new PriceNode(priceTime, o.getPrice());
                            priceNodeMap.put(ymd, pn);
                        } else {
                            return;
                        }

                        idSet.add(sid);
                    }
                }
        );

        listAndProcessTask2.setQueueMaxSize(1500);
        listAndProcessTask2.setProcessorCount(10);

        while (!pd.isEnd()) {
            listAndProcessTask2.go();
            // save work
            print(String.format("[%s]save map : %d", TimeUtils.parse(pd.startDate, "yyyyMMdd"), historyPriceMap.size()));
            pd.save(historyPriceMap);

            historyPriceMap.clear();
            pd.addDay();

            if (pd.getStartDate().compareTo(END_DATE) > 0) {
                break;
            }
        }

        print("count=" + count.get() + ", id set=" + idSet.size());
        return "ok";
    }

    @RequestMapping(value = "/fixtitlelikedurex", method = RequestMethod.GET)
    public String fixtitlelikedurex() {

        String queryString = "SELECT t FROM PtmProduct t WHERE t.title LIKE '%durex%' AND t.createTime > '2016-05-07 21:57:00' ";

        ListProcessWorkerStatus<PtmProduct> ws = new ListProcessWorkerStatus<PtmProduct>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new MysqlListWorker(queryString, ws, dbm));

        for (int i = 0; i < 10; i++) {
            es.execute(new FixPtmProductWorker(ws, dbm, productService));
        }

        return "ok";
    }

    ///fixtask/fixflipkartsourcesidnull
    @RequestMapping(value = "/fixflipkartsourcesidnull", method = RequestMethod.GET)
    @ResponseBody
    public String fixflipkartsourcesidnull() {

        final String Q_FLIPKART_SKU_SOURCESID_ISNULL = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.sourceSid IS NULL ";
        final String Q_FLIPKART_SKU_SOURCESID_LIKEITME = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.sourceSid LIKE '%itm%' ";

        ExecutorService es = Executors.newCachedThreadPool();

        ListProcessWorkerStatus<PtmCmpSku> ws = new ListProcessWorkerStatus<PtmCmpSku>();

        es.execute(new MysqlListWorker<PtmCmpSku>(Q_FLIPKART_SKU_SOURCESID_ISNULL, ws, dbm));
        es.execute(new MysqlListWorker<PtmCmpSku>(Q_FLIPKART_SKU_SOURCESID_LIKEITME, ws, dbm));

        for (int i = 0; i < 10; i++) {
            es.execute(new FixFlipkartSourceSidWorker(ws, dbm));
        }


        return "ok";
    }

    ///fixtask/fixflipkarturltocleanurl
    @RequestMapping(value = "/fixflipkarturltocleanurl", method = RequestMethod.GET)
    @ResponseBody
    public String fixflipkarturltocleanurl() {

        ExecutorService es = Executors.newCachedThreadPool();

        final String Q_FLIPKART_SKU = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.oriUrl IS NOT NULL ";

        ListProcessWorkerStatus<PtmCmpSku> ws = new ListProcessWorkerStatus<PtmCmpSku>();
        es.execute(new MysqlListWorker<PtmCmpSku>(Q_FLIPKART_SKU, ws, dbm));

        for (int i = 0; i < 10; i++) {
            es.execute(new FixFlipkartCleanUrlWorker(ws, dbm));
        }

        return "ok";
    }

    //fixtask/fixflipkarturlwithoutpid
    @RequestMapping(value = "/fixflipkarturlwithoutpid", method = RequestMethod.GET)
    @ResponseBody
    public String fixflipkarturlwithoutpid() {

        final String Q_FLIPKART_WITHOUTPID1 = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url LIKE '%?pid' ";
        final String Q_FLIPKART_WITHOUTPID2 = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url NOT LIKE '%?%' ";
        final String suffix = "itmefw6ygh9d6yhr";

        List<PtmCmpSku> skuList = dbm.query(Q_FLIPKART_WITHOUTPID1);

        for (PtmCmpSku sku : skuList) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

            updater.getPo().setUrl(sku.getUrl() + suffix);

            dbm.update(updater);

            logger.debug("result = [" + sku.getUrl() + "=" + suffix + "]");
        }

        skuList = dbm.query(Q_FLIPKART_WITHOUTPID2);

        for (PtmCmpSku sku : skuList) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

            updater.getPo().setUrl(sku.getUrl() + "?pid=" + suffix);

            dbm.update(updater);

            logger.debug("result = [" + sku.getUrl() + "?pid=" + suffix + "]");
        }

        return "ok";
    }

    //fixtask/fixflipkarturllikeitm
    @RequestMapping(value = "/fixflipkarturllikeitm", method = RequestMethod.GET)
    @ResponseBody
    public String fixflipkarturllikeitm() {

        final String Q_FLIPKART_URLLIKEITM = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url like '%?pid=itm%' ";
        final String FLIPKART_HEAD = "http://www.flipkart.com";

        List<PtmCmpSku> skuList = dbm.query(Q_FLIPKART_URLLIKEITM);

        for (PtmCmpSku sku : skuList) {

            String url = sku.getUrl();

            url = FlipkartHelper.getUrlByDeeplink(url);

            HttpResponseModel responseModel = HttpUtils.get(url, null);
            String redirect = responseModel.getRedirect();

            url = FLIPKART_HEAD + redirect;

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

            updater.getPo().setUrl(url);
            updater.getPo().setOriUrl(url);

            dbm.update(updater);

            logger.debug("id = [" + sku.getId() + "],url = [" + url + "]");

        }

        return "ok";
    }

    //fixtask/getNoProductCategory
    @RequestMapping(value = "/getNoProductCategory")
    @ResponseBody
    public String getNoProductCategory() {

        final String Q_SECOND_CATEGORY = "SELECT t FROM PtmCategory t WHERE t.level = 2 ";
        final String Q_THIRD_CATEGORY = "SELECT t FROM PtmCategory t WHERE t.level = 3 ";

        ConcurrentLinkedQueue<PtmCategory> categoryQueue = new ConcurrentLinkedQueue<PtmCategory>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new CategoryListWorker(Q_SECOND_CATEGORY, dbm, categoryQueue));
        es.execute(new CategoryListWorker(Q_THIRD_CATEGORY, dbm, categoryQueue));

        for (int i = 0; i < 5; i++) {
            es.execute(new CategoryTestWorker(dbm, categoryQueue));
        }

        return "ok";
    }

    //fixtask/fetchMobileCategoryBrandModel
    @RequestMapping(value = "/fetchMobileCategoryBrandModel")
    @ResponseBody
    public String fetchMobileCategoryBrandModel() {

        ExecutorService es = Executors.newCachedThreadPool();

        ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        for (int i = 0; i < 20; i++) {
            es.execute(new FetchMobileCategoryBrandModel(cmpSkuQueue, fetchDubboService, cmpSkuService));
        }

        es.execute(new FetchMobileCategoryBrandModelListWorker(dbm, cmpSkuQueue, fetchDubboService) {
        });

        return "ok";
    }

    class ProcessDate {

        private Date startDate;
        private Date endDate;

        public ProcessDate(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public boolean isEnd() {
            print(String.format("%s", TimeUtils.parse(startDate, "yyyy-MM-dd")));
            return TimeUtils.today() < this.startDate.getTime();
        }

        public void addDay() {
            this.startDate = TimeUtils.addDay(startDate, 1);
            this.endDate = TimeUtils.addDay(endDate, 1);
        }

        public void save(ConcurrentHashMap<Long, ConcurrentHashMap<String, PriceNode>> historyPriceMap) {
            Iterator<Long> it = historyPriceMap.keySet().iterator();
            int total = historyPriceMap.size();
            int count = 0;
            while (it.hasNext()) {
                Long sid = it.next();
                ConcurrentHashMap<String, PriceNode> priceNodeMap = historyPriceMap.get(sid);
                List<PriceNode> priceNodes = new ArrayList<>();
                for (ConcurrentHashMap.Entry<String, PriceNode> priceNodeEntry : priceNodeMap.entrySet()) {
                    priceNodes.add(priceNodeEntry.getValue());
                }

                cmpSkuService.saveHistoryPrice(sid, priceNodes);
                count++;
                if (count % 400 == 0) {
                    print(String.format("save %d/%d", count, total));
                }
            }
        }
    }
}
