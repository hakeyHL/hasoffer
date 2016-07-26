package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.TitleCountVo;
import hasoffer.admin.worker.FixSkuErrorInPriceWorker;
import hasoffer.admin.worker.ShopcluesOffsaleUpdateWorker;
import hasoffer.admin.worker.ShopcluesStockOutUpdateWorker;
import hasoffer.admin.worker.ShopcluesUrlFixListWorker;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.*;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.HijackLog;
import hasoffer.core.persistence.mongo.UrmDeviceRequestLog;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuIndex2Updater;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.*;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel;
import hasoffer.core.search.ISearchService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.core.user.IDeviceService;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.fetch.sites.paytm.PaytmHelper;
import hasoffer.fetch.sites.shopclues.ShopcluesHelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

/**
 * Date : 2016/3/25
 * Function :
 */
@Controller
@RequestMapping(value = "/fixdata")
public class FixController {

    private static final String Q_SHOPCLUES_OFFSALE = "SELECT t FROM PtmCmpSku t WHERE t.website = 'SHOPCLUES' AND t.status = 'OFFSALE' ORDER BY t.id";
    private static final String Q_SHOPCLUES_STOCKOUT = "SELECT t FROM PtmCmpSku t WHERE t.website = 'SHOPCLUES' AND t.status = 'OUTSTOCK' ORDER BY t.id";

    private static final String Q_PTMCATEGORY_BYPARENTID = "SELECT t FROM PtmCategory t WHERE t.parentId = ?0 ";
    private static final String Q_PTMCMPSKU_BYCATEGORYID = "SELECT t FROM PtmCmpSku t WHERE t.categoryId = ?0 ";

    private static final String Q_PTMCMPSKU = "SELECT t FROM PtmCmpSku t WHERE t.productId < 100000";
    private static final String Q_INDEX = "SELECT t FROM PtmCmpSkuIndex2 t ORDER BY t.id ASC";

    private final static String Q_TITLE_COUNT = "SELECT t.title,COUNT(t.id) FROM PtmProduct t WHERE t.title is not null GROUP BY t.title HAVING COUNT(t.id) > 1 ORDER BY COUNT(t.id) DESC";
    private final static String Q_PRODUCT_BY_TITLE = "SELECT t FROM PtmProduct t WHERE t.title = ?0 ORDER BY t.id ASC";

    private static Logger logger = LoggerFactory.getLogger(FixController.class);
    @Resource
    IProductService productService;
    @Resource
    ISearchService searchService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IFetchService fetchService;
    @Resource
    IDeviceService deviceService;
    @Resource
    IDataFixService dataFixService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;
    @Resource
    ICategoryService categoryservice;
    @Resource
    ProductIndexServiceImpl productIndexServiceImpl;

    private LinkedBlockingQueue<TitleCountVo> titleCountQueue = new LinkedBlockingQueue<TitleCountVo>();

    @RequestMapping(value = "/deleteproduct/{proId}", method = RequestMethod.GET)
    public
    @ResponseBody
    String deleteproduct(@PathVariable Long proId) {
        if (proId > 0) {
            PtmProduct product = dbm.get(PtmProduct.class, proId);
            if (product == null) {
                System.out.println("product is null");
                productService.deleteProduct(proId);
            } else {
                System.out.println("product is not null");
                logger.info(product.toString());
            }
        }
        return "ok";
    }

