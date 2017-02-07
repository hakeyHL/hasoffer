package hasoffer.task.worker;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.mongo.PriceNode;
import hasoffer.core.persistence.mongo.PtmCmpSkuHistoryPrice;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuDubboUpdate2Worker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdate2Worker.class);
    private IFetchDubboService fetchDubboService;
    private ICmpSkuService cmpSkuService;
    private IRedisListService redisListService;
    private IMongoDbManager mdm;
    private IDataBaseManager dbm;
    private IDealService dealService;

    public CmpSkuDubboUpdate2Worker(IFetchDubboService fetchDubboService, ICmpSkuService cmpSkuService, IRedisListService redisListService, IMongoDbManager mdm, IDataBaseManager dbm, IDealService dealService) {
        this.fetchDubboService = fetchDubboService;
        this.cmpSkuService = cmpSkuService;
        this.redisListService = redisListService;
        this.mdm = mdm;
        this.dbm = dbm;
        this.dealService = dealService;
    }

    @Override
    public void run() {

        while (true) {

            try {


                String fetchUrlResultStr = fetchDubboService.popFetchUrlResult(TaskTarget.SKU_UPDATE);
                if (fetchUrlResultStr == null) {
                    logger.info("fetchUrlResult get null sleep 10 MINUTES");
                    TimeUnit.MINUTES.sleep(10);
                    continue;
                }
                FetchUrlResult fetchUrlResult = JSONUtil.toObject(fetchUrlResultStr, FetchUrlResult.class);

                if (fetchUrlResult.getUrl() == null) {
                    logger.info("fetchUrlResult.getUrl() null");
                    continue;
                }

                logger.info("pop get response success " + fetchUrlResult.getWebsite());
                String url = fetchUrlResult.getUrl();
                Website website = fetchUrlResult.getWebsite();

                TaskStatus taskStatus = fetchUrlResult.getTaskStatus();

                if (TaskStatus.FINISH.equals(taskStatus)) {

                    logger.info("taskStatus is finish " + website);

                    String urlKey = HexDigestUtil.md5(url);
                    List<PtmCmpSku> skuList = cmpSkuService.getPtmCmpSkuListByUrlKey(urlKey);

                    if (skuList == null || skuList.size() == 0) {
                        logger.info("urkKey not found " + website + "_ url = " + url);
                    } else {
                        logger.info("urkKey found " + website + " skulist begin to update " + skuList.size());
                        for (PtmCmpSku ptmCmpSku : skuList) {
                            //更新商品的信息，写入多图数据，写入描述/参数
                            updatePtmCmpSku(ptmCmpSku, fetchUrlResult);
                            logger.info("update success for " + ptmCmpSku.getWebsite());
                        }
                    }
                } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
                    logger.info("taskStatus is exception " + website + "_ url = " + url);
                } else {
                    logger.info("taskStatus is " + taskStatus + "_" + website + "_ url = " + url);
                }
            } catch (Exception e) {
                logger.info("CmpSkuDubboUpdate2Worker.run() exception.", e);
                e.printStackTrace();
            }
        }
    }

    private void updatePtmCmpSku(PtmCmpSku sku, FetchUrlResult fetchUrlResult) {
        // try update sku
        Long skuid = sku.getId();
        String url = sku.getUrl();
        float skuPrice = sku.getPrice();
        float skuOriPrice = sku.getOriPrice();

        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            logger.info("website is null for _" + skuid + "_");
            return;
        }

        FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();
        logger.info(fetchedProduct.toString());

