package hasoffer.admin.controller;

import hasoffer.admin.worker.*;
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
import hasoffer.core.persistence.po.ptm.updater.PtmCategoryUpdater;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuIndex2Updater;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
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
import jodd.io.FileUtil;
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
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

        List<Object[]> titleCountMaps = dbm.query(Q_TITLE_COUNT);

        for (Object[] m : titleCountMaps) {
            String title = (String) m[0];
            System.out.println(m[1] + "\t:\t" + title);

            mergeProducts(title);

            if (!"all".equals(counts)) {
                break;
            }
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

    @RequestMapping(value = "/fixImage", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixImage() {
        final String Q_PRODUCT_WEBSITE =
                "SELECT t FROM PtmProduct t WHERE t.sourceSite='FLIPKART' and t.id > 1740208";

        ListAndProcessTask2<PtmProduct> listAndProcessTask2 = new ListAndProcessTask2<PtmProduct>(
                new IList<PtmProduct>() {
                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_PRODUCT_WEBSITE, page, 500);
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
                            String oriImageUrl = fetchService.fetchFlipkartImageUrl(sourceUrl);

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

    //fixdata/fixProductCategory
    @RequestMapping(value = "/fixProductCategory", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixProductCategory() {

        ConcurrentLinkedQueue<PtmCmpSku> quene = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new FixPtmProductCategoryListWorker(dbm, quene));

        for (int i = 0; i < 10; i++) {
            es.execute(new FixPtmProductCategoryUpdateWorker(quene, dbm, productService));
        }

        return "ok";
    }

    /*
        该方法用来合并类目太多的情况
     */
    //fixdata/fixPtmcategory1
    @RequestMapping(value = "/fixPtmcategory1", method = RequestMethod.GET)
    @ResponseBody
    public String fixPtmcategory1() {

        long[] idArray = {4, 56, 4561, 4568};

        for (long id : idArray) {

            //get all child category
            List<PtmCategory> ptmcategoryList = dbm.query(Q_PTMCATEGORY_BYPARENTID, Arrays.asList(id));

            //merge other category to first category
            PtmCategory firstChildCategory = ptmcategoryList.get(0);

            for (int i = 1; i < ptmcategoryList.size(); i++) {

                PtmCategory needUpdateCategory = ptmcategoryList.get(i);

                List<PtmCmpSku> skus = dbm.query(Q_PTMCMPSKU_BYCATEGORYID, Arrays.asList(needUpdateCategory.getId()));

                System.out.println("need update " + skus.size());
                for (PtmCmpSku sku : skus) {

                    PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

                    updater.getPo().setCategoryId(firstChildCategory.getId());

                    dbm.update(updater);

                    System.out.println("update success for [" + sku.getId() + "] from [" + needUpdateCategory.getId() + "] to [" + firstChildCategory.getId() + "]");
                }

                categoryservice.tempDeleteCategoryForCategoryUpdate(needUpdateCategory.getId());
                System.out.println("category delete [" + needUpdateCategory.getId() + "]");
            }
        }
        return "ok";
    }

    /**
     * 类目优化
     * 用来合并三级类目,只适用于关键字只出现在同一个父级目录下
     */
    //fixdata/mergeAndRenameCategroy
    @RequestMapping(value = "/mergeAndRenameCategroy", method = RequestMethod.GET)
    @ResponseBody
    public String mergeAndRenameCategroy() throws IOException {

        File file = new File("C:/Users/wing/Desktop/OptimizeCategory.sql");

//        String[] categoryKeywordArray = {
//                "Landline Phones",
//                "Mobile Accessories",
//                "Tablet Accessories",
//                "Hair Care",
//                "Home Audio",
//                "Gaming Consoles",
//                "Hair Accessories",
//                "Learning & Educational Toys",
//                "Tricycles",
//                "Dolls & Doll Houses",
//                "Puppets",
//                "Home Appliances",
//                "Coffee Mugs",
////                "Bags",
//                "Kids__Clothing",
//                "Home Automation & Safety",
////                "Hardware",
////                "Tools",
////                "musical instruments",
//                "Printers & Inks"
//        };


//        for (String categoryKeyword : categoryKeywordArray) {

        String categoryKeyword = "Pencil Boxes";

        FileUtil.appendString(file, "-- 合并" + categoryKeyword + "\r\n");

        String Q_CATEGORY_BYKEYWORD = "SELECT t FROM PtmCategory t WHERE t.name LIKE '%" + categoryKeyword + "%'";

        List<PtmCategory> categoryList = dbm.query(Q_CATEGORY_BYKEYWORD);

        boolean flag = true;
        long tempCategoryId = 0;

        for (PtmCategory ptmCategory : categoryList) {

            if (ptmCategory.getLevel() != 3) {
                continue;
            }

            //对其下面的第一个子类目进行名称修改
            if (flag) {
                categoryservice.updateCategoryName(ptmCategory.getId(), "Brand " + categoryKeyword);
                tempCategoryId = ptmCategory.getId();
                flag = false;
                continue;
            }

            //拼接俩条sql，一条update，一条delete
            //UPDATE ptmcmpsku SET categoryid = 157 WHERE categoryid = 158;
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("UPDATE PtmCmpSku SET categoryid = " + tempCategoryId + " WHERE categoryid = " + ptmCategory.getId() + ";\r\n");
            stringBuilder.append("DELETE FROM PtmCategory WHERE id = " + ptmCategory.getId() + ";\r\n");

            FileUtil.appendString(file, stringBuilder.toString());
        }

//        }

        return "ok";
    }

    //fixdata/fixShitCategory
    //对类目数据进行二次维护,将4级，5级子类目关联的商品，关联到3级类目上
    @RequestMapping(value = "/fixShitCategory")
    @ResponseBody
    public String fixShitCategory() {

        //找到level为3的类目id
        List<PtmCategory> categoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.level = 3 ");


        for (int i = 0; i < categoryList.size(); i++) {

            PtmCategory ptmCategory = categoryList.get(i);

            //找到3级类目对应的子类目
            List<PtmCategory> childCategoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.parentId = ?0 ", Arrays.asList(ptmCategory.getId()));

            for (PtmCategory childCategory : childCategoryList) {
                //找到3级对应的4级子类目对应的sku
                List<PtmCmpSku> skus = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.categoryId = ?0 ", Arrays.asList(childCategory.getId()));

                System.out.println("update " + ptmCategory.getName());
                System.out.println("3 child category " + childCategory.getName());

                for (PtmCmpSku sku : skus) {

                    //更新sku的categoryId
                    cmpSkuService.updateCategoryid(sku.getId(), ptmCategory.getId());
                    PtmProduct product = productService.getProduct(sku.getProductId());
                    if (product == null) {
                        continue;
                    }
                    //更新对应product的categoryId
                    productService.updateProductCategory(product, ptmCategory.getId());

                    System.out.println("skus " + sku.getId());
                    System.out.println("product" + product.getId());

                }

                //找到4级对应的5级子类目对应的sku
                List<PtmCategory> grandChildCategoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.parentId = ?0 ", Arrays.asList(childCategory.getId()));

                for (PtmCategory grandChildPtmCategory : grandChildCategoryList) {

                    List<PtmCmpSku> grandChildSkus = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.categoryId = ?0 ", Arrays.asList(grandChildPtmCategory.getId()));

                    System.out.println("childCategory " + childCategory.getName());
                    System.out.println("4 child category " + grandChildPtmCategory.getName());

                    for (PtmCmpSku sku : grandChildSkus) {

                        //更新sku的categoryId
                        cmpSkuService.updateCategoryid(sku.getId(), ptmCategory.getId());
                        PtmProduct product = productService.getProduct(sku.getProductId());
                        if (product == null) {
                            continue;
                        }
                        //更新对应product的categoryId
                        productService.updateProductCategory(product, ptmCategory.getId());

                        System.out.println("skus " + sku.getId());
                        System.out.println("product " + product.getId());
                    }
                }
            }
            System.out.println(i + "---" + categoryList.size());
        }

        return "ok";
    }

    /**
     * 该类用于
     * 类目数据正确也能匹配到商品，但是由于类目的level变化，导致查询不返回，解决办法，将商品重新导入一遍
     *
     * @return
     */
    //fixdata/fixcategorychange
    @RequestMapping(value = "/fixcategorychange")
    @ResponseBody
    public String fixcategorychange() {

        int[] array = {
                5,
                57,
                157,
                4689,
                4848,
                4870,
                5149,
                5199,
                5271,
                5353,
                5379,
                6068,
                6498,
                7505,
                7580,
                7810,
                8146,
                12394,
                12534,
                14312,
                20529,
                21908,
                22469,
                23790,
                63271,
                67247,
                67880,
                68096,
                68152,
                68302,
                68909,
                68949,
                69130,
                70439,
                70453,
                70676,
                71006,
                71028,
                71973,
                72377,
                72457,
                75358,
                75552,
                75801,
                76008,
                77858,
                78370,
                80407,
                83768,
                83893,
                83907,
                85423,
                86071,
                86128,
                86647,
                87497,
                89174,
                92727,
                94238,
                95884,
                5248,
                6353,
                6694,
                6907,
                7465,
                7672,
                8541,
                8652,
                9005,
                9022,
                11875,
                12656,
                13984,
                14714,
                14954,
                16247,
                16305,
                16686,
                17775,
                19410,
                19893,
                20388,
                21080,
                21338,
                21756,
                21788,
                22079,
                22626,
                22882,
                23943,
                23944,
                24049,
                24172,
                24660,
                25029,
                25256,
                25355,
                28054,
                29460,
                30601,
                30718,
                30795,
                31633,
                31693,
                32001,
                32002,
                32205,
                32428,
                32642,
                33058,
                33059,
                33060,
                33061,
                33062,
                33064,
                33065,
                33066,
                33067,
                33069,
                33071,
                33347,
                33568,
                34079,
                34411,
                34860,
                34987,
                35206,
                35563,
                36633,
                37066,
                38262,
                39095,
                40458,
                40841,
                41374,
                42743,
                42912,
                43054,
                43854,
                45481,
                47095,
                47434,
                47439,
                48995,
                49632,
                50480,
                51392,
                52042,
                52044,
                53997,
                54992,
                55206,
                55207,
                58020,
                58354,
                59753,
                59756,
                61933,
                64378,
                68160,
                68669,
                68693,
                69392,
                69655,
                69656,
                70059,
                70148,
                70301,
                70395,
                70705,
                70737,
                70760,
                70761,
                71740,
                71870,
                71986,
                72156,
                72475,
                72902,
                73473,
                74036,
                74331,
                74432,
                74502,
                74943,
                74980,
                75229,
                75403,
                75489,
                75580,
                76246,
                76621,
                77118,
                77164,
                77203,
                77310,
                77323,
                77878,
                78021,
                78310,
                78326,
                78385,
                78417,
                78743,
                79118,
                79516,
                79620,
                79636,
                80035,
                80123,
                80182,
                80381,
                80583,
                81030,
                81230,
                81351,
                81382,
                81886,
                81887,
                82104,
                82176,
                82277,
                82619,
                82762,
                83174,
                83233,
                83492,
                83536,
                83835,
                83856,
                84148,
                84284,
                84285,
                84369,
                84795,
                84801,
                84914,
                84939,
                84977,
                85038,
                85041,
                85053,
                85421,
                85800,
                85805,
                85827,
                86086,
                86156,
                86196,
                86200,
                86239,
                86328,
                86451,
                86610,
                86740,
                86911,
                86926,
                87192,
                87341,
                87394,
                87397,
                87502,
                88114,
                88229,
                88245,
                88278,
                88476,
                88674,
                88851,
                89021,
                89384,
                89390,
                89508,
                89511,
                89620,
                89644,
                89646,
                89660,
                89943,
                90216,
                90238,
                90261,
                90408,
                90477,
                90775,
                90926,
                90987,
                91383,
                91660,
                91830,
                92050,
                92104,
                92128,
                92388,
                92518,
                92832,
                92973,
                93156,
                93241,
                93620,
                93682,
                93771,
                93841,
                93973,
                94189,
                94483,
                94730,
                94794,
                95064,
                95210,
                95847,
                95963,
                95995,
                96387,
                96401,
                96608,
                96609,
                96891,
                97481,
                97482,
                97494,
                97712,
                98266,
                98758,
                98859,
                98877,
                98961,
                99111,
                99172,
                99203,
                99328,
                99500,
                99933,
                100030,
                100052,
                100325,
                100346,
                100347,
                100519,
                100547,
                100805,
                100870,
                100932,
                100942,
                101130,
                101480,
                101813,
                102097,
                102128,
                102219,
                102249,
                102534,
                102580,
                102646,
                102706,
        };

        final ConcurrentLinkedQueue<Long> logQueue = new ConcurrentLinkedQueue<Long>();

        for (int i = 0; i < array.length; i++) {
            logQueue.add(Long.valueOf(array[i]));
        }

        ExecutorService es = Executors.newCachedThreadPool();

        for (int i = 0; i < 5; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        Long ptmcategoryId = logQueue.poll();

                        List<PtmCmpSku> skus = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.categoryId = ?0 ", Arrays.asList(ptmcategoryId));

                        for (PtmCmpSku sku : skus) {
                            //更新sku的categoryId
//                cmpSkuService.updateCategoryid(sku.getId(), ptmcategoryId);
                            PtmProduct product = productService.getProduct(sku.getProductId());
                            if (product == null) {
                                continue;
                            }
                            //更新对应product的categoryId
                            productService.updateProductCategory(product, ptmcategoryId);

                            System.out.println("skus " + sku.getId());
                            System.out.println("product" + product.getId());
                        }

                        //todo 更新完一个后需要将类目释放出来

                        PtmCategory category = dbm.querySingle("SELECT t FROM PtmCategory t WHERE t.id = ?0 ", Arrays.asList(ptmcategoryId));

                        PtmCategoryUpdater updater = new PtmCategoryUpdater(ptmcategoryId);

                        if (category.getLevel() == 20) {
                            updater.getPo().setLevel(2);
                        } else if (category.getLevel() == 30) {
                            updater.getPo().setLevel(3);
                        } else {
                            System.out.println("level error for" + ptmcategoryId);
                        }

                        dbm.update(updater);
                    }
                }
            });

        }

        return "";
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

    //fixdata/fixshitcategory497L
    @RequestMapping(value = "/fixshitcategory497L")
    @ResponseBody
    public String fixshitcategory497L() {

        long descPtmcategoryId = 497L;

        long[] arrays = {517L, 558L, 13984L, 25355L, 36633L, 78417L, 98266L, 24049L};

        for (long ptmcategoryId : arrays) {

            System.out.println("start: from [" + ptmcategoryId + "] to" + descPtmcategoryId);

            List<PtmCmpSku> skus = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.categoryId = ?0 ", Arrays.asList(ptmcategoryId));

            for (PtmCmpSku sku : skus) {
//            更新sku的categoryId
                cmpSkuService.updateCategoryid(sku.getId(), descPtmcategoryId);
                PtmProduct product = productService.getProduct(sku.getProductId());
                if (product == null) {
                    continue;
                }
                //更新对应product的categoryId
                productService.updateProductCategory(product, descPtmcategoryId);

                System.out.println("skus " + sku.getId());
                System.out.println("product" + product.getId());
            }

            PtmCategory category = dbm.querySingle("SELECT t FROM PtmCategory t WHERE t.id = ?0 ", Arrays.asList(ptmcategoryId));

            PtmCategoryUpdater updater = new PtmCategoryUpdater(ptmcategoryId);

            //请这些类目从类目结构中屏蔽掉
            updater.getPo().setLevel(category.getLevel() + 10);

            dbm.update(updater);
        }

        return "";
    }


    //fixdata/fixshitcategoryCables
    @RequestMapping(value = "/fixshitcategoryCables")
    @ResponseBody
    public String fixshitcategoryCables() {

        long descPtmcategoryId = 4984L;

        long[] arrays = {5546L, 8981L, 5987L};

        for (long ptmcategoryId : arrays) {

            System.out.println("start: from [" + ptmcategoryId + "] to" + descPtmcategoryId);

            List<PtmCmpSku> skus = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.categoryId = ?0 ", Arrays.asList(ptmcategoryId));

            for (PtmCmpSku sku : skus) {
//            更新sku的categoryId
                cmpSkuService.updateCategoryid(sku.getId(), descPtmcategoryId);
                PtmProduct product = productService.getProduct(sku.getProductId());
                if (product == null) {
                    continue;
                }
                //更新对应product的categoryId
                productService.updateProductCategory(product, descPtmcategoryId);

                System.out.println("skus " + sku.getId());
                System.out.println("product" + product.getId());
            }

            PtmCategory category = dbm.querySingle("SELECT t FROM PtmCategory t WHERE t.id = ?0 ", Arrays.asList(ptmcategoryId));

            PtmCategoryUpdater updater = new PtmCategoryUpdater(ptmcategoryId);

            //请这些类目从类目结构中屏蔽掉
            updater.getPo().setLevel(category.getLevel() + 10);

            dbm.update(updater);
        }

        return "";
    }
}
