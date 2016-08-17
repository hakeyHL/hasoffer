package hasoffer.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.admin.controller.vo.TitleCountVo;
import hasoffer.admin.worker.FixSkuErrorInPriceWorker;
import hasoffer.admin.worker.FlipkartSkuCategory2GetListWorker;
import hasoffer.admin.worker.FlipkartSkuCategory2GetSaveWorker;
import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.ImagePath;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.*;
import hasoffer.core.analysis.ProductAnalysisService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.HijackLog;
import hasoffer.core.persistence.mongo.UrmDeviceRequestLog;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuIndex2Updater;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.*;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.utils.Httphelper;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.fetch.sites.paytm.PaytmHelper;
import hasoffer.fetch.sites.shopclues.ShopcluesHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
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
    @Resource
    ICacheService cacheServiceImpl;
    private LinkedBlockingQueue<TitleCountVo> titleCountQueue = new LinkedBlockingQueue<TitleCountVo>();

    /**
     * 该方法用于将现有sku中（Date：2016-08-08）,flipkart的被访问的sku，找到其对应的类目
     */
    //flipkart/getflipkartskucate2
    @RequestMapping(value = "/getFlipkartSkuCate2", method = RequestMethod.GET)
    @ResponseBody
    public String getFlipkartSkuCate2() {

        //俩种添加策略
        //1.按照访问向队列添加
        //2.按照id升序向队列添加
        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.categoryid = 0";
//        String queryString = ;

        ListAndProcessWorkerStatus ws = new ListAndProcessWorkerStatus();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new FlipkartSkuCategory2GetListWorker(queryString, ws, dbm));

        for (int i = 0; i < 10; i++) {
            es.execute(new FlipkartSkuCategory2GetSaveWorker(dbm, ws));
        }

        return "ok";
    }

    @RequestMapping(value = "/fixproductcmps/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public String fixproductcmps1(@PathVariable long productId) {
        fixProductCmps(productId);
        return "ok";
    }

    @RequestMapping(value = "/fixmultiskus", method = RequestMethod.GET)
    @ResponseBody
    public String fixmultiskus(@RequestParam String filename) {
        File file = new File("/home/hasoffer/tmp/" + filename);
        List<String> lines;
        try {
            lines = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
        int count = 0;
        for (String line : lines) {

            String[] vals = line.split("\t");
            long skuCount = Long.valueOf(vals[0].trim());
            long productId = Long.valueOf(vals[1].trim());

            System.out.println(productId + "\t" + skuCount);

            fixProductCmps(productId);

            count++;
            if (count % 100 == 0) {
                System.out.println(count + "..products processed.");
//                break;
            }
        }

        return "ok";
    }

    private void fixProductCmps(long productId) {
        PtmProduct product = productService.getProduct(productId);
        if (product != null) {
            System.out.println("---------------- " + productId + " ----------------");
            System.out.println(product.getTitle());
            Set<String> skuUrlSet = new HashSet<>();

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId);
            for (PtmCmpSku cmpSku : cmpSkus) {
                if (!StringUtils.isEmpty(product.getTitle())) {
                    System.out.println(cmpSku.getTitle());
                    float score = ProductAnalysisService.stringMatch(product.getTitle(), cmpSku.getTitle());
                    if (score < 0.4) {
                        logger.debug(String.format("[Delete_%d]Score is [%f].", cmpSku.getId(), score));
                        cmpSkuService.deleteCmpSku(cmpSku.getId());
                        continue;
                    }
                }

                boolean exists = skuUrlSet.contains(cmpSku.getUrl());

                if (exists) {
                    logger.debug(String.format("[Delete_%d] Exist.", cmpSku.getId()));
                    cmpSkuService.deleteCmpSku(cmpSku.getId());
                } else {
                    skuUrlSet.add(cmpSku.getUrl());
                }

            }

            System.out.println("---------------------end-----------------------");
        }
    }

    //fixdata/updateptmproduct/{id}
    @RequestMapping(value = "/updateptmproduct/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String updateptmproduct(@PathVariable long id) {

        //更新商品价格
        productService.updatePtmProductPrice(id);
        //清除product缓存
        cacheServiceImpl.del("PRODUCT_" + id);
        //清除sku缓存        PRODUCT__listPagedCmpSkus_3198_1_10
        Set<String> keys = cacheServiceImpl.keys("PRODUCT__listPagedCmpSkus_" + id + "_*");

        for (String key : keys) {
            cacheServiceImpl.del(key);
        }

        return "ok";
    }

    @RequestMapping(value = "/setprostdbyml", method = RequestMethod.GET)
    public
    @ResponseBody
    String setprostdbyml() {


        System.out.println("all finished.");

        return "ok";
    }

    @RequestMapping(value = "/initproductifstd", method = RequestMethod.GET)
    public
    @ResponseBody
    String initproductifstd() {

        List<PtmCategory> stdCates = getStdCategories();

        int page = 1, size = 2000;

        int len = stdCates.size();
        for (int i = 0; i < len; i++) {

            PtmCategory cate = stdCates.get(i);

            System.out.println(String.format("set cate[%d] to std product", cate.getId()));

            List<PtmProduct> products = productService.listProducts(cate.getId(), 1, Integer.MAX_VALUE);

            for (PtmProduct o : products) {
                productService.updateProductStd(o.getId(), true);
            }
        }

        System.out.println("all finished.");

        return "ok";
    }

    private List<PtmCategory> getStdCategories() {

        Long[] cateIds = new Long[]{1L, 257L, 4662L, 1504L, 2334L};

        List<PtmCategory> cates = new ArrayList<>();

        for (Long cateId : cateIds) {
            PtmCategory category = categoryservice.getCategory(cateId);
            getStdCategories(cates, category);
        }

        return cates;
    }

    private void getStdCategories(List<PtmCategory> cates, PtmCategory category) {
        if (category == null) {
            return;
        }
        cates.add(category);
        if (category.getLevel() < 3) {
            List<PtmCategory> cates2 = categoryservice.listSubCategories(category.getId());
            for (PtmCategory cate : cates2) {
                getStdCategories(cates, cate);
            }
        }
    }

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

    @RequestMapping(value = "/deleteproductanyway/{proId}", method = RequestMethod.GET)
    public
    @ResponseBody
    String deleteproduct2(@PathVariable Long proId) {
        if (proId > 0) {
            productService.deleteProduct(proId);
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


                while (true) {

                    try {
                        PageableResult<PtmProduct> pageableResult = dbm.queryPage("SELECT t FROM PtmProduct t Where t.id > ?0 ORDER BY t.id ASC ", curPage, pageSize, Arrays.asList(1639554L));


                        if (curPage > 1) {
                            pageableResult = dbm.queryPage("SELECT t FROM PtmProduct t Where t.id > ?0 ORDER BY t.id ASC ", curPage, pageSize, Arrays.asList(1639554L));
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

                    } catch (Exception e) {
                        continue;
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

    //fixdata/fixsmallimagepathnull
    @RequestMapping("/fixsmallimagepathnull")
    @ResponseBody
    public String fixsmallimagepathnull() {


        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        int processCount = 20;

        es.execute(new Runnable() {

            String Q_SKU_IMAGE = "SELECT t FROM PtmCmpSku t WHERE t.smallImagePath is null and t.status <> 'OFFSALE' and t.price > 0 and t.oriImageUrl IS NOT NULL and t.oriImageUrl <> '' ORDER BY t.id";

            int page = 1, PAGE_SIZE = 1000;

            @Override
            public void run() {

                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);

                long totalPage = pageableResult.getTotalPage();
                System.out.println("totalpage =" + totalPage);

                while (page < totalPage) {

                    if (cmpSkuQueue.size() > 10000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {

                        }
                    }

                    if (page > 1) {
                        pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);
                    }

                    List<PtmCmpSku> ptmCmpSkuList = pageableResult.getData();

                    for (PtmCmpSku ptmCmpSku : ptmCmpSkuList) {

                        if (WebsiteHelper.DEFAULT_WEBSITES.contains(ptmCmpSku.getWebsite())) {

                            cmpSkuQueue.add(ptmCmpSku);
                        }
                    }

                    System.out.println("queue size =" + cmpSkuQueue.size());
                    System.out.println("currentPage =" + page);
//                    break;//for test
                    page++;
                }
            }
        });

        for (int i = 0; i < processCount; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        PtmCmpSku t = cmpSkuQueue.poll();

                        if (t == null) {
//                            System.out.println("poll get null sleep 15 seconds");
                            try {
                                TimeUnit.SECONDS.sleep(15);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        String oriImageUrl = t.getOriImageUrl();
                        System.out.println("ready to download " + t.getId());

                        try {
                            ImagePath imagePath = hasoffer.core.utils.ImageUtil.downloadAndUpload2(oriImageUrl);

                            cmpSkuService.fixSmallImagePath(t.getId(), imagePath.getSmallPath());

                            System.out.println("fix success for " + t.getId());
                        } catch (ImageDownloadOrUploadException e) {
                            System.out.println("down image error for " + t.getId());
                        }


                    }
                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                break;
            }
        }

        return "ok";
    }

    //fixdata/fixflipkartcategoryidnull
    @RequestMapping(value = "/fixflipkartcategoryidnull")
    @ResponseBody
    public String fixflipkartcategoryidnull() {

        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {

            String Q_SKU_IMAGE = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' and t.categoryId IS NULL ORDER BY t.id";

            int page = 1, PAGE_SIZE = 1000;

            @Override
            public void run() {

                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);

                long totalPage = pageableResult.getTotalPage();
                System.out.println("totalpage =" + totalPage);

                while (page < totalPage) {

                    if (cmpSkuQueue.size() > 10000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {

                        }
                    }

                    if (page > 1) {
                        pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);
                    }

                    List<PtmCmpSku> ptmCmpSkuList = pageableResult.getData();

                    cmpSkuQueue.addAll(ptmCmpSkuList);

                    System.out.println("queue size =" + cmpSkuQueue.size());
                    System.out.println("currentPage =" + page);
//                    break;//for test
                    page++;
                }
            }
        });

        for (int i = 0; i < 10; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        PtmCmpSku ptmcmpsku = cmpSkuQueue.poll();

                        if (ptmcmpsku == null) {
//                            System.out.println("poll get null sleep 15 seconds");
                            try {
                                TimeUnit.SECONDS.sleep(15);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        System.out.println("ready to parse " + ptmcmpsku.getId());

                        String skuid = FlipkartHelper.getSkuIdByUrl(ptmcmpsku.getUrl());

                        String url = "https://www.flipkart.com/api/3/page/dynamic/product";

                        String json = "{\"requestContext\":{\"productId\":\"" + skuid + "\"}}";

                        Map<String, String> header = new HashMap<>();

                        header.put("x-user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 FKUA/website/41/website/Desktop");

                        try {

                            String response = Httphelper.doPostJsonWithHeader(url, json, header);

                            JSONObject responseJson = JSON.parseObject(response);

                            JSONArray jsonArray = responseJson.getJSONObject("response").getJSONObject("product_breadcrumb").getJSONObject("data").getJSONObject("0").getJSONObject("value").getJSONArray("productBreadcrumbs");

                            String catepath = "";

                            for (int i = 0; i < jsonArray.size(); i++) {
                                if (i > 2) {
                                    break;
                                }

                                catepath = jsonArray.getJSONObject(i).getString("title");
                            }

                            if (StringUtils.isEmpty(catepath)) {

                                PtmCategory3 category3 = dbm.querySingle("SELECT t FROM PtmCategory3 t WHERE t.name = ?0", Arrays.asList(catepath));


                            }

                        } catch (Exception e) {
                            System.out.println("parse exception for " + ptmcmpsku.getId());
                        }
                    }
                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                break;
            }
        }

        return "ok";
    }

    //fixdata/fixSkuSmallImagePathSizeZero
    @RequestMapping(value = "/fixSkuSmallImagePathSizeZero")
    @ResponseBody
    public String fixSkuSmallImagePathSizeZero() {

        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {

            @Override
            public void run() {

                String[] strArray = {"0717", "0719", "0720", "0721", "0722", "0723", "0724", "0725", "0726", "0727", "0728", "0729", "0730", "0731", "0801", "0802", "0803", "0804", "0805", "0806", "0808", "0809", "0810", "0812", "0813"};

                for (int i = 0; i < strArray.length; i++) {

                    String str = strArray[i];
                    System.out.println("cur str" + str);

                    int curPage = 1;
                    int pageSize = 1000;

                    PageableResult<PtmCmpSku> pageableResult = dbm.queryPage("SELECT t FROM PtmCmpSku t WHERE t.smallImagePath like '/2016/" + str + "/%' ", curPage, pageSize);

                    long totalPage = pageableResult.getTotalPage();

                    while (curPage <= totalPage) {

                        if (cmpSkuQueue.size() > 10000) {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            System.out.println("queue size = " + cmpSkuQueue.size());
                            continue;
                        }

                        if (curPage > 1) {
                            pageableResult = dbm.queryPage("SELECT t FROM PtmCmpSku t WHERE t.smallImagePath like '/2016/" + str + "/%' ", curPage, pageSize);
                        }

                        List<PtmCmpSku> cmpSkuList = pageableResult.getData();

                        cmpSkuQueue.addAll(cmpSkuList);

                        curPage++;
                    }
                }
            }
        });

        for (int i = 0; i < 10; i++) {

            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        PtmCmpSku sku = cmpSkuQueue.poll();

                        if (sku == null) {
                            try {
                                TimeUnit.SECONDS.sleep(3);
                            } catch (InterruptedException e) {

                            }
                            continue;
                        }

                        cmpSkuService.downloadImage2(sku);
                    }
                }
            });
        }

        return "ok";
    }

    //fixdata/fixSkuSmallImagePathSizeZeroTest
    @RequestMapping(value = "/fixSkuSmallImagePathSizeZeroTest")
    @ResponseBody
    public String fixSkuSmallImagePathSizeZeroTest() {

        PtmCmpSku sku = dbm.querySingle("SELECT t FROM PtmCmpSku t WHERE t.id = ?0 ", Arrays.asList(6428134L));

        cmpSkuService.downloadImage2(sku);

        return "ok";
    }


    //fixdata/deleteProductByString
    @RequestMapping(value = "/deleteProductByString")
    @ResponseBody
    public String deleteProductByString() {

        String idString = "1017088,1044799,1189854,1190178,1190246,1190757,1191259,1191514,1191745,1192017,1192693,1192973,1193338,1194425,1194647,1195381,1198392,1199494,1199623,1199671,1199898,1200105,1200203,1201348,1201845,1203826,1203844,1204639,1205476,1207069,1207293,1211361,1211564,1212737,1212743,1213258,1213269,1213271,1213327,1213393,1213458,1214639,1216543,1216974,1217255,1217338,1217575,1217868,1218872,1219471,1220061,1220596,1222718,1223341,1223425,1224699,1225455,1226348,1227023,1227031,1227608,1228322,1231292,1231632,1232170,1233200,1233888,1234346,1234753,1234933,1235671,1236386,1237130,1237689,1238467,1239536,1240433,1240542,1242093,1242837,1243989,1244173,1245573,1246031,1246655,1246677,1247603,1248925,1248955,1249049,1249302,1252200,1252326,1252497,1252774,1254712,1254715,1254810,1255337,1256501,1256889,1257681,1258452,1259127,1259143,1259147,1259836,1260145,1260668,1261701,1261712,1262003,1262155,1263336,1263939,1264269,1264823,1265005,1266627,1268347,1268608,1269503,1270463,1270858,1271869,1274101,1274159,1274168,1275104,1275112,1275641,1276474,1276789,1276966,1277361,1278677,1279995,1280913,1280914,1281084,1281746,1282496,1283428,1283877,1283899,1284383,1284389,1284787,1287041,1287741,1288529,1290342,1292284,1292539,1292594,1293759,1294960,1295845,1295861,1300061,1301768,1301822,1301941,1302331,1302747,1303190,1304377,1306308,1306765,1306773,1306862,1307918,1308704,1308838,1310180,1310633,1311182,1314043,1314103,1314119,1316627,1317793,1319184,1319682,1320112,1322445,1323701,1323903,1323928,1324078,1325923,1326526,1328192,1328259,1328334,1328635,1329094,1330262,1333339,1333995,1334817,1335571,1336014,1336161,1336626,1337902,1338328,1340047,1341427,1341675,1341939,1341944,1342926,1343307,1343466,1343478,1343483,1345017,1348376,1348695,1349129,1350268,1351914,1352150,1352937,1353296,1353814,1354030,1354400,1355531,1356398,1357114,1358070,1358233,1359698,1360537,1360693,1360995,1361340,1361504,1362150,1364324,1365245,1365805,1365952,1366062,1367651,1368160,1368834,1369661,1371449,1371547,1372757,1374170,1374863,1375366,1376811,1378000,1378463,1382479,1382560,1383507,1384883,1385325,1385558,1388467,1389230,1389795,1390121,1390125,1390126,1390130,1390281,1392250,1393183,1393981,1394000,1394083,1394868,1395269,1395677,1399297,1399727,1400665,1400714,1400838,1403639,1404245,1404625,1404651,1405053,1405393,1406078,1406396,1406762,1406877,1406997,1407222,1407255,1407446,1407853,1407942,1408345,1408400,1408612,1408740,1408793,1408960,1410202,1410986,1411765,1412587,1412772,1413432,1414882,1414946,1415112,1415518,1415911,1416425,1416613,1419141,1419632,1419918,1419953,1420295,1420394,1420789,1424279,1424299,1424979,1425995,1426195,1426364,1426734,1427847,1427860,1428175,1429308,1429522,1429928,1430145,1431414,1431701,1432156,1432249,1432266,1433022,1433275,1433518,1433924,1437013,1437762,1437847,1439150,1439307,1439919,1442311,1445998,1447712,1450100,1451136,1453047,1454786,1454878,1455099,1455223,1456063,1458096,1459205,1459910,1461028,1462199,1462699,1462977,1463966,1464284,1464776,1464825,1465356,1466373,1467914,1468388,1468390,1470159,1471319,1471487,1472401,1472616,1472958,1473204,1473219,1473345,1474517,1475932,1476037,1476685,1477087,1482285,1487046,1487369,1487974,1488980,1491464,1494036,1500235,1505508,1509906,1509950,1510205,1510707,1510981,1511169,1511469,1511730,1511732,1511748,1513350,1513689,1514149,1514302,1514680,1514775,1514926,1515455,1515513,1515818,1515943,1515981,1515984,1516435,1516470,1516471,1516753,1516966,1519574,1519632,1519739,1519925,1519963,1520017,1521420,1521445,1521543,1522398,1522687,1523004160868353,1523009,1523650,1523734305308673,1523817,1523885,1524795162230785,1524912,1525353507979265,1525747,1525761,1525766,1526109,1526139486994435,1526286,1526793,1526841,1526941,1526963,1527021,1527767279599617,1527870358814721,1528765,1528905,1529072949657602,1529236158414849,1529330647695361,1530808,1534733,1534772,1534775,1534897,1535152,1535335011975169,1535563,1535785,1535876,1536344329289730,1536786,1536787,1536825,1536984279416833,1536988574384129,1537035,1537229092552705,1537365,1537432,1537591,1537619934576641,1537852,1537980,1538097,1538165395423235,1538473,1538556237447170,1538565,1538588,1538708,1538924,1538941,1539035,1539122,1539242,1539252,1539357,1539362,1539433,1539501130252289,1539621389336577,1539694,153973,1540089540771841,1540099,1540222684758018,1540507,1540746670768129,1540762,1540764,1540894,1541224,1541369441026049,1541644318932995,1541859,1542288564027393,1542387348275201,1542461,1542692290953220,1542891,1542892,1542900,1542902,1543030,1543267,1543494,1543615,1543694,1544113,1545516,1545568,1546396,1546504,1547029,1547416754978817,1547592,1548054,1548334,1548533,1549083202289666,1550728174764033,1552308722728962,1553096,1553845,1554636595003393,1554711,1554713,1554923,1555767,1556164,1556310,1556443,1556562,1556563,1556695,1557146,1557518518059010,1557844935573505,1557965194657793,1558733993803777,1558882,1559536,1561697,1561848,1562560,1562815,1563060,1563071,1563483,1564149,1566312,1568584,1570888,1571072,1572298,1573915,1574165,1574384,1575007,1576074,1576103,1576872,1576873,1579247,1580032,1580643,1580709,1580724,1580810,1581275,1581517,1581955,1582300,1582339,1582730,1582763,1582993,1583044,1583259,1583260,1583290,1583503,1583507,1583528,1584551,1584924,1585140,1585480,1585496,1585526,1585634,1586780,1586784,1587302,1587557,1588094,1588096,1588101,1588324,1588346,1588444,1588447,1588455,1588510,1588618,1588765,1588865,1589078,1589118,1589313,1589433,1589910,1590565,1590865,1591186,1591448,1592186,1592581,1592884,1592885,1593040,1593338,1593541,1593810,1593817,1593838,1593883,1594004,1594168,1594247,1594304,1594736,1594883,1595222,1595225,1595403,1595433,1595807,1595816,1595870,1596176,1596498,1596554,1596608,1596955,1596972,1596987,1597052,1597488,1597549,1597776,1597798,1597803,1597813,1597865,1597945,1597957,1597962,1598067,1598222,1598343,1598348,1598424,1598498,1599094,1599122,1599158,1599480,1599544,1599594,1600018,1600509,1600600,1600690,1600694,1600792,1600852,1600984,1601017,1601209,1601262,1601306,1601458,1601649,1602013,1602442,1602449,1602794,1602902,1602933,1602978,1603570,1604099,1604488,1604510,1604543,1605115,1605260,1605564,1605851,1605950,1606477,1606478,1606552,1606585,1607143,1607187,1607295,1607415,1607663,1608095,1608328,1608457,1608467,1608575,1608702,1608912,1609024,1609152,1609522,1609580,1609593,1609646,1609728,1609816,1609831,1609846,1610029,1610282,1610346,1610398,1610416,1610545,1610643,1610822,1610883,1610916,1611070,1611129,1611438,1611504,1611732,1611784,1611861,1611872,1611890,1612100,1612112,1612122,1612236,1612446,1612502,1612621,1612763,1612773,1612789,1613437,1613825,1613980,1614313,1614846,1615055,1615067,1615121,1615393,1615570,1615768,1616052,1616055,1616104,1616131,1616296,1616871,1616875,1617537,1617715,1617935,1618305,1618369,1618670,1618701,1618811,1618814,1618899,1618930,1619021,1619169,1619230,1619256,1619315,1619463,1619618,1619669,1619808,1619867,1619874,1619916,1620042,1620233,1620249,1620354,1620507,1620561,1620692,1620929,1621008,1621295,1621424,1621559,1621578,1621585,1621834,1621854,1622474,1622570,1622621,1623326,1623403,1623535,1623543,1623563,1623935,1624032,1624744,1624745,1624804,1625117,1625159,1625201,16274,1628412,1634053,1636549,1639287,1642338,1643626,1650836,1655892,1656097,1656172,1656634,1656919,1657295,1657947,1658070,1658365,1658831,1658903,1659159,1659813,1660423,1660842,1661243,1661423,1661530,1662000,1662064,1662093,1662094,1672220,1673918,1676255,1679943,1686122,1691870,1696202,1707857,1708001,1710537,1711042,1713155,1714834,1715180,1715794,1716362,1718011,1718372,1718445,1718457,1719298,1719405,1719869,1720708,1722289,1722302,1722904,1722940,1723398,1724299,1724546,1724863,1724966,1725133,1725233,1725263,1726409,1726740,1728958,1729518,1730463,1731261,1731542,1731923,1731930,1731931,1732179,1732812,1732819,1733413,1733820,1734397,1735711,1735849,1736129,1736972,1737157,1737400,1737767,1743564,1745886,1746705,1755186,1766083,1766579,1767093,1769754,1772219,1772570,1774023,1774074,1775237,1775892,1776695,1778397,1779858,1782022,1784915,1785265,1786113,1786974,1787598,1788069,1790129,1790429,1790922,1791339,1791691,1792158,1800240,1800604,1801154,1801971,1802005,1803319,1803350,1803655,1804667,1804682,1805491,1805757,1806325,1806373,1807445,1808642,1808837,1809428,1809493,1809645,1810333,1810439,1810467,1810488,1810620,1810773,1812036,1812107,1812204,1813351,1813655,1813713,1814358,1814523,1815367,1816657,1816800,1819374,1820360,1821047,1821184,1821495,1821658,1821788,1821993,1822248,1822549,1822840,1822957,1823019,1823146,1823166,1823228,1823310,1823313,1823365,1823422,1823516,1823675,1824215,1824309,1825117,1825621,1825775,1825804,1825848,1825904,1826238,1826261,1826486,1826518,1826730,1826806,1828001,1828550,1828815,1828845,1829585,1829876,1830137,1830621,1832744,1833193,1833431,1833603,1833761,1834614,1835086,1835616,1835792,1835914,1835952,1836082,1836425,1837038,1837076,1837445,1838868,1840038,1840082,1840504,1841030,1841169,1841282,1841304,1841940,1843260,1843658,1843834,1844202,1845679,1846003,1846905,1847082,1847348,1847439,1849841,1850214,1850587,1850735,1850863,1850893,1851764,1851947,1852758,1853220,1853274,1855365,1855400,1855656,1856924,1858219,1858258,1859033,1859302,1859500,1859675,1860340,1860420,1862131,1862697,1863135,1863265,1863592,1866181,1866850,1867331,1874125,1874376,1875355,1875729,1876262,1876349,1876495,1877248,1877702,1877728,1878095,1878413,1879055,1880056,1880175,1880300,1880322,1881070,1881573,1881598,1881909,1882023,1882058,1883401,1883482,1883653,1884021,1884672,1885949,1889685,1890075,1890507,1891629,1891997,1892005,1892008,1892044,1892259,1893169,1893598,1893980,1894828,1894946,1895234,1896207,1896558,1897051,1897204,1898007,1899207,1899873,1900187,1901384,1901744,1901761,1902004,1902078,1903238,1903356,1903514,1903626,1903642,1905305,1907798,1908812,1908985,1909990,1911035,1912300,1912445,1912490,1912960,1913355,1914731,1915488,1919599,1919917,1920718,1920893,1921457,1921991,1922918,1923037,1923645,1925603,1925888,1926617,1926941,1927024,1927388,1927661,1928535,1929187,1929248,1929606,1930143,1930989,1931394,1931529,1932463,1932550,1932705,1932889,1933086,1933527,1933638,1934063,1936206,1936210,1936301,1936503,1937785,1938205,1938794,1938849,1938983,1939442,1939892,1940174,1940366,1940562,1940946,1941348,1941398,1942582,1942674,1942682,1942866,1943359,1943494,1943827,1944028,1945735,1945836,1948803,1950929,1951386,1951873,1952704,1952998,1954615,1954640,1954972,1956131,1956348,1958348,1958438,1959051,1960314,1961028,1964937,1964985,1967287,1970842,1971022,1971181,1971240,1971330,1971350,1971600,1971695,1971820,1971839,1971978,1972227,1972375,1973037,1973079,1973412,1973563,1973610,1973720,1973796,1973821,1974071,1974545,1974658,1974889,1975127,1975170,1975220,1975293,1975570,1975693,1976134,1976166,1977069,1977334,1977419,1977494,1978369,1978680,1978755,1978881,849435,882508,913634,925422,934834,976601,995570";

        String[] idArray = idString.split(",");

        for (String id : idArray) {

            try {
                productService.deleteProduct(Long.valueOf(id));
            } catch (Exception e) {
                System.out.println("delete fail for " + id);
            }
        }

        return "ok";
    }
}
