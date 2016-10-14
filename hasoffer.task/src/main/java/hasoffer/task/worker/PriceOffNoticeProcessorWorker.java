package hasoffer.task.worker;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class PriceOffNoticeProcessorWorker implements Runnable {

    private static final String PRICEOFF_NOTICE_SKUID_QUEUE = "PRICEOFF_NOTICE_SKUID_QUEUE";
    public static Integer PRICEOFFNOTICE_PRICESSOR_WORKER_THREADNUMBER = 0;
    private static Logger logger = LoggerFactory.getLogger(PriceOffNoticeProcessorWorker.class);
    private ConcurrentLinkedQueue<Long> queue;
    private IFetchDubboService fetchDubboService;
    private IRedisListService redisListService;
    private ICmpSkuService cmpSkuService;
    private IDataBaseManager dbm;

    public PriceOffNoticeProcessorWorker(ConcurrentLinkedQueue<Long> queue, IFetchDubboService fetchDubboService, IRedisListService redisListService, ICmpSkuService cmpSkuService, IDataBaseManager dbm) {
        this.queue = queue;
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.cmpSkuService = cmpSkuService;
        this.dbm = dbm;
        PRICEOFFNOTICE_PRICESSOR_WORKER_THREADNUMBER++;
    }

    @Override
    public void run() {

        long startTime = TimeUtils.now();

        while (true) {

            Long skuid = queue.poll();

            try {

                if (TimeUtils.now() - startTime > TimeUtils.MILLISECONDS_OF_1_HOUR * 1) {
                    PRICEOFFNOTICE_PRICESSOR_WORKER_THREADNUMBER--;
                    System.out.println("price off notice processor worker thread has live above 1 hours ,thread going to die ");
                    System.out.println("alive thread number " + PRICEOFFNOTICE_PRICESSOR_WORKER_THREADNUMBER);
                    break;
                }

                if (skuid == null) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        logger.info("task update get null sleep 3 seconds");
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                PtmCmpSku sku = dbm.get(PtmCmpSku.class, skuid);

                Date updateTime = sku.getUpdateTime();
                if (updateTime != null) {
                    if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                        continue;
                    }
                }

                updatePtmCmpSku(sku);

            } catch (Exception e) {
                System.out.println(TimeUtils.nowDate());
                e.printStackTrace();
            }
        }

        System.out.println("queue size is " + queue.size());
    }

    private void updatePtmCmpSku(PtmCmpSku sku) {
        // try update sku
        Long skuid = sku.getId();
        String url = sku.getUrl();
        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            return;
        }

        TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(website, url, TimeUtils.SECONDS_OF_1_MINUTE * 35);

        FetchUrlResult fetchUrlResult = null;

        //如果返回结果状态为running，那么将sku返回队列
        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
            queue.add(skuid);
//            logger.info("taskstatus RUNNING for [" + skuid + "]");
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.info("taskstatus STOPPED for [" + skuid + "]");
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.info("taskstatus EXCEPTION for [" + skuid + "]");
        } else if (TaskStatus.NONE.equals(taskStatus)) {
            queue.add(skuid);
            if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {
                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskLevel.LEVEL_2);
            } else {
                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskLevel.LEVEL_5);
            }
            logger.info("taskstatus NONE for [" + skuid + "] , resend success");
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + skuid + "]");
            fetchUrlResult = fetchDubboService.getProductsByUrl(sku.getWebsite(), sku.getUrl(), TimeUtils.SECONDS_OF_1_MINUTE * 35);

            FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

            System.out.println(JSONUtil.toJSON(fetchedProduct).toString() + "id=" + skuid);

            try {

                PtmProduct ptmProduct = dbm.get(PtmProduct.class, sku.getProductId());

                if (ptmProduct != null) {

                    //保存sku的描述信息
                    cmpSkuService.createSkuDescription(sku, fetchedProduct);

                    String productTitle = ptmProduct.getTitle();

                    if (StringUtils.isEqual(productTitle, sku.getTitle())) {
                        //保存product的描述信息
                        cmpSkuService.createProductDescription(sku, fetchedProduct);
                        System.out.println("update product spec success for " + ptmProduct.getId());
                    } else {
                        System.out.println("product spec should remove " + ptmProduct.getId());
                    }
                } else {
                    System.out.println(skuid + " product is null");
                }
            } catch (Exception e) {
                System.out.println("createDescription fail " + skuid);
            }

            try {
                cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuid, fetchedProduct);
            } catch (Exception e) {
                logger.info("updateCmpSkuBySpiderFetchedProduct fail " + skuid);
                e.printStackTrace();
            }

            try {
                cmpSkuService.createPtmCmpSkuImage(skuid, fetchedProduct);
            } catch (Exception e) {
                logger.info("createPtmCmpSkuImage fail " + skuid);
            }

//            如果价格发生变化，加到redis队列中
            if (sku.getPrice() != fetchedProduct.getPrice()) {

                redisListService.push(PRICEOFF_NOTICE_SKUID_QUEUE, skuid + "");

                logger.info("push success for " + skuid);
            }
        }
    }
}
