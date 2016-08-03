package hasoffer.task.worker;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.mongo.PtmProductDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuImage;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.IPtmCmpSkuImageService;
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
    private IPtmCmpSkuImageService ptmCmpSkuImageService;
    private IMongoDbManager mdm;

    public CmpSkuDubboUpdateWorker(IDataBaseManager dbm, ConcurrentLinkedQueue<SrmSearchLog> queue, ICmpSkuService cmpSkuService, IFetchDubboService fetchService, IProductService productService, IMongoDbManager mdm, IPtmCmpSkuImageService ptmCmpSkuImageService) {
        this.dbm = dbm;
        this.queue = queue;
        this.cmpSkuService = cmpSkuService;
        this.fetchService = fetchService;
        this.productService = productService;
        this.mdm = mdm;
        this.ptmCmpSkuImageService = ptmCmpSkuImageService;
    }

    @Override
    public void run() {

        while (true) {

            try {

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
                if (skuList == null || skuList.size() == 0) {
                    continue;
                }

                productService.updatePtmProductPrice(productId);

            } catch (Exception e) {

            }
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
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queue.add(searchLog);
            logger.info("taskstatus RUNNING for [" + sku.getId() + "]");
            return;
        } else if (TaskStatus.STOPPED.equals(taskStatus)) {
            logger.info("taskstatus STOPPED for [" + sku.getId() + "]");
            return;
        } else if (TaskStatus.EXCEPTION.equals(taskStatus)) {
            logger.info("taskstatus EXCEPTION for [" + sku.getId() + "]");
//            logger.info("EXCEPTION url:[" + sku.getUrl() + "]");
            return;
        } else {//(TaskStatus.FINISH.equals(taskStatus)))
            logger.info("taskstatus FINISH for [" + sku.getId() + "]");
            fetchedProduct = fetchedResult.getFetchProduct();
        }

        System.out.println(JSONUtil.toJSON(fetchedProduct).toString());

        //更新ptmcmpsku表
        try {
            //送达时间如果为空，写1-5天
            if (StringUtils.isEmpty(fetchedProduct.getDeliveryTime())) {
                fetchedProduct.setDeliveryTime("1-5");
            }
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(sku.getId(), fetchedProduct);
        } catch (Exception e) {
            if (fetchedProduct != null) {
                logger.info("title:" + fetchedProduct.getTitle());
            }
        }

        //创建多图
        try {
            PtmCmpSkuImage ptmCmpSkuImage = dbm.querySingle("SELECT t FROM PtmCmpSkuImage t WHERE t.id = ?0 ", Arrays.asList(sku.getId()));

            if (ptmCmpSkuImage == null) {

                List<String> imageUrlList = fetchedProduct.getImageUrlList();

                if (imageUrlList != null && imageUrlList.size() != 0) {

                    ptmCmpSkuImage = new PtmCmpSkuImage();

                    ptmCmpSkuImage.setId(sku.getId());
                    ptmCmpSkuImage.setOriImageUrlNumber(imageUrlList.size() >= 4 ? 4 : imageUrlList.size());//如果数量大于4，就存4张

                    for (int i = 0; i < imageUrlList.size(); i++) {

                        if (i == 0) {
                            ptmCmpSkuImage.setOriImageUrl1(imageUrlList.get(i));
                        } else if (i == 1) {
                            ptmCmpSkuImage.setOriImageUrl2(imageUrlList.get(i));
                        } else if (i == 2) {
                            ptmCmpSkuImage.setOriImageUrl3(imageUrlList.get(i));
                        } else if (i == 3) {
                            ptmCmpSkuImage.setOriImageUrl4(imageUrlList.get(i));
                        } else {
                            continue;
                        }
                    }

                    ptmCmpSkuImageService.createPtmCmpSkuImage(ptmCmpSkuImage);
                    System.out.println("create ptmCmpSkuImage success for ptmCmpSkuId = [" + sku.getId() + "]");
                }
            } else {

                System.out.println("get null or 0 imageurl for ptmCmpSkuId = [" + sku.getId() + "]");

            }
        } catch (Exception e) {
            System.out.println("create ptmCmpSkuImage fail for ptmCmpSkuId = [" + sku.getId() + "]");
        }


        //添加描述
        try {

            PtmCmpSkuDescription ptmCmpSkuDescription = mdm.queryOne(PtmCmpSkuDescription.class, sku.getId());

            if (ptmCmpSkuDescription == null) {
                String jsonParam = fetchedProduct.getJsonParam();
                String description = fetchedProduct.getDescription();

                ptmCmpSkuDescription = new PtmCmpSkuDescription();

                ptmCmpSkuDescription.setId(sku.getId());
                ptmCmpSkuDescription.setJsonParam(jsonParam);
                ptmCmpSkuDescription.setJsonDescription(description);

                if (StringUtils.isEmpty(jsonParam) && StringUtils.isEmpty(description)) {
                    return;
                }
                mdm.save(ptmCmpSkuDescription);
                System.out.println("create ptmCmpSkuDescription success for ptmCmpSkuId = [" + sku.getId() + "]");
            }

            //save productDescription
            PtmProductDescription ptmProductDescription = mdm.queryOne(PtmProductDescription.class, sku.getProductId());
            if (ptmProductDescription == null) {

                String jsonParam = fetchedProduct.getJsonParam();
                String description = fetchedProduct.getDescription();

                ptmCmpSkuDescription = new PtmCmpSkuDescription();

                ptmCmpSkuDescription.setId(sku.getId());
                //最开始需求没说明白描述和参数问题，字段写错了，修改通知前台
                ptmCmpSkuDescription.setJsonDescription(jsonParam);
//                ptmCmpSkuDescription.setJsonParam(description);

                if (StringUtils.isEmpty(jsonParam) && StringUtils.isEmpty(description)) {
                    return;
                }
                mdm.save(ptmProductDescription);
                System.out.println("create ptmProductDescription success for ptmproductId = [" + sku.getProductId() + "]");

            }

        } catch (Exception e) {

            logger.info(e.getMessage());
            System.out.println("create description fail for ptmCmpSkuId = [" + sku.getId() + "]");
        }

    }
}
