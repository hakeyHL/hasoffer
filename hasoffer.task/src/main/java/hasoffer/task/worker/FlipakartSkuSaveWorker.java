package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuFetchResult;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.helper.WebsiteSummaryProductProcessorFactory;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/4/28.
 */
public class FlipakartSkuSaveWorker implements Runnable {

    private Logger logger = LoggerFactory.getLogger(FlipakartSkuSaveWorker.class);

    private ConcurrentLinkedQueue<PtmCmpSku> skuQueue;
    private IMongoDbManager mdm;


    public FlipakartSkuSaveWorker(ConcurrentLinkedQueue<PtmCmpSku> skuQueue, IMongoDbManager mdm) {
        this.skuQueue = skuQueue;
        this.mdm = mdm;
    }

    @Override
    public void run() {

        while (true) {

            PtmCmpSku sku = skuQueue.poll();

            if (sku == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println("FlipakartSkuSaveWorker has no jobs. go to sleep!");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            // 判断，如果该sku 当天更新过, 直接跳过
            Date updateTime = sku.getUpdateTime();
            if (updateTime != null) {
                if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                    continue;
                }
            }

            // try update sku
            String url = sku.getUrl();
            Website website = WebsiteHelper.getWebSite(url);

            //website null return
            if (website == null) {
                logger.debug(url + " parse website get null");
                continue;
            }

            ISummaryProductProcessor summaryProductProcessor = WebsiteSummaryProductProcessorFactory.getSummaryProductProcessor(website);

            FetchedProduct fetchedProduct = null;

            try {
                fetchedProduct = summaryProductProcessor.getSummaryProductByUrl(url);
            } catch (HttpFetchException e) {
                logger.debug("httpFetchException for [" + sku.getId() + "]");
            } catch (ContentParseException e) {
                logger.debug("contentparseException for [" + sku.getId() + "]");
            }catch (Exception e){
                logger.debug(e.toString()+" for ["+sku.getId()+"]");
            }

            if (fetchedProduct == null) {
                logger.debug("summaryProductProcessor get product null for [" + sku.getId() + "]");
                continue;
            }

            PtmCmpSkuFetchResult fetchResult = new PtmCmpSkuFetchResult(sku.getId(), fetchedProduct.getUrl());
            fetchResult.setSourceId(fetchedProduct.getSourceSid());
            fetchResult.setSubTitle(fetchedProduct.getSubTitle());
            fetchResult.setPrice(fetchedProduct.getPrice());
            fetchResult.setWebsite(fetchedProduct.getWebsite());
            fetchResult.setTitle(fetchedProduct.getTitle());
            fetchResult.setImageUrl(fetchedProduct.getImageUrl());
//            fetchResult.setPageHtml(summaryProduct.getPageHtml());

            SkuStatus status = null;
            if (ProductStatus.OFFSALE.equals(fetchedProduct.getProductStatus())) {
                status = SkuStatus.OFFSALE;
            } else if (ProductStatus.OUTSTOCK.equals(fetchedProduct.getProductStatus())) {
                status = SkuStatus.OUTSTOCK;
            } else {
                status = SkuStatus.ONSALE;
            }
            fetchResult.setSkuStatus(status);

            mdm.save(fetchResult);
            logger.debug("save success for [" + sku.getId() + "]");
        }

    }
}
