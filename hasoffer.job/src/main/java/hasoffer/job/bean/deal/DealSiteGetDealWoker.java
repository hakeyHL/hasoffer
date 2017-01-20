package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.model.FetchedDealInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/14.
 * 该worker用来将抓取返回的数据，封装成appdeal对象
 */
public class DealSiteGetDealWoker implements Runnable {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(DealSiteGetDealWoker.class);
    private static final List<Website> dealSiteList = new ArrayList<>();

    //初始化deal抓取的网站
    static {
        dealSiteList.add(Website.DESIDIME);
    }

    private IFetchDubboService fetchDubboService;
    private IDealService dealService;

    public DealSiteGetDealWoker(IFetchDubboService fetchDubboService, IDealService dealService) {
        this.fetchDubboService = fetchDubboService;
        this.dealService = dealService;
    }


    @Override
    public void run() {
        while (true) {

            try {

                for (Website website : dealSiteList) {

                    FetchDealResult fetchDealResult = fetchDubboService.getDealInfo(website);

                    if (fetchDealResult == null) {
                        logger.info("getDealInfo get null");
                        continue;
                    }

                    TaskStatus taskStatus = fetchDealResult.getTaskStatus();

                    logger.info("taskStatus " + taskStatus);

                    if (TaskStatus.FINISH.equals(taskStatus)) {

                        List<FetchedDealInfo> dealInfoList = fetchDealResult.getDealInfoList();

                        for (FetchedDealInfo fetchedDealInfo : dealInfoList) {

                            logger.info("fetchedDealInfo " + fetchedDealInfo.toString());

                            AppDeal deal = getDeal(fetchedDealInfo);

                            if (deal != null) {
                                dealService.createAppDealByPriceOff(deal);
                            }
                        }

                        break;
                    }
                }

                TimeUnit.SECONDS.sleep(20);
            } catch (Exception e) {
                logger.info("deal site fetch exception");
                e.printStackTrace();
            }
        }
    }


    public AppDeal getDeal(FetchedDealInfo fetchedDealInfo) {

        AppDeal appdeal = new AppDeal();

        Website webSite = WebsiteHelper.getWebSite(fetchedDealInfo.getLink());

        if (webSite == null) {
            webSite = Website.UNKNOWN;
        }

        appdeal.setWebsite(webSite);
        appdeal.setAppdealSource(AppdealSource.DEAL_SITE);
        appdeal.setCreateTime(TimeUtils.nowDate());
        appdeal.setExpireTime(TimeUtils.add(TimeUtils.nowDate(), TimeUtils.MILLISECONDS_OF_1_HOUR * 24));
//        appdeal.setDisplay(true);
        appdeal.setDisplay(false);
        appdeal.setLinkUrl(fetchedDealInfo.getLink());
        appdeal.setOriLinkUrl(fetchedDealInfo.getOriLink());
        appdeal.setPush(false);
        appdeal.setTitle(fetchedDealInfo.getTitle());
        appdeal.setCategory(fetchedDealInfo.getCategoryName());
        appdeal.setDealClickCount(Long.valueOf(fetchedDealInfo.getView()));
        appdeal.setDescription(StringUtils.unescapeHtml(fetchedDealInfo.getDescription()));
        appdeal.setPresentPrice(fetchedDealInfo.getPrice());
        appdeal.setPriceDescription("Rs." + fetchedDealInfo.getPrice());
        appdeal.setOriginPrice(fetchedDealInfo.getOriPrice());
        appdeal.setDiscount(fetchedDealInfo.getDiscount());
        appdeal.setOriginClickCount(Long.valueOf(fetchedDealInfo.getView()));

        return appdeal;
    }
}
