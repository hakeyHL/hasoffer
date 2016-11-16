package hasoffer.job.bean.fetch;

import com.alibaba.fastjson.JSON;
import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private StdSkuBo convertResultToStdSkuBo(FetchCompareWebsiteResult compareWebsiteFetchResult) throws IOException {

        //product基本信息相关
        FetchedProduct ptmproduct = compareWebsiteFetchResult.getPtmproduct();
        System.out.println("product _" + ptmproduct);

        //sku列表信息相关
        float minPrice = 0.0f;
        boolean flag = true;
        List<StdSkuPrice> skuPrices = new ArrayList<>();
        List<FetchedProduct> ptmcmpskuList = compareWebsiteFetchResult.getPtmcmpskuList();

        for (FetchedProduct ptmcmpsku : ptmcmpskuList) {
            System.out.println("sku _" + ptmcmpsku);
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

        long categoryId = compareWebsiteFetchResult.getCategoryId();
        System.out.println("categoryid _" + categoryId);
        PtmStdSku stdSku = new PtmStdSku(ptmproduct.getTitle(), ptmproduct.getBrand(), ptmproduct.getModel(), categoryId, minPrice, ptmproduct.getSourceId(), ptmproduct.getUrl());


        //product图片列表相关
        List<String> imageUrlList = ptmproduct.getImageUrlList();
        List<StdSkuImage> stdImages = new ArrayList<>();
        if (stdImages != null && stdImages.size() != 0) {
            for (String imageUrl : imageUrlList) {
                System.out.println("imageUrl _" + imageUrl);
                StdSkuImage stdImage = new StdSkuImage(0L, 0L, 0L, imageUrl, "", "", "");
                stdImages.add(stdImage);
            }
        }

        //描述部分
        List<PtmStdSkuParamGroup> paramGroups = new ArrayList<>();
        List<FetchedParamGroup> fetchedParamGroupList = compareWebsiteFetchResult.getFetchedParamGroupList();
        for (FetchedParamGroup fetchedParamGroup : fetchedParamGroupList) {
            String fetchedJson = JSON.toJSONString(fetchedParamGroup);
            System.out.print("param _" + fetchedJson);
            paramGroups.add(JSON.parseObject(fetchedJson, PtmStdSkuParamGroup.class));
        }
        PtmStdSkuDetail stdSkuDetail = new PtmStdSkuDetail(0, paramGroups, "");


        StdSkuBo stdSkuBo = new StdSkuBo(stdSku, null, skuPrices, stdImages, stdSkuDetail);


        return stdSkuBo;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        while (true) {

            FetchCompareWebsiteResult compareWebsiteFetchResult = fetchDubboService.getCompareWebsiteFetchResult(Website.MOBILE91);

            if (compareWebsiteFetchResult == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {

                }
                System.out.println("pop get null wait 10 seconds");
                continue;
            }

            TaskStatus taskStatus = compareWebsiteFetchResult.getTaskStatus();

            if (TaskStatus.FINISH.equals(taskStatus)) {

                StdSkuBo stdSkuBo = null;
                try {
                    stdSkuBo = convertResultToStdSkuBo(compareWebsiteFetchResult);
                } catch (IOException e) {
                    System.out.println("spec convert error");
                    continue;
                }

                boolean stdSku = stdProductService.createStdSku(stdSkuBo);
                System.out.print("create " + stdSku);

            } else {
                System.out.println("pop get " + taskStatus + "continue");
            }
        }
    }
}
