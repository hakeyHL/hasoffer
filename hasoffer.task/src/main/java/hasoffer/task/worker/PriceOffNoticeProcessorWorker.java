package hasoffer.task.worker;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.task.controller.DubboUpdateController;
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
    private static Logger logger = LoggerFactory.getLogger(PriceOffNoticeProcessorWorker.class);
    private ConcurrentLinkedQueue<PtmCmpSku> queue;
    private IFetchDubboService fetchDubboService;
    private IRedisListService redisListService;
    private ICmpSkuService cmpSkuService;

    public PriceOffNoticeProcessorWorker(ConcurrentLinkedQueue<PtmCmpSku> queue, IFetchDubboService fetchDubboService, IRedisListService redisListService, ICmpSkuService cmpSkuService) {
        this.queue = queue;
        this.fetchDubboService = fetchDubboService;
        this.redisListService = redisListService;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        while (true) {

            PtmCmpSku sku = queue.poll();

            try {

                if (sku == null) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        logger.info("task update get null sleep 3 seconds");
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (DubboUpdateController.Price_OFF_LIST_THREAD_NUM == 0 && queue.size() == 0) {
                        System.out.println("price off process queue has no object ,thread going to die");
                        break;
                    } else {
                        continue;
                    }
                }

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

            System.out.println("Price_OFF_LIST_THREAD_NUM == " + DubboUpdateController.Price_OFF_LIST_THREAD_NUM);
            System.out.println("queue size is " + queue.size());
            System.out.println("sku ex: " + sku.getId());
        }
    }

    private void updatePtmCmpSku(PtmCmpSku sku) {
        // try update sku
        Long skuid = sku.getId();
        String url = sku.getUrl();
        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            return;
        }

        TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(website, url, TimeUtils.SECONDS_OF_1_DAY);

        FetchUrlResult fetchUrlResult = null;

        //如果返回结果状态为running，那么将sku返回队列
        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
            queue.add(sku);
//            logger.info("taskstatus RUNNING for [" + skuid + "]");
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.info("taskstatus STOPPED for [" + skuid + "]");
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.info("taskstatus EXCEPTION for [" + skuid + "]");
        } else if (TaskStatus.NONE.equals(taskStatus)) {
            queue.add(sku);
            if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website) || Website.AMAZON.equals(website)) {
                queue.add(sku);
                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskLevel.LEVEL_2);
            } else {
                queue.add(sku);
                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskLevel.LEVEL_5);
            }
            logger.info("taskstatus NONE for [" + skuid + "] , resend success");
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + skuid + "]");
            fetchUrlResult = fetchDubboService.getProductsByUrl(sku.getWebsite(), sku.getUrl(), TimeUtils.SECONDS_OF_1_DAY);

            FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

            System.out.println(JSONUtil.toJSON(fetchedProduct).toString() + "id=" + skuid);

            try {
                cmpSkuService.createDescription(sku, fetchedProduct);
            } catch (Exception e) {
                logger.info("createDescription fail " + skuid);
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
