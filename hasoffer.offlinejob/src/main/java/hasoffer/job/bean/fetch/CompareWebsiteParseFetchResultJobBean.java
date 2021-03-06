package hasoffer.job.bean.fetch;

import com.alibaba.fastjson.JSON;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.stdsku.StdSkuAttr;
import hasoffer.core.bo.stdsku.StdSkuBo;
import hasoffer.core.bo.stdsku.StdSkuImage;
import hasoffer.core.bo.stdsku.StdSkuPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.persistence.po.ptm.PtmStdSkuDetail;
import hasoffer.core.persistence.po.ptm.PtmStdSkuParamGroup;
import hasoffer.core.product.IStdProductService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.model.FetchCompareWebsiteResult;
import hasoffer.spider.model.FetchedProduct;
import hasoffer.spider.model.param.FetchedParamGroup;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/8/16.
 */
public class CompareWebsiteParseFetchResultJobBean extends QuartzJobBean {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(ComPareWebsiteSendFetchRequestJobBean.class);

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    IStdProductService stdProductService;
    int getResultNum = 0;//用来标记获取的结果个数
    int resultFinishNum = 0;//正常结果
    int resultExceptionNum = 0;//异常结果

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        while (true) {

            FetchCompareWebsiteResult compareWebsiteFetchResult = fetchDubboService.getCompareWebsiteFetchResult(Website.MOBILE91);

            if (compareWebsiteFetchResult == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {

                }
                logger.info("pop get null wait 10 seconds");
                continue;
            }

            TaskStatus taskStatus = compareWebsiteFetchResult.getTaskStatus();
            getResultNum++;
            logger.info("hava revice " + getResultNum + " response");

            if (TaskStatus.FINISH.equals(taskStatus)) {

                resultFinishNum++;
                logger.info("hava revice finish " + resultFinishNum + " response");

                StdSkuBo stdSkuBo = null;
                try {
                    stdSkuBo = convertResultToStdSkuBo(compareWebsiteFetchResult);
                } catch (Exception e) {
                    logger.info("convert fetch result to stdSkubo exception");
                    e.printStackTrace();
                    continue;
                }

                try {
                    boolean stdSku = stdProductService.createStdSku(stdSkuBo);
                    logger.info("create " + stdSku);
                } catch (Exception e) {
                    logger.info("create fail");
                }

            } else {
                logger.info("pop get " + taskStatus + "continue");
                logger.info(taskStatus + " url " + compareWebsiteFetchResult.getUrl());
                resultExceptionNum++;
                logger.info("hava revice finish" + resultExceptionNum + " response");

            }
        }
    }

    private StdSkuBo convertResultToStdSkuBo(FetchCompareWebsiteResult compareWebsiteFetchResult) throws Exception {

        //product基本信息相关
        FetchedProduct ptmproduct = compareWebsiteFetchResult.getPtmproduct();
        logger.info("product _" + ptmproduct);
        if (ptmproduct == null) {
            throw new RuntimeException();
        }

        //sku列表信息相关
        float minPrice = 0.0f;
        boolean flag = true;
        List<StdSkuPrice> skuPrices = new ArrayList<>();
        List<FetchedProduct> ptmcmpskuList = compareWebsiteFetchResult.getPtmcmpskuList();
        if (ptmcmpskuList != null) {
            for (FetchedProduct ptmcmpsku : ptmcmpskuList) {
                logger.info("sku _" + ptmcmpsku);
                if (ptmcmpsku.getPrice() != 0) {
                    if (flag) {
                        minPrice = ptmcmpsku.getPrice();
                    } else {
                        if (minPrice > ptmcmpsku.getPrice()) {
                            minPrice = ptmcmpsku.getPrice();
                        }
                    }
                }

                StdSkuPrice stdPrice = new StdSkuPrice(0L, 0, ptmcmpsku.getTitle(), ptmcmpsku.getPrice(), 0, ptmcmpsku.getShipping(), ptmcmpsku.getSkuStatus(), ptmcmpsku.getWebsite(), ptmcmpsku.getUrl(), TimeUtils.nowDate(), TimeUtils.nowDate());
                skuPrices.add(stdPrice);
            }
        }

        long categoryId = compareWebsiteFetchResult.getCategoryId();
        logger.info("categoryid _" + categoryId);

        PtmStdSku stdSku = new PtmStdSku(ptmproduct.getTitle(), ptmproduct.getBrand(), ptmproduct.getModel(), categoryId, minPrice, ptmproduct.getSourceId(), ptmproduct.getUrl());

        //product图片列表相关
        List<String> imageUrlList = ptmproduct.getImageUrlList();
        List<StdSkuImage> stdImages = new ArrayList<>();
        if (imageUrlList != null && imageUrlList.size() != 0) {
            for (String imageUrl : imageUrlList) {
                logger.info("imageUrl _" + imageUrl);
                StdSkuImage stdImage = new StdSkuImage(0L, 0L, 0L, imageUrl, "", "", "");
                stdImages.add(stdImage);
            }
        }

        //描述部分
        List<PtmStdSkuParamGroup> paramGroups = new ArrayList<>();
        List<FetchedParamGroup> fetchedParamGroupList = compareWebsiteFetchResult.getFetchedParamGroupList();
        for (FetchedParamGroup fetchedParamGroup : fetchedParamGroupList) {
            String fetchedJson = JSON.toJSONString(fetchedParamGroup);
            logger.info("param _" + fetchedJson);
            paramGroups.add(JSON.parseObject(fetchedJson, PtmStdSkuParamGroup.class));
        }
        PtmStdSkuDetail stdSkuDetail = new PtmStdSkuDetail(0, paramGroups, "");

        logger.info("stdSku = " + stdSku);

        //商品规格
        Map<String, String> attrMap = ptmproduct.getAttrMap();
        Map<String, StdSkuAttr> skuAttrs = null;
        if (attrMap != null && attrMap.size() > 0) {
            skuAttrs = new HashMap<>();
            for (Map.Entry<String, String> attrMapEntry : attrMap.entrySet()) {
                String key = attrMapEntry.getKey();
                String value = attrMapEntry.getValue();
                StdSkuAttr stdSkuAttr = new StdSkuAttr(key, value);
                skuAttrs.put(key, stdSkuAttr);
            }
        }

        StdSkuBo stdSkuBo = new StdSkuBo(stdSku, skuAttrs, skuPrices, stdImages, stdSkuDetail);

        return stdSkuBo;
    }
}
