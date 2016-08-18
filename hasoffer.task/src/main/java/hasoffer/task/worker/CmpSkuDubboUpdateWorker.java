package hasoffer.task.worker;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
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
public class CmpSkuDubboUpdateWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdateWorker.class);
    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<PtmCmpSku> queue;
    private IFetchDubboService fetchDubboService;
    private ICmpSkuService cmpSkuService;

    public CmpSkuDubboUpdateWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<PtmCmpSku> queue, IFetchDubboService fetchDubboService, IProductService productService, ICmpSkuService cmpSkuService) {
        this.dbm = dbm;
        this.queue = queue;
        this.fetchDubboService = fetchDubboService;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        while (true) {

            try {

                PtmCmpSku sku = queue.poll();

                if (sku == null) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        logger.info("task update get null sleep 3 seconds");
                    } catch (InterruptedException e) {
                        return;
                    }
                    continue;
                }

                Date updateTime = sku.getUpdateTime();
                if (updateTime != null) {
                    if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                        continue;
                    }
                }

                //更新商品的信息，写入多图数据，写入描述/参数
                updatePtmCmpSku(sku);

                //更新商品的价格，同时修改updateTime字段
//                productService.updatePtmProductPrice(productId);

            } catch (Exception e) {

            }
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

        TaskStatus taskStatus = fetchDubboService.getUrlTaskStatus(website, url);

        FetchUrlResult fetchUrlResult = null;

        //如果返回结果状态为running，那么将sku返回队列
        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
            queue.add(sku);
            logger.info("taskstatus RUNNING for [" + skuid + "]");
            return;
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.info("taskstatus STOPPED for [" + skuid + "]");
            return;
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.info("taskstatus EXCEPTION for [" + skuid + "]");
            return;
        } else if (TaskStatus.NONE.equals(taskStatus)) {
            queue.add(sku);
            fetchDubboService.sendUrlTask(sku.getWebsite(), url, TaskLevel.LEVEL_1);
            logger.info("taskstatus NONE for [" + skuid + "]");
            return;
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + skuid + "]");
            fetchUrlResult = fetchDubboService.getProductsByUrl(skuid, sku.getWebsite(), sku.getUrl());

            FetchedProduct fetchedProduct = fetchUrlResult.getFetchProduct();

            System.out.println(JSONUtil.toJSON(fetchedProduct).toString() + "id=" + skuid);

            cmpSkuService.createDescription(sku, fetchedProduct);

            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuid, fetchedProduct);

            cmpSkuService.createPtmCmpSkuImage(skuid, fetchedProduct);

            //对FLIPKART没有类目的数据进行更新,暂时注释掉
//            if (Website.FLIPKART.equals(sku.getWebsite()) && sku.getCategoryId() == 0) {
//
//                List<String> categoryPathList = fetchedProduct.getCategoryPathList();
//
//                String lastCategoryPath = categoryPathList.get(categoryPathList.size() - 1);
//
//                PtmCategory3 ptmCategory3 = dbm.querySingle("SELECT t FROM PtmCategory3 t WHERE t.name = ?0", Arrays.asList(lastCategoryPath));
//
//                long categoryid = ptmCategory3.getHasofferCateogryId();
//
//                if (categoryid != 0) {
//                    cmpSkuService.updateCategoryid(skuid, categoryid);
//                }
//            }
        }
    }
}
