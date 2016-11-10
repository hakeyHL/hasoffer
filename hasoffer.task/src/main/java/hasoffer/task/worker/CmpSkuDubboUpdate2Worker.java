package hasoffer.task.worker;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuDubboUpdate2Worker implements Runnable {

    private static final String PRICE_DROP_SKUID_QUEUE = "PRICE_DROP_SKUID_QUEUE";
    //    public static long popNumber = 0;
    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdate2Worker.class);
    private IFetchDubboService fetchDubboService;
    private ICmpSkuService cmpSkuService;
    private IRedisListService redisListService;

//    private long popFinishNumber = 0;
//    private long popExceptionNumber = 0;
//    private long urlKeyFoundNumber = 0;
//    private long urlKeyNotFoundNumber = 0;
//    private long testFlipkartNumber = 0;
//    private long testSnapdealNumber = 0;

    public CmpSkuDubboUpdate2Worker(IFetchDubboService fetchDubboService, ICmpSkuService cmpSkuService, IRedisListService redisListService) {
        this.fetchDubboService = fetchDubboService;
        this.cmpSkuService = cmpSkuService;
        this.redisListService = redisListService;
    }

    @Override
    public void run() {

        while (true) {

            try {
//                if (CmpSkuDubboUpdate2Worker.popNumber < 0) {
//                    System.out.println("popNumber " + popNumber);
//                    System.out.println("popFinishNumber " + popFinishNumber);
//                    System.out.println("popExceptionNumber " + popExceptionNumber);
//                    System.out.println("urlKeyFoundNumber " + urlKeyFoundNumber);
//                    System.out.println("urlKeyNotFoundNumber " + urlKeyNotFoundNumber);
//                    System.out.println("testFlipkartNumber " + testFlipkartNumber);
//                    System.out.println("testSnapdealNumber " + testSnapdealNumber);
//                    break;
//                } else {
//                    System.out.println("popNumber " + popNumber);
//                    System.out.println("popFinishNumber " + popFinishNumber);
//                    System.out.println("popExceptionNumber " + popExceptionNumber);
//                    System.out.println("urlKeyFoundNumber " + urlKeyFoundNumber);
//                    System.out.println("urlKeyNotFoundNumber " + urlKeyNotFoundNumber);
//                    System.out.println("testFlipkartNumber " + testFlipkartNumber);
//                    System.out.println("testSnapdealNumber " + testSnapdealNumber);
//                }

                String fetchUrlResultStr = fetchDubboService.popFetchUrlResult(TaskTarget.SKU_UPDATE);
                if (fetchUrlResultStr == null) {
//                    TimeUnit.MINUTES.sleep(3);
                    TimeUnit.SECONDS.sleep(10);
                    logger.info("fetchUrlResult get null sleep 10 MINUTES");
                    continue;
                }
                FetchUrlResult fetchUrlResult = JSONUtil.toObject(fetchUrlResultStr, FetchUrlResult.class);
//                popNumber--;
                if (fetchUrlResult.getUrl() == null) {
                    logger.info("fetchUrlResult.getUrl() null");
                    continue;
                }

                String url = fetchUrlResult.getUrl();

                TaskStatus taskStatus = fetchUrlResult.getTaskStatus();

                if (TaskStatus.FINISH.equals(taskStatus)) {
//                    popFinishNumber++;

                    if (Website.FLIPKART.equals(fetchUrlResult.getWebsite())) {
                        logger.info("pop get flipkart finish result");
                    }

                    String urlKey = HexDigestUtil.md5(url);
                    List<PtmCmpSku> skuList = cmpSkuService.getPtmCmpSkuListByUrlKey(urlKey);

//                    if (Website.FLIPKART.equals(fetchUrlResult.getWebsite())) {
//                        testFlipkartNumber++;
//                    }
//                    if (Website.SNAPDEAL.equals(fetchUrlResult.getWebsite())) {
//                        testSnapdealNumber++;
//                    }

                    if (skuList == null || skuList.size() == 0) {
//                        urlKeyNotFoundNumber++;
                        logger.info("urkKey not found url = " + url);
                    } else {
//                        urlKeyFoundNumber++;
                        for (PtmCmpSku ptmCmpSku : skuList) {
                            //更新商品的信息，写入多图数据，写入描述/参数
                            updatePtmCmpSku(ptmCmpSku, fetchUrlResult);
                            logger.info("update success for " + ptmCmpSku.getWebsite());
                        }
                    }
                } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
                    if (Website.FLIPKART.equals(fetchUrlResult.getWebsite())) {
                        logger.info("pop get flipkart exception result");
                    }
                }
            } catch (Exception e) {
                logger.info("CmpSkuDubboUpdate2Worker.run() exception.", e);
            }
        }
    }

    private void updatePtmCmpSku(PtmCmpSku sku, FetchUrlResult fetchUrlResult) {
        // try update sku
        Long skuid = sku.getId();
        String url = sku.getUrl();
        float price = sku.getPrice();

        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            logger.info("website is null for _" + skuid + "_");
            return;
        }

        FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

        try {
            //
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuid, fetchedProduct);
        } catch (Exception e) {
            logger.info("updateCmpSkuBySpiderFetchedProduct fail " + skuid);
            e.printStackTrace();
        }

//        try {
//            cmpSkuService.createPtmCmpSkuImage(skuid, fetchedProduct);
//        } catch (Exception e) {
//            logger.info("createPtmCmpSkuImage fail " + skuid);
//        }

//            如果降价且CommentsNumber 大于40写入队列，并且状态必须是onsale
        if (price > fetchedProduct.getPrice() && fetchedProduct.getCommentsNumber() > 40 && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
            redisListService.push(PRICE_DROP_SKUID_QUEUE, skuid + "");
            System.out.println("price drop add to queue success " + skuid);
        }

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
    }
}