//        由于部分deal要增加新的最低价标识，所以deal的生成策略要写在更新之前
        //获取最低价
        try {
            PtmCmpSkuHistoryPrice ptmCmpSkuHistoryPrice = mdm.queryOne(PtmCmpSkuHistoryPrice.class, skuid);

            if (ptmCmpSkuHistoryPrice == null) {
                logger.info("update ptmcmpsku priceHistory get null");
            } else {
                float minPrice = getMinPrice(ptmCmpSkuHistoryPrice);
                logger.info("minPrice " + minPrice);
                logger.info("fetchedProduct.getPrice() " + fetchedProduct.getPrice());

                if (minPrice != 0.0f
                        && fetchedProduct.getPrice() <= minPrice                                    //更新后的价格小于等于更新前的历史最低价格
                        && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())                   //状态onsale
                        && fetchedProduct.getCommentsNumber() > 100                                  //评论大于40 大于100
                        && skuOriPrice != 0.0                                                       //原价不为空
                        && fetchedProduct.getPrice() < skuOriPrice                                  //现价低于原价
                        && fetchedProduct.getPrice() != 0.0                                         //现价不为0
                        && fetchedProduct.getPrice() < skuPrice) {                                  //现价比更新前的价格低

//            对现价进行判断，如果更新后的价格小于更新前的历史最低价格，且商品更新前有两个不同的历史价格（价格是0的不计入），则将创建deal的标题最前方加上【New Lowest Price】（表示新低价）
                    if (fetchedProduct.getPrice() < minPrice && ptmCmpSkuHistoryPrice.getPriceNodes().size() > 1) {
                        logger.info("create NEWLOWEST deal");
                        createDeal(skuid, "NEWLOWEST", fetchedProduct);
                    }

//            对现价进行判断，如果更新后的价格等于更新前的历史最低价格，且现价不大于150卢比，且商品有flipkart assured或 Fulfilled by Amazon的标识，则将创建的deal的标题最前方加上【Add on】
                    if (fetchedProduct.getPrice() == minPrice && fetchedProduct.getPrice() <= 150 && (Boolean) fetchedProduct.getFlagMap().get("ADDABLE")) {
                        logger.info("create ADDON deal");
                        createDeal(skuid, "ADDON", fetchedProduct);
                    }

//            对现价进行判断，如果更新后的价格等于更新前的历史最低价格，且现价高于150卢比，则将按常规方式创建deal；
                    if (fetchedProduct.getPrice() == minPrice && fetchedProduct.getPrice() > 150) {
                        logger.info("create default deal");
                        createDeal(skuid, "", fetchedProduct);
                    }
                }
            }

        } catch (Exception e) {
            logger.info("CmpSkuDubboUpdate2Worker  create deal fail " + skuid);
            e.printStackTrace();
        }


        try {
            //
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuid, fetchedProduct);
        } catch (Exception e) {
            logger.info("updateCmpSkuBySpiderFetchedProduct fail " + skuid);
            e.printStackTrace();
        }

        logger.info("updateCmpSkuBySpiderFetchedProduct success " + fetchedProduct.getWebsite() + "_" + fetchedProduct.getSkuStatus() + "_" + skuid);
    }

    private void createDeal(long skuid, String titleFlagString, FetchedProduct fetchedProduct) {

        logger.info("createDeal method " + titleFlagString);

        PtmCmpSku sku = cmpSkuService.getCmpSkuById(skuid);
        if (sku == null) {
            return;
        }

        AppDeal appdeal = new AppDeal();

        appdeal.setWebsite(sku.getWebsite());
        appdeal.setAppdealSource(AppdealSource.PRICE_OFF);
        appdeal.setCreateTime(TimeUtils.nowDate());
        //question 这种deal只有涨价才失效，加他个365天
        appdeal.setExpireTime(TimeUtils.addDay(TimeUtils.nowDate(), 365));
        appdeal.setLinkUrl(sku.getUrl());
        appdeal.setPush(false);

        if (StringUtils.isEqual(titleFlagString, "NEWLOWEST")) {
            appdeal.setTitle("【New Lowest Price】 " + sku.getTitle());
            logger.info("【New Lowest Price】 dealTitle");
        } else if (StringUtils.isEqual(titleFlagString, "ADDON")) {
            appdeal.setTitle("【Add on】 " + sku.getTitle());
            logger.info("【Add on】 dealTitle");
        } else {
            appdeal.setTitle(sku.getTitle());
            logger.info("default dealTitle");
        }

        appdeal.setPtmcmpskuid(sku.getId());
        StringBuilder sb = new StringBuilder();

        float newPrice = sku.getPrice();
        float minPrice = getMinPrice(mdm.queryOne(PtmCmpSkuHistoryPrice.class, skuid));

        if (newPrice >= minPrice) {//100%-110%
            //如果现价不低于史低价
            //文案 Rs.现价 is almost history lowest price(History lowest price is Rs.更新前的史低价). Click here to check price history（点击此行展示价格走势浮层）. Good offer always expire in hours.Good time to get it,Hurry up!
            sb.append("Rs.").append(BigDecimal.valueOf(newPrice)).append(" is almost history lowest price(History lowest price is Rs.").append(minPrice).append(").Click here to check price history.Good offer always expire in hours.Good time to get it,Hurry up!");
        } else {//小于100%
            //否则
            //Rs.现价 is the newest history lowest price(Previous lowest price is Rs.更新前的史低价).Click here to check price history（以高亮可点击文案展示点击唤出价格走势浮层 具体逻辑见price history）. Good offer always expire in hours.Good time to get it,Hurry up!
            sb.append("Rs.").append(BigDecimal.valueOf(newPrice)).append(" is newest history lowest price(Previous lowest price is Rs.").append(minPrice).append(").Click here to check price history.Good offer always expire in hours.Good time to get it,Hurry up!");
        }

        appdeal.setDescription(sb.toString());
        appdeal.setPresentPrice(newPrice);
        appdeal.setPriceDescription("Rs." + (int) newPrice);
        float oriPrice = sku.getOriPrice();
        appdeal.setOriginPrice(oriPrice);
        appdeal.setDiscount((int) ((1 - newPrice / oriPrice) * 100));
        //默认显示  2017-02-07
        appdeal.setDisplay(true);

        //url重复不创建
        List<AppDeal> appdealList = dbm.query("SELECT t FROM AppDeal t WHERE t.linkUrl = ?0", Arrays.asList(sku.getUrl()));
        if (appdealList != null && appdealList.size() != 0) {
            logger.info("query by url get " + appdealList.size() + " sku");
            return;
        }

        //当天title不能重名
        String title = sku.getTitle();
        Website website = sku.getWebsite();
        appdealList = dbm.query("SELECT t FROM AppDeal t WHERE t.title = ?0 AND t.website = ?1 ", Arrays.asList(title, website));
        if (appdealList != null && appdealList.size() != 0) {
            logger.info("query by title website get " + appdealList.size() + " sku");
            return;
        }

        //todo s3可能又内网的访问方式，不收费
        String imagePath = sku.getImagePath();
        imagePath = ImageUtil.getImageUrl(imagePath);
        String dealPath = "";
        String dealBigPath = "";
        String dealSmallPath = "";

        try {
            File imageFile = ImageUtil.downloadImage(imagePath);

            dealPath = ImageUtil.uploadImage(imageFile);
            dealBigPath = ImageUtil.uploadImage(imageFile, 316, 180);
            dealSmallPath = ImageUtil.uploadImage(imageFile, 180, 180);
        } catch (Exception e) {
            logger.info("check get priceoff deal image download error");
            return;
        }

        appdeal.setImageUrl(dealPath);
        appdeal.setInfoPageImage(dealBigPath);
        appdeal.setListPageImage(dealSmallPath);

        if (fetchedProduct.getFlagMap().size() != 0) {
            if ((Boolean) fetchedProduct.getFlagMap().get("ADDABLE") && fetchedProduct.getPrice() > 500) {
                appdeal.setShippingFee(0.0f);
            }
        }

        dealService.createAppDealByPriceOff(appdeal);
        System.out.println("create deal info id " + appdeal.getId() + "_now parice " + appdeal.getPresentPrice());
    }

    private float getMinPrice(PtmCmpSkuHistoryPrice historyPrice) {

        float minPrice = 0.0f;

        //获取历史最低价格
        List<PriceNode> priceNodes = historyPrice.getPriceNodes();

        if (priceNodes == null || priceNodes.size() <= 0) {
            return 0.0f;
        }

        for (int i = 0; i < priceNodes.size(); i++) {

            if (i == 0) {
                minPrice = priceNodes.get(i).getPrice();
            } else {
                float anotherPrice = priceNodes.get(i).getPrice();
                if (anotherPrice < minPrice) {
                    minPrice = anotherPrice;
                }
            }
        }

        return minPrice;
    }
}


