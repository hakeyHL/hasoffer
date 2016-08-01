package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2015/12/21.
 */
public class CmpSkuDubboUpdateWorker implements Runnable {

    private static final String Q_PTMCMPSKU_BYPRODUCTID = "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ";
    private static Logger logger = LoggerFactory.getLogger(CmpSkuDubboUpdateWorker.class);
    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<SrmSearchLog> queue;
    private ICmpSkuService cmpSkuService;
    private IFetchDubboService fetchService;
    private IProductService productService;
    private IMongoDbManager mdm;

    public CmpSkuDubboUpdateWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue, ICmpSkuService cmpSkuService, IFetchDubboService fetchService, IProductService productService, IMongoDbManager mdm) {
        this.dbm = dbm;
        this.queue = queue;
        this.cmpSkuService = cmpSkuService;
        this.fetchService = fetchService;
        this.productService = productService;
        this.mdm = mdm;
    }

    @Override
    public void run() {
        while (true) {
            SrmSearchLog searchLog = queue.poll();

            if (searchLog == null) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    logger.info("task update get null sleep 3 seconds");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            long productId = searchLog.getPtmProductId();
            if (productId == 0) {
                continue;
            }

            List<PtmCmpSku> skuList = dbm.query(Q_PTMCMPSKU_BYPRODUCTID, Arrays.asList(productId));

            for (PtmCmpSku sku : skuList) {
                //判断，如果该sku 当天更新过价格, 直接跳过
                Date updateTime = sku.getUpdateTime();
                if (updateTime != null) {
                    if (updateTime.compareTo(TimeUtils.toDate(TimeUtils.today())) > 0) {
                        continue;
                    }
                }

                //更新商品的信息，写入多图数据，写入描述/参数
                updatePtmCmpSku(sku, searchLog);
            }

            //更新商品的价格，同时修改updateTime字段
//            暂时注释掉，测试完再打开
            if (skuList == null || skuList.size() == 0) {
                continue;
            }
            productService.updatePtmProductPrice(productId);
        }
    }

    private void updatePtmCmpSku(PtmCmpSku sku, SrmSearchLog searchLog) {
        // try update sku
        String url = sku.getUrl();
        Website website = WebsiteHelper.getWebSite(url);

        if (website == null) {
            logger.info(" parse website get null for [" + sku.getId() + "]");
            return;
        }

        FetchUrlResult fetchedResult = null;

        try {
            fetchedResult = fetchService.getProductsByUrl(website, url);
        } catch (HttpFetchException e) {
            logger.info("HttpFetchException for [" + sku.getId() + "]");
        } catch (ContentParseException e) {
            logger.info("ContentParseException for [" + sku.getId() + "]");
        }

        TaskStatus taskStatus = fetchedResult.getTaskStatus();

        FetchedProduct fetchedProduct = null;

        //如果返回结果状态为running，那么将sku返回队列
        if (TaskStatus.RUNNING.equals(taskStatus) || TaskStatus.START.equals(taskStatus)) {
            queue.add(searchLog);
//            logger.info("taskstatus RUNNING for [" + sku.getId() + "]");
            return;
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.info("taskstatus STOPPED for [" + sku.getId() + "]");
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.info("taskstatus EXCEPTION for [" + sku.getId() + "]");
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + sku.getId() + "]");
            fetchedProduct = fetchedResult.getFetchProduct();
        }

//        此处是FK、SD正常更新逻辑放弃对title字段的更新，该有另外的task统一维护
//        切换新的更新模式，采用页面更新的方式，所有可以不用考虑title
//        if (fetchedProduct != null) {
//            if (Website.FLIPKART.equals(fetchedProduct.getWebsite()) || Website.SNAPDEAL.equals(fetchedProduct.getWebsite())) {
//                fetchedProduct.setTitle(null);
//            }
//        }

        System.out.println(JSONUtil.toJSON(fetchedProduct).toString());


        //更新ptmcmpsku表
        try {
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(sku.getId(), fetchedProduct);
            logger.info("fetch success for [" + sku.getId() + "]");
        } catch (Exception e) {
            logger.info(e.toString());
            if (fetchedProduct != null) {
                logger.info("title:" + fetchedProduct.getTitle());
            }
        }

        //创建多图
        try {
            List<PtmCmpSkuImage> ptmCmpSkuImageList = dbm.query("SELECT t FROM PtmCmpSkuImage t WHERE t.ptmcmpskuId = ?0 ", Arrays.asList(sku.getId()));
            if (ptmCmpSkuImageList == null || ptmCmpSkuImageList.size() == 0) {

                List<String> imageUrlList = fetchedProduct.getImageUrlList();

                for (int i = 0; i < imageUrlList.size(); i++) {

                    PtmCmpSkuImage ptmCmpSkuImage = new PtmCmpSkuImage();

                    ptmCmpSkuImage.setOriImageUrl(imageUrlList.get(i));
                    ptmCmpSkuImage.setPtmcmpskuId(sku.getId());

                    dbm.create(ptmCmpSkuImage);
                    System.out.println("create ptmCmpSkuImage success for ptmCmpSkuId = [" + sku.getId() + "] " + i);
                }
            }
        } catch (Exception e) {
            System.out.println("create ptmCmpSkuImage fail for ptmCmpSkuId = [" + sku.getId() + "]");
        }


        //添加描述
        try {
            String jsonParam = fetchedProduct.getJsonParam();
            String description = fetchedProduct.getDescription();

            PtmCmpSkuDescription ptmCmpSkuDescription = new PtmCmpSkuDescription();

            ptmCmpSkuDescription.setId(sku.getId());
            ptmCmpSkuDescription.setJsonParam(jsonParam);
            ptmCmpSkuDescription.setDescription(description);

            mdm.save(ptmCmpSkuDescription);
            System.out.println("create ptmCmpSkuDescription success for ptmCmpSkuId = [" + sku.getId() + "]");
        } catch (Exception e) {
            System.out.println("create ptmCmpSkuDescription fail for ptmCmpSkuId = [" + sku.getId() + "]");
        }

    }
}
