package hasoffer.job.bean.fetch;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.PtmStdBrandCard;
import hasoffer.core.persistence.mongo.PtmStdSkuDescription;
import hasoffer.core.product.IStdProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spider.model.FetchedProductReview;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/8/16.
 */
public class PtmStdSkuFetchReviewJobBean extends QuartzJobBean {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(PtmStdSkuFetchReviewJobBean.class);

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    IStdProductService stdProductService;
    @Resource
    IMongoDbManager mdm;
    @Resource
    IDataBaseManager dbm;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        long startTime = TimeUtils.now();
//
//        //send
//        int curPage = 1;
//        int pageSize = 1000;
//
//        PageableResult<PtmStdSku> pageableResult = dbm.queryPage("SELECT t FROM PtmStdSku t ORDER BY t.id", curPage, pageSize);
//
//        long totalPage = pageableResult.getTotalPage();
//        while (curPage <= totalPage) {
//
//            if (curPage > 1) {
//                pageableResult = dbm.queryPage("SELECT t FROM PtmStdSku t ORDER BY t.id", curPage, pageSize);
//            }
//
//            List<PtmStdSku> stdSkuList = pageableResult.getData();
//
//            if (stdSkuList != null && stdSkuList.size() > 0) {
//                for (PtmStdSku ptmStdSku : stdSkuList) {
//                    if (!StringUtils.isEmpty(ptmStdSku.getSourceUrl())) {
//                        fetchDubboService.sendCompareWebsiteFetchTask(Website.MOBILE91, ptmStdSku.getSourceUrl(), TaskLevel.LEVEL_1, ptmStdSku.getId());
//                        logger.info("PtmStdSkuFetchReviewJobBean send request success for " + ptmStdSku.getId() + " " + ptmStdSku.getSourceUrl());
//                    }
//                }
//            }
//            curPage++;
//        }

        //while true receive result
        while (true) {

            FetchCompareWebsiteResult compareWebsiteFetchResult = fetchDubboService.getCompareWebsiteFetchResult(Website.MOBILE91);

            if (compareWebsiteFetchResult == null) {
                try {
                    TimeUnit.SECONDS.sleep(3);

                    //如果程序存活超过1天，自杀
                    if (TimeUtils.now() - startTime > TimeUtils.MILLISECONDS_OF_1_DAY) {
                        break;
                    }

                } catch (InterruptedException e) {

                }
                logger.info("pop get null wait 10 seconds");
                continue;
            }

            TaskStatus taskStatus = compareWebsiteFetchResult.getTaskStatus();
            logger.info("PtmStdSkuFetchReviewJobBean revice " + taskStatus + " response");

            if (TaskStatus.FINISH.equals(taskStatus)) {

                long ptmstdSkuId = compareWebsiteFetchResult.getCategoryId();//此处借用了categoryId字段用来传值
                FetchedProduct fetchedProduct = compareWebsiteFetchResult.getPtmproduct();

                if (fetchedProduct == null) {
                    logger.info("PtmStdSkuFetchReviewJobBean fetchedProduct is null");
                    continue;
                }
                logger.info("PtmStdSkuFetchReviewJobBean fetchedProduct " + fetchedProduct);

                String brandName = fetchedProduct.getBrand();
                String brandCardString = fetchedProduct.getBrandCard();
                String feathers = fetchedProduct.getUniqueFeatures();
                String summary = fetchedProduct.getSummary();
                List<FetchedProductReview> fetchedProductReviewList = fetchedProduct.getFetchedProductReviewList();

                if (brandName == null) {
                    logger.info("PtmStdSkuFetchReviewJobBean fetchedProduct brandName is null");
                    continue;
                }
                String brandCardId = HexDigestUtil.md5(brandName.toUpperCase());
                logger.info("PtmStdSkuFetchReviewJobBean brandCardId " + brandCardId + " " + brandName);

/**---------------------------关于PtmStdBrandCard如果不存在就创建，如果存在就跳过-------------------------------------------------*/
                try {
                    PtmStdBrandCard ptmStdBrandCard = mdm.queryOne(PtmStdBrandCard.class, brandCardId);

                    if (ptmStdBrandCard == null) {
                        logger.info("PtmStdSkuFetchReviewJobBean queryOne  PtmStdBrandCard get null");
                        ptmStdBrandCard = new PtmStdBrandCard();
                        ptmStdBrandCard.setId(brandCardId);
                        ptmStdBrandCard.setBrandName(brandName);
                        ptmStdBrandCard.setBrandCardString(brandCardString);
                        stdProductService.createBrandCard(ptmStdBrandCard);
                    } else {
                        logger.info("PtmStdSkuFetchReviewJobBean queryOne  PtmStdBrandCard get not null");
                    }
                } catch (Exception e) {
                    logger.info("PtmStdSkuFetchReviewJobBean create ptmStdBrandCard fail" + brandName);
                    e.printStackTrace();
                }
/**---------------------------关于PtmStdBrandCard如果不存在就创建，如果存在就跳过-------------------------------------------------*/


/**---------------------------关于PtmStdSkuDescription如果不存在就创建，如果存在需要更新-------------------------------------------------*/
                try {

                    PtmStdSkuDescription oldPtmStdSkuDescription = mdm.queryOne(PtmStdSkuDescription.class, brandCardId);

                    PtmStdSkuDescription newPtmStdSkuDescription = new PtmStdSkuDescription();
                    newPtmStdSkuDescription.setId(ptmstdSkuId);
                    newPtmStdSkuDescription.setSummary(summary);
                    newPtmStdSkuDescription.setFeatures(feathers);
                    newPtmStdSkuDescription.setFetchedProductReviewList(fetchedProductReviewList);

                    logger.info("PtmStdSkuDescription newPtmStdSkuDescription " + newPtmStdSkuDescription);

                    if (oldPtmStdSkuDescription == null) {
                        stdProductService.createPtmStdSkuDescription(newPtmStdSkuDescription);
                        logger.info("PtmStdSkuDescription createPtmStdSkuDescription success");
                    } else {
                        stdProductService.updatePtmStdSkuDescription(newPtmStdSkuDescription, oldPtmStdSkuDescription);
                        logger.info("PtmStdSkuDescription updatePtmStdSkuDescription success");
                    }

                } catch (Exception e) {
                    logger.info("PtmStdSkuFetchReviewJobBean saveOrUpdateptm StdSkuDescription fail " + ptmstdSkuId);
                    e.printStackTrace();
                }
/**---------------------------关于PtmStdSkuDescription如果不存在就创建，如果存在需要更新-------------------------------------------------*/
            }
        }
    }
}
