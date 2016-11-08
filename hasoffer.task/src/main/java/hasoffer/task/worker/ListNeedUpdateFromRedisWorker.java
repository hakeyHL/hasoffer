package hasoffer.task.worker;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.IRedisSetService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.enums.TaskTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/1.
 */
public class ListNeedUpdateFromRedisWorker implements Runnable {

    private static final String UPDATE_WAIT_QUEUE = "PRODUCT_WAIT_4_UPDATE_";
    private static final String KEY_PROCESSED_SET = "PRODUCT_UPDATE_PROCESSED_";

    private static Logger logger = LoggerFactory.getLogger(ListNeedUpdateFromRedisWorker.class);
    private IFetchDubboService fetchDubboService;
    private IRedisListService redisListService;
    private IRedisSetService redisSetService;
    private ICmpSkuService cmpSkuService;
    private ProductCacheManager productCacheManager;
    private long cacheSeconds;
    private long number;

    private long testPopProductNumber = 0;
    private long testProcedProductNumber = 0;
    private long testTotalPtmCmpSkuNumber = 0;
    private long testSendPtmCmpSkuNumber = 0;
    private long testSendFlipkartNumber = 0;

    public ListNeedUpdateFromRedisWorker(IFetchDubboService fetchDubboService, IRedisListService redisListService, IRedisSetService redisSetService, ICmpSkuService cmpSkuService, long cacheSeconds, ProductCacheManager productCacheManager) {
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.redisSetService = redisSetService;
        this.cmpSkuService = cmpSkuService;
        this.cacheSeconds = cacheSeconds;
        this.productCacheManager = productCacheManager;
    }

    public ListNeedUpdateFromRedisWorker(IFetchDubboService fetchDubboService, IRedisListService redisListService, IRedisSetService redisSetService, ICmpSkuService cmpSkuService, long cacheSeconds, ProductCacheManager productCacheManager, long number) {
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.redisSetService = redisSetService;
        this.cmpSkuService = cmpSkuService;
        this.cacheSeconds = cacheSeconds;
        this.productCacheManager = productCacheManager;
        this.number = number;
    }

    @Override
    public void run() {

        String ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);
        long tomorrowDayStart = TimeUtils.getDayStart(TimeUtils.addDay(TimeUtils.nowDate(), 1).getTime());

        while (true) {

            //保证时间正常
            if (tomorrowDayStart < TimeUtils.now()) {
                ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);
                tomorrowDayStart = TimeUtils.getDayStart(TimeUtils.addDay(TimeUtils.nowDate(), 1).getTime());
            }

            System.out.println("current ymd = " + ymd);
            System.out.println("current daystart is " + tomorrowDayStart);

            if (testSendFlipkartNumber > number) {
                System.out.println("testPopProductNumber " + testPopProductNumber);
                System.out.println("testProcedProductNumber " + testProcedProductNumber);
                System.out.println("testTotalPtmCmpSkuNumber " + testTotalPtmCmpSkuNumber);
                System.out.println("testSendPtmCmpSkuNumber " + testSendPtmCmpSkuNumber);
                System.out.println("testSendFlipkartNumber " + testSendFlipkartNumber);
                break;
            }


            //队列取数
            int num = 1200;
            while (num > 0) {
                num--;

                Object pop = redisListService.pop(UPDATE_WAIT_QUEUE + ymd);
                if (pop == null) {//如果队列没有数据了，休息30分钟
                    try {
                        System.out.println("sku update pop get null sleep 30 min " + tomorrowDayStart);
                        TimeUnit.MINUTES.sleep(30);
                    } catch (InterruptedException e) {

                    }
                    continue;
                }

                testPopProductNumber++;

                //if proceded set has this productId，continue next one
                if (redisSetService.contains(KEY_PROCESSED_SET, (String) pop)) {
                    continue;
                }

                //根据商品id，发起更新任务
                Long productId = Long.valueOf((String) pop);

                List<PtmCmpSku> ptmCmpSkuList = cmpSkuService.listCmpSkus(productId);
                testProcedProductNumber++;

                //在加入队列的时候进行一些必要的判断
                if (ptmCmpSkuList != null && ptmCmpSkuList.size() > 0) {

                    for (PtmCmpSku sku : ptmCmpSkuList) {

                        testTotalPtmCmpSkuNumber++;

                        //offsale的不再更新
                        if (SkuStatus.OFFSALE.equals(sku.getStatus())) {
                            continue;
                        }

                        Website website = sku.getWebsite();

                        //暂时过滤掉myntra
                        if (Website.MYNTRA.equals(website)) {
                            continue;
                        }

                        //高优先级的网站
                        if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {

                            //过滤掉snapdeal中viewAllSeller的情况
                            if (Website.SNAPDEAL.equals(website)) {
                                String url = sku.getUrl();
                                url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
                                sku.setUrl(url);
                            }
                            //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
                            if (Website.AMAZON.equals(website)) {
                                String url = sku.getUrl();
                                url = url.replace("gp/offer-listing", "dp");
                                sku.setUrl(url);
                            }

                            if (Website.FLIPKART.equals(website)) {
                                testSendFlipkartNumber++;
                            }

                            fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), cacheSeconds, TaskTarget.SKU_UPDATE, TaskLevel.LEVEL_3);
                            testSendPtmCmpSkuNumber++;
                        } else {
                            fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), cacheSeconds, TaskTarget.SKU_UPDATE, TaskLevel.LEVEL_5);
                            testSendPtmCmpSkuNumber++;
                        }

                        logger.info("send url request succes for " + sku.getWebsite() + " sku id is _" + sku.getId() + "_");
                    }

                    //now productid hava been sended ,add to processed set
                    productCacheManager.put2UpdateProcessedSet(productId);
//                    redisSetService.add(KEY_PROCESSED_SET, );
                }
            }
        }
    }
}
