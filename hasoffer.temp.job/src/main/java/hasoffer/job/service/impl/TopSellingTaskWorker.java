package hasoffer.job.service.impl;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.job.dto.TopSellingTaskDTO;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.List;
import java.util.Queue;

/**
 * Created on 2015/12/21.
 */
public class TopSellingTaskWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(TopSellingTaskWorker.class);
    private Queue<TopSellingTaskDTO> queue;
    private IFetchDubboService fetchDubboService;
    private ICmpSkuService cmpSkuService;

    public TopSellingTaskWorker(List<TopSellingTaskDTO> queue, IFetchDubboService fetchDubboService, ICmpSkuService cmpSkuService, IPriceOffNoticeService priceOffNoticeService) {
        this.queue = new ArrayDeque<>(queue);
        this.fetchDubboService = fetchDubboService;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        while (!queue.isEmpty()) {
            try {
                TopSellingTaskDTO sku = queue.poll();
                Date updateTime = sku.getUpdateTime();
                if (updateTime != null) {
                    if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                        continue;
                    }
                }
                //更新商品的信息，写入多图数据，写入描述/参数
                updatePtmCmpSku(sku);
            } catch (Exception e) {
                System.out.println(TimeUtils.nowDate());
                e.printStackTrace();
            }
        }
    }

    private void updatePtmCmpSku(TopSellingTaskDTO sku) {
        // try update sku
        Long skuid = sku.getId();
        String url = sku.getUrl();
        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            return;
        }

        int expireSeconds = 60 * 20;
        TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(website, url, expireSeconds);

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
                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskLevel.LEVEL_2);
            } else {
                fetchDubboService.sendUrlTask(sku.getWebsite(), sku.getUrl(), TaskLevel.LEVEL_5);
            }
            logger.info("taskstatus NONE for [" + skuid + "] , resend success");
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + skuid + "]");
            fetchUrlResult = fetchDubboService.getProductsByUrl(sku.getWebsite(), sku.getUrl(), expireSeconds);

            FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

            System.out.println(JSONUtil.toJSON(fetchedProduct).toString() + "id=" + skuid);

//            该更新方法已经过时
//            try {
//                PtmCmpSku ptmCmpSku = new PtmCmpSku();
//                ptmCmpSku.setId(sku.getId());
//                ptmCmpSku.setProductId(sku.getProductId());
//                ptmCmpSku.setUrl(sku.getUrl());
//                ptmCmpSku.setUpdateTime(sku.getUpdateTime());
//                cmpSkuService.createDescription(ptmCmpSku, fetchedProduct);
//            } catch (Exception e) {
//                logger.info("createDescription fail " + skuid);
//            }

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
        }
    }
}
