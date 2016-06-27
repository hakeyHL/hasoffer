package hasoffer.task.worker;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.worker.ListAndProcessWorkerStatus;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuDubboUpdateWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdateWorker.class);
    private ListAndProcessWorkerStatus<PtmCmpSku> ws;
    private ICmpSkuService cmpSkuService;
    private IFetchDubboService fetchService;

    public CmpSkuDubboUpdateWorker(ListAndProcessWorkerStatus<PtmCmpSku> ws, ICmpSkuService cmpSkuService , IFetchDubboService fetchService) {
        this.ws = ws;
        this.cmpSkuService = cmpSkuService;
        this.fetchService = fetchService;
    }

    @Override
    public void run() {
        while (true) {
            PtmCmpSku sku = ws.getSdQueue().poll();

            if (sku == null) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.debug("task update get null sleep 3 seconds");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            if (!WebsiteHelper.DEFAULT_WEBSITES.contains(sku.getWebsite())) {
                continue;
            }

//  for test暂时注释掉               判断，如果该sku 当天更新过价格, 直接跳过
//                Date updateTime = sku.getUpdateTime();
//                if (updateTime != null) {
//                    if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
//                        continue;
//                    }
//                }

            // try update sku
            String url = sku.getUrl();
            Website website = WebsiteHelper.getWebSite(url);

            if (website == null) {
                logger.debug(url + " parse website get null");
                continue;
            }

            FetchUrlResult fetchedResult = null;

//            try {
//                fetchedResult = fetchService.getProductsByUrl(website, url);
//            } catch (HttpFetchException e) {
//                e.printStackTrace();
//            } catch (ContentParseException e) {
//                e.printStackTrace();
//            }

            TaskStatus taskStatus = fetchedResult.getTaskStatus();

            FetchedProduct fetchedProduct = null;

            //如果返回结果状态为running，那么将sku返回队列
            if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
                ws.getSdQueue().add(sku);
                continue;
            } else if (TaskStatus.STOPPED.equals(taskStatus)) {
                //do something
            } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
                //do something
            } else {//(TaskStatus.FINISH.equals(taskStatus)))
                fetchedProduct = fetchedResult.getFetchProduct();
            }


            //此处是FK、SD正常更新逻辑放弃对title字段的更新，该有另外的task统一维护
            if (fetchedProduct != null) {
                if (Website.FLIPKART.equals(fetchedProduct.getWebsite()) || Website.SNAPDEAL.equals(fetchedProduct.getWebsite())) {
                    fetchedProduct.setTitle(null);
                }
            }

            try {

                cmpSkuService.updateCmpSkuBySpiderFetchedProduct(sku.getId(), fetchedProduct);
                logger.debug(sku.getId() + " fetch success " + website);
            } catch (Exception e) {
                logger.debug(e.toString());
                if (fetchedProduct != null) {
                    logger.debug("title:" + fetchedProduct.getTitle());
                }
            }
        }
    }
}
