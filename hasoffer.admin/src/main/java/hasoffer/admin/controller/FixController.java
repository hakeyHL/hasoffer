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
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuIndex2Updater;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.product.*;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

        Date date = TimeUtils.stringToDate("2016-03-23 19:00:00", "yyyy-MM-dd HH:mm:ss");

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
                    }

                    date = product.getCreateTime();

                    logger.debug(product.getId() + " - " + TimeUtils.parse(date, "yyyy-MM-dd HH:mm:ss"));
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
}