//          如果降价且CommentsNumber 大于40写入队列，并且状态必须是onsale
//          新加逻辑push逻辑使用true---rightPush
//          新加逻辑pop辑使用true---rightPop
//          Date：2016-12-05 12:41
//        if (price > fetchedProduct.getPrice() && fetchedProduct.getCommentsNumber() > 40 && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
//            redisListService.push(PRICE_DROP_SKUID_QUEUE, skuid + "", true);
//            logger.info("price drop add to queue success " + skuid);
//        }


//        try {
//            cmpSkuService.createPtmCmpSkuImage(skuid, fetchedProduct);
//        } catch (Exception e) {
//            logger.info("createPtmCmpSkuImage fail " + skuid);
//        }


//        try {
//
//            PtmProduct ptmProduct = dbm.get(PtmProduct.class, sku.getProductId());
//
//            if (ptmProduct != null) {
//
//                //保存sku的描述信息
//                cmpSkuService.createSkuDescription(sku, fetchedProduct);
//
//                String productTitle = ptmProduct.getTitle();
//
//                if (StringUtils.isEqual(productTitle, sku.getTitle())) {
//                    //保存product的描述信息
//                    cmpSkuService.createProductDescription(sku, fetchedProduct);
//                    System.out.println("update product spec success for " + ptmProduct.getId());
//                } else {
//                    System.out.println("product spec should remove " + ptmProduct.getId());
//                }
//            } else {
//                System.out.println(skuid + " product is null");
//            }
//        } catch (Exception e) {
//            logger.info("createDescription fail " + skuid);
//        }

//            对FLIPKART没有类目的数据进行更新,暂时注释掉
//        if (Website.FLIPKART.equals(sku.getWebsite())) {
//
//            if (sku.getCategoryId() == null || sku.getCategoryId() == 0) {
//
//                List<String> categoryPathList = fetchedProduct.getCategoryPathList();
//
//                if (categoryPathList != null && categoryPathList.size() != 0) {
//
//                    String lastCategoryPath = categoryPathList.get(categoryPathList.size() - 1);
//
//                    PtmCategory3 ptmCategory3 = dbm.querySingle("SELECT t FROM PtmCategory3 t WHERE t.name = ?0", Arrays.asList(lastCategoryPath));
//
//                    if (ptmCategory3 != null) {
//
//                        long categoryid = ptmCategory3.getHasofferCateogryId();
//
//                        if (categoryid != 0) {
//                            cmpSkuService.updateCategoryid(skuid, categoryid);
//                            logger.info("update flipkart sku categoryid success for _" + skuid + "_  to _" + categoryid + "_");
//                        }
//
//                    }
//                }
//            }
//        }