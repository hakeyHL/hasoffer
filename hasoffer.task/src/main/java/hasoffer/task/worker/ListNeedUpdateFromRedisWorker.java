package hasoffer.task.worker;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.ApiUtils;
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

    private static final String UPDATE_WAIT_QUEUE = "API_PRODUCT_WAIT_4_UPDATE_";
    private static final String KEY_PROCESSED_SET = "PRODUCT_UPDATE_PROCESSED_";
    private static Logger logger = LoggerFactory.getLogger(ListNeedUpdateFromRedisWorker.class);
    private IFetchDubboService fetchDubboService;
    private IRedisListService redisListService;
    private IRedisSetService redisSetService;
    private IPtmStdSkuService stdSkuService;
    private ICmpSkuService cmpSkuService;
    private ProductCacheManager productCacheManager;

    public ListNeedUpdateFromRedisWorker(IFetchDubboService fetchDubboService, IRedisListService redisListService, IRedisSetService redisSetService, IPtmStdSkuService stdSkuService, ICmpSkuService cmpSkuService, ProductCacheManager productCacheManager) {
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.redisSetService = redisSetService;
        this.stdSkuService = stdSkuService;
        this.cmpSkuService = cmpSkuService;
        this.productCacheManager = productCacheManager;
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
                logger.info("current ymd = " + ymd);
                logger.info("current daystart is " + tomorrowDayStart);
            }

            try {

                Object pop = redisListService.pop(UPDATE_WAIT_QUEUE + ymd);
                if (pop == null) {//如果队列没有数据了，休息5分钟
                    try {
                        logger.info("sku update pop get null sleep 30 min " + tomorrowDayStart);
                        TimeUnit.MINUTES.sleep(5);
                    } catch (InterruptedException e) {
                        logger.info("ListNeedUpdateFromRedisWorker list pop from redis InterruptedException");
                    }
                    continue;
                }

                //if proceded set has this productId，continue next one
                logger.info("pop from wait update queue");
                if (redisSetService.contains(KEY_PROCESSED_SET + ymd, pop)) {
                    logger.info("proceded set has this productId，continue next one");
                    continue;
                }

                //根据商品id，发起更新任务
                Long productId = Long.valueOf((String) pop);
                logger.info("proceded set do not hava this productid " + productId + " get skuList");

                if (productId > ConstantUtil.API_ONE_BILLION_NUMBER) {//ptmStdPrice
                    sendPtmStdPriceUrlUpdateReqest(productId);
                } else {
                    sendPtmCmpSkuUrlUpdateReqest(productId);
                }

                //now productid hava been sended ,add to processed set
                productCacheManager.put2UpdateProcessedSet(productId, ymd);
            } catch (Exception e) {

            }
        }
    }


    void sendPtmCmpSkuUrlUpdateReqest(long productId) {

        List<PtmCmpSku> ptmCmpSkuList = cmpSkuService.listCmpSkus(productId);

        //在加入队列的时候进行一些必要的判断
        if (ptmCmpSkuList != null && ptmCmpSkuList.size() > 0) {

            for (PtmCmpSku sku : ptmCmpSkuList) {

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

                    fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskTarget.SKU_UPDATE, TaskLevel.LEVEL_3);
                    logger.info("send ptmcmpsku url request succes for " + sku.getWebsite() + " sku id is _" + sku.getId() + "_");
                } else {
                    fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskTarget.SKU_UPDATE, TaskLevel.LEVEL_5);
                    logger.info("send ptmcmpsku url request succes for " + sku.getWebsite() + " sku id is _" + sku.getId() + "_");
                }
            }
        }
    }

    void sendPtmStdPriceUrlUpdateReqest(long productId) {

        List<PtmStdPrice> stdPriceList = stdSkuService.listStdPrice(ApiUtils.removeBillion(productId));

        if (stdPriceList != null && stdPriceList.size() > 0) {

            logger.info("get stdPriceList size is " + stdPriceList.size());

            for (PtmStdPrice stdPrice : stdPriceList) {

                //offsale的不再更新
                if (SkuStatus.OFFSALE.equals(stdPrice.getSkuStatus())) {
                    continue;
                }

                Website website = stdPrice.getWebsite();

                //暂时过滤掉myntra
                if (Website.MYNTRA.equals(website)) {
                    continue;
                }

                //高优先级的网站
                if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {

                    //过滤掉snapdeal中viewAllSeller的情况
                    if (Website.SNAPDEAL.equals(website)) {
                        String url = stdPrice.getUrl();
                        url = StringUtils.filterAndTrim(url, Arrays.asList("/viewAllSellers"));
                        stdPrice.setUrl(url);
                    }
                    //过滤掉amazon中gp/offer-listing的url,该url没有描述等信息
                    if (Website.AMAZON.equals(website)) {
                        String url = stdPrice.getUrl();
                        url = url.replace("gp/offer-listing", "dp");
                        stdPrice.setUrl(url);
                    }

                    fetchDubboService.sendUrlTask(stdPrice.getWebsite(), stdPrice.getUrl(), TaskTarget.STDPRICE_UPDATE, TaskLevel.LEVEL_3);
                    logger.info("send ptmstdprice url request succes for " + stdPrice.getWebsite() + " stdprice id is _" + stdPrice.getId() + "_");
                } else {
                    fetchDubboService.sendUrlTask(stdPrice.getWebsite(), stdPrice.getUrl(), TaskTarget.STDPRICE_UPDATE, TaskLevel.LEVEL_5);
                    logger.info("send ptmstdprice url request succes for " + stdPrice.getWebsite() + " stdprice id is _" + stdPrice.getId() + "_");
                }
            }

        } else {
            logger.info("get empty stdPriceList for productid " + productId);
        }

    }
}