    @RequestMapping(value = "/cleansearchlogs", method = RequestMethod.GET)
    public
    @ResponseBody
    String cleansearchlogs() {

        ListAndProcessTask2<SrmSearchLog> listAndProcessTask2 = new ListAndProcessTask2<SrmSearchLog>(
                new IList<SrmSearchLog>() {
                    @Override
                    public PageableResult<SrmSearchLog> getData(int page) {
                        return searchService.listSearchLogs(page, 1000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<SrmSearchLog>() {
                    @Override
                    public void process(SrmSearchLog o) {
                        long proId = o.getPtmProductId();
                        if (proId > 0) {
                            PtmProduct product = dbm.get(PtmProduct.class, proId);
                            if (product == null) {
                                productService.deleteProduct(proId);
                            }
                        }
                    }
                }
        );

        listAndProcessTask2.go();

        return "ok";
    }

    /**
     * find title Count queue
     *
     * @return
     */
    @RequestMapping(value = "/setsearchcounts", method = RequestMethod.GET)
    public
    @ResponseBody
    String setsearchcounts() {
        // 根据 srmproductsearchcount 表的数据更新 solr
        String sql = "SELECT DISTINCT(t.productId) FROM SrmProductSearchCount t";

        List<Long> ids = dbm.query(sql);

        for (Long id : ids) {
            PtmProduct product = productService.getProduct(id);

            if (product != null) {
                productService.importProduct2Solr(product);
            }
        }
        return "ok";
    }

    /**
     * find title Count queue
     *
     * @return
     */
    @RequestMapping(value = "/findsametitleproducts", method = RequestMethod.GET)
    public
    @ResponseBody
    synchronized String findsametitleproducts() {

        if (titleCountQueue.size() > 0) {
            return "queue size : " + titleCountQueue.size();
        }

        List<Object[]> titleCountMaps = dbm.query(Q_TITLE_COUNT);

        for (Object[] m : titleCountMaps) {
            String title = (String) m[0];
            System.out.println(m[1] + "\t:\t" + title);

            titleCountQueue.add(new TitleCountVo(title, Integer.parseInt(m[1].toString())));
        }

        return "queue size : " + titleCountQueue.size();
    }

    /**
     * 修复title相同的product
     * /fixtask/mergesametitleproduct
     *
     * @return
     */
    @RequestMapping(value = "/mergesametitleproduct", method = RequestMethod.GET)
    public
    @ResponseBody
    String mergesametitleproduct(@RequestParam(defaultValue = "1") String counts) {

        int count = 1;

        TitleCountVo tcv = titleCountQueue.poll();

        while (tcv != null) {
            System.out.println(tcv.toString());

            mergeProducts(tcv.getTitle());

            if (NumberUtils.isNumber(counts)) {
                int countsInt = Integer.parseInt(counts);
                if (count >= countsInt) {
                    break;
                }
            }

            count++;
            tcv = titleCountQueue.poll();
        }

        System.out.println("finished.");

        return "ok";
    }

    private void mergeProducts(String title) {

        List<PtmProduct> products = dbm.query(Q_PRODUCT_BY_TITLE, Arrays.asList(title));

        if (!ArrayUtils.hasObjs(products) || products.size() <= 1) {
            return;
        }

        PtmProduct finalProduct = products.get(0);

        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(finalProduct.getId());

        Map<String, PtmCmpSku> cmpSkuMap = new HashMap<String, PtmCmpSku>();
        for (PtmCmpSku cmpSku : cmpSkus) {
            if (cmpSku.getWebsite() == null) {
                // todo 处理
                continue;
            }
            cmpSkuMap.put(cmpSku.getUrl(), cmpSku);
        }

        // 处理其他 products
        // cmpsku 合并
        int size = products.size();
        System.out.println(String.format("[%s] products would be merged into product[%d].", size, finalProduct.getId()));
        for (int i = 1; i < size; i++) {
            searchService.mergeProducts(finalProduct, cmpSkuMap, products.get(i));
        }

    }

    @RequestMapping(value = "/fiximages/{site}", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixImage(@PathVariable final String site) {
        final String Q_PRODUCT_WEBSITE =
                "SELECT t FROM PtmProduct t WHERE t.sourceSite=?0";

        ListAndProcessTask2<PtmProduct> listAndProcessTask2 = new ListAndProcessTask2<PtmProduct>(
                new IList<PtmProduct>() {
                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_PRODUCT_WEBSITE, page, 500, Arrays.asList(site));
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        try {
                            System.out.println(o.getId() + "\t [re load image] " + TimeUtils.now());
                            // update image for product
                            String sourceUrl = o.getSourceUrl();
                            // visit flipkart page to get image url
                            String oriImageUrl = "";

                            oriImageUrl = fetchService.fetchWebsiteImageUrl(Website.valueOf(site), sourceUrl);

//                            if (Website.FLIPKART.name().equals(site)) {
//                                oriImageUrl = fetchService.fetchFlipkartImageUrl(sourceUrl);
//                            } else if (Website.SNAPDEAL.name().equals(site)) {
//                                oriImageUrl = fetchService.fetchSnapdealImageUrl(sourceUrl);
//                            } else if (Website.EBAY.name().equals(site)) {
//                                oriImageUrl = fetchService.fetchEbayImageUrl(sourceUrl);
//                            }

                            productService.updateProductImage2(o.getId(), oriImageUrl);

                        } catch (Exception e) {
                            logger.debug(e.getMessage() + "\t" + o.getId());
                        }
                    }
                }
        );

        listAndProcessTask2.go();

        return "ok";
    }

    @RequestMapping(value = "/createskuindex", method = RequestMethod.GET)
    public
    @ResponseBody
    String createskuindex() {
        ListAndProcessTask2<PtmCmpSku> listAndProcessTask2 = new ListAndProcessTask2<PtmCmpSku>(new IList() {
            @Override
            public PageableResult getData(int page) {
                return dbm.queryPage(Q_PTMCMPSKU, page, 2000);
            }

            @Override
            public boolean isRunForever() {
                return false;
            }

            @Override
            public void setRunForever(boolean runForever) {

            }
        }, new IProcess<PtmCmpSku>() {
            @Override
            public void process(PtmCmpSku cmpSku) {
                try {
                    cmpskuIndexService.createOrUpdate(new CmpSkuModel(cmpSku));
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
            }
        });

        listAndProcessTask2.go();

        return "ok";
    }

    //fixdata/products
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixdataproducts() {

        Date date = TimeUtils.stringToDate("2016-07-01 21:40:35", "yyyy-MM-dd HH:mm:ss");

        final int page = 1, size = 200;

        PageableResult<PtmProduct> pagedProducts = productService.listProductsByCreateTime(date, page, size);

        final long totalPage = pagedProducts.getTotalPage();

        List<PtmProduct> products = null;

        for (int i = 1; i <= totalPage; i++) {

            if (i > 1) {
                pagedProducts = productService.listProductsByCreateTime(date, page, size);
            }

            products = pagedProducts.getData();

            if (ArrayUtils.hasObjs(products)) {
                for (PtmProduct product : products) {
                    long count = searchService.statLogsCountByProduct(product.getId());

                    if (count == 0) {
                        productService.deleteProduct(product.getId());
                        System.out.println("delete product : " + product.getId() + " - " + TimeUtils.parse(date, "yyyy-MM-dd HH:mm:ss"));
                    }

                    date = product.getCreateTime();
                }
            }
        }


        return "ok";
    }

    @RequestMapping(value = "/fixshopcluesoffsaleurlbyresearch", method = RequestMethod.GET)
    public String fixshopcluesoffsaleurlbyresearch() {

        ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new ShopcluesUrlFixListWorker(dbm, skuQueue, Q_SHOPCLUES_OFFSALE));

        for (int i = 0; i < 10; i++) {
            es.execute(new ShopcluesOffsaleUpdateWorker(skuQueue, cmpSkuService, dbm, dataFixService));
        }

        return "ok";
    }

    @RequestMapping(value = "/fixshopcluesstockouturlbyresearch", method = RequestMethod.GET)
    public String fixshopcluesstockouturlbyresearch() {

        ConcurrentLinkedQueue<PtmCmpSku> skuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new ShopcluesUrlFixListWorker(dbm, skuQueue, Q_SHOPCLUES_STOCKOUT));

        for (int i = 0; i < 10; i++) {
            es.execute(new ShopcluesStockOutUpdateWorker(skuQueue, cmpSkuService, dbm, dataFixService));
        }

        return "ok";
    }

    @RequestMapping(value = "/hijacklogfix", method = RequestMethod.GET)
    public String hijacklogfix() {

        long minTime = 1462363145553L;
        boolean flag = true;


        Date startDate = TimeUtils.stringToDate("2016-04-29 00:00:00", "yyyy-MM-dd hh:mm:ss");

        while (flag) {

            Date endDate = TimeUtils.add(startDate, TimeUtils.MILLISECONDS_OF_1_MINUTE * 30);

            Query query = new Query();
            query.addCriteria(Criteria.where("createTime").gte(startDate).lt(endDate));

            List<UrmDeviceRequestLog> requestLogList = mdm.query(UrmDeviceRequestLog.class, query);

            if (requestLogList == null || requestLogList.size() == 0) {
                startDate = endDate;
                logger.debug("get 0 log and new startTime:" + startDate.toString());
                continue;
            }

            for (UrmDeviceRequestLog requestLog : requestLogList) {

                if (requestLog.getCreateTime().getTime() > minTime) {
                    logger.debug(requestLog.getId() + " stop");
                    flag = false;
                    break;
                }

                String query1 = requestLog.getQuery();
                if (!StringUtils.isEmpty(query1) && query1.contains("rediToAffiliateUrl")) {

                    Website website = requestLog.getCurShopApp();

                    HijackLog hijackLog = new HijackLog();
                    hijackLog.setId(requestLog.getId());
                    hijackLog.setCreateTime(requestLog.getCreateTime());
                    hijackLog.setWebsite(website);

                    mdm.save(hijackLog);
                    logger.debug(requestLog.getId() + " parse success");
                }
            }

            startDate = endDate;
            logger.debug("new startTime:" + startDate.toString());
        }

        return "ok";
    }

    @RequestMapping(value = "/fixpaytmoriurl", method = RequestMethod.GET)
    public String fixpaytmoriurl() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.oriUrl LIKE '%//catalog.paytm.com/v1/%'";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            String oriUrl = ptmCmpSku.getOriUrl();

            oriUrl = oriUrl.replace("//catalog.paytm.com/v1/", "//paytm.com/shop/");

            updater.getPo().setOriUrl(oriUrl);

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " oriUrl fix success");
        }

        return "ok";
    }


    @RequestMapping(value = "/fixpaytmurlnull", method = RequestMethod.GET)
    public String fixpaytmurlnull() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'PAYTM' AND t.url IS NULL ";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            updater.getPo().setUrl(PaytmHelper.getCleanUrl(ptmCmpSku.getOriUrl()));

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " update success");
        }

        return "ok";
    }


    @RequestMapping(value = "/fixpaytmurl", method = RequestMethod.GET)
    public String fixpaytmurl() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.url LIKE '%//catalog.paytm.com/v1/%'";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            String url = ptmCmpSku.getUrl();

            url = url.replace("//catalog.paytm.com/v1/", "//paytm.com/shop/");

            updater.getPo().setUrl(url);

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " url fix success");
        }

        return "ok";
    }

    //
    @RequestMapping(value = "/fixshopcluesurlnull", method = RequestMethod.GET)
    public String fixshopcluesurlnull() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'SHOPCLUES' AND t.url IS NULL AND t.oriUrl IS NOT NULL";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            updater.getPo().setUrl(ShopcluesHelper.getCleanUrl(ptmCmpSku.getOriUrl()));

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " update success");
        }

        return "ok";
    }

    //fixdata/fixfilpkarturlcontainnull
    @RequestMapping(value = "/fixfilpkarturlcontainnull", method = RequestMethod.GET)
    public String fixfilpkarturlcontainnull() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url like '%?pid=null%' ";

        String startUrl = "http://www.flipkart.com";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            String url = ptmCmpSku.getUrl();

            try {

                HttpResponseModel response = HtmlUtils.getResponse(ptmCmpSku.getUrl(), 3);

                String redirect = response.getRedirect();

                url = startUrl + redirect;

            } catch (Exception e) {
                logger.debug("parse error for [" + url + "]" + e.toString());
            }

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            updater.getPo().setUrl(url);
            String sourceSid = FlipkartHelper.getSkuIdByUrl(url);
            updater.getPo().setSourceSid(sourceSid);

            dbm.update(updater);
            logger.debug(ptmCmpSku.getId() + " update success");
        }

        return "ok";
    }

    //fixdata/fixflipkarturlnull
    @RequestMapping(value = "/fixflipkarturlnull", method = RequestMethod.GET)
    @ResponseBody
    public String fixflipkarturlnull() {

        final String Q_FLIPKART_NULLURL = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url IS NULL AND t.oriUrl IS NOT NULL";

        List<PtmCmpSku> skus = dbm.query(Q_FLIPKART_NULLURL);

        for (PtmCmpSku sku : skus) {

            String oriUrl = sku.getOriUrl();

            String url = FlipkartHelper.getCleanUrl(oriUrl);

            url = FlipkartHelper.getUrlByDeeplink(url);

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

            updater.getPo().setUrl(url);

            dbm.update(updater);

            logger.debug("update url success for [" + sku.getId() + "] to [" + url + "]");
        }

        return "ok";
    }

    //fixdata/fixerrorskuinprice
    @RequestMapping(value = "/fixerrorskuinprice", method = RequestMethod.GET)
    @ResponseBody
    public String fixerrorskuinprice() {

        final ConcurrentLinkedQueue<Long> idQueue = new ConcurrentLinkedQueue<Long>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {
            @Override
            public void run() {

                long i = 1;

                while (true) {

                    if (idQueue.size() > 2000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        idQueue.add(i);
                        i++;
                    }
                }
            }
        });

        for (int i = 0; i < 10; i++) {
            es.execute(new FixSkuErrorInPriceWorker(idQueue, dbm));
        }

        return "ok";
    }

    //fixdata/fixSourceSidIndex
    @RequestMapping(value = "/fixSourceSidIndex", method = RequestMethod.GET)
    @ResponseBody
    public String fixSourceSidIndex() {

        int curPage = 1;
        int pageSize = 1000;

        PageableResult<PtmCmpSkuIndex2> pageableResult = dbm.queryPage(Q_INDEX, curPage, pageSize);

        long totalPage = pageableResult.getTotalPage();
        List<PtmCmpSkuIndex2> indexList = pageableResult.getData();

        while (curPage <= totalPage) {

            if (curPage > 1) {
                pageableResult = dbm.queryPage(Q_INDEX, curPage, pageSize);
                indexList = pageableResult.getData();
            }

            for (PtmCmpSkuIndex2 index : indexList) {

                PtmCmpSkuIndex2Updater updater = new PtmCmpSkuIndex2Updater(index.getId());

                Website website = index.getWebsite();
                String sourceSid = index.getSourceSid();

                String skuTitle = index.getSkuTitle();
                String newSiteSkutitleIndex = HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(skuTitle));
                String oriSiteSkutitleIndex = index.getSiteSkuTitleIndex();

                if (!StringUtils.isEqual(newSiteSkutitleIndex, oriSiteSkutitleIndex)) {
                    updater.getPo().setSiteSkuTitleIndex(newSiteSkutitleIndex);
                }

                updater.getPo().setSiteSourceSidIndex(HexDigestUtil.md5(website.name() + sourceSid));


                dbm.update(updater);

                logger.debug("update success for [" + index.getId() + "]");
            }

            logger.debug(curPage + " page success ; left " + (totalPage - curPage) + "page");
            curPage++;
        }

        return "ok";
    }

    //fixdata/testcategoryresult
    @RequestMapping("/testcategoryresult")
    @ResponseBody
    public String testcategoryresult() {

        List<PtmCategory> secondCategoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.level = 2 ");

        for (PtmCategory category : secondCategoryList) {

            List<Object> thirdCategoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.parentId = ?0 ", Arrays.asList(category.getId()));

            if (thirdCategoryList == null || thirdCategoryList.size() == 0) {
                PageableResult<ProductModel> pageableResult = productIndexServiceImpl.searchPro(category.getId(), 2, 1, 20);
                if (pageableResult.getNumFund() != 0) {
                    continue;
                } else {
                    System.out.println(category.getId());
                }
            } else {
                continue;
            }

        }

        List<PtmCategory> thirdCategoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.level = 3 ");

        for (PtmCategory category : thirdCategoryList) {

            PageableResult<ProductModel> pageableResult = productIndexServiceImpl.searchPro(category.getId(), 3, 1, 20);
            if (pageableResult.getNumFund() != 0) {
                continue;
            } else {
                System.out.println(category.getId());
            }

        }

        return "";
    }

    //fixdata/fixproductprice
    @RequestMapping(value = "/fixproductprice")
    @ResponseBody
    public String fixproductprice() {

        final ConcurrentLinkedQueue<PtmProduct> productQueue = new ConcurrentLinkedQueue<PtmProduct>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {

            @Override
            public void run() {

                int curPage = 1;

                int pageSize = 1000;

                PageableResult<PtmProduct> pageableResult = dbm.queryPage("SELECT t FROM PtmProduct t Where t.id > ?0 ORDER BY t.id ASC ", curPage, pageSize, Arrays.asList(512276L));

                while (true) {

                    if (curPage > 1) {
                        pageableResult = dbm.queryPage("SELECT t FROM PtmProduct t Where t.id > ?0 ORDER BY t.id ASC ", curPage, pageSize, Arrays.asList(512276L));
                    }

                    if (productQueue.size() > 5000) {
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    List<PtmProduct> productList = pageableResult.getData();

                    for (PtmProduct ptmProduct : productList) {

                        long skuNumber = dbm.querySingle("SELECT COUNT(*) FROM PtmCmpSku t WHERE t.productId = ?0 AND t.status = 'ONSALE' ", Arrays.asList(ptmProduct.getId()));

                        if (skuNumber > 0) {
                            productQueue.add(ptmProduct);
                        }

                    }

                    curPage++;
                }
            }
        });


        for (int i = 0; i < 5; i++) {

            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        PtmProduct ptmProduct = productQueue.poll();

                        if (ptmProduct == null) {
                            continue;
                        }

                        try {
                            productService.updatePtmProductPrice(ptmProduct.getId());
                        } catch (Exception e) {
                            productQueue.add(ptmProduct);
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                }
            });

        }

        return "ok";
    }
}
