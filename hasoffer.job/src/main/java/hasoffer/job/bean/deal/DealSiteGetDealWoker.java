package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
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
    private IDataBaseManager dbm;
    private IDealService dealService;

    public DealSiteGetDealWoker(IDataBaseManager dbm, IFetchDubboService fetchDubboService, IDealService dealService) {
        this.dbm = dbm;
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
                        continue;
                    }

                    TaskStatus taskStatus = fetchDealResult.getTaskStatus();

                    System.out.println("taskStatus " + taskStatus);

                    if (TaskStatus.FINISH.equals(taskStatus)) {

                        List<FetchedDealInfo> dealInfoList = fetchDealResult.getDealInfoList();

                        for (FetchedDealInfo fetchedDealInfo : dealInfoList) {

                            System.out.println("fetchedDealInfo " + fetchedDealInfo.toString());

                            AppDeal deal = getDeal(fetchedDealInfo, dbm);

                            if (deal != null) {
                                dealService.createAppDealByPriceOff(deal);
                            }
                        }

                        break;
                    }

                }

                TimeUnit.MINUTES.sleep(20);
            } catch (Exception e) {
                System.out.println("deal site fetch exception");
                e.printStackTrace();
            }
        }
    }


    public AppDeal getDeal(FetchedDealInfo fetchedDealInfo, IDataBaseManager dbm) {

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
        appdeal.setDescription(fetchedDealInfo.getDescription());
        appdeal.setPresentPrice(fetchedDealInfo.getPrice());
        appdeal.setPriceDescription("Rs." + fetchedDealInfo.getPrice());
        appdeal.setOriginPrice(fetchedDealInfo.getOriPrice());
        appdeal.setDiscount(fetchedDealInfo.getDiscount());

        return appdeal;

    }
}
