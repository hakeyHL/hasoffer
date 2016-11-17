package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.utils.ImageUtil;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.model.FetchDealResult;
import hasoffer.spider.model.FetchedDealInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final List<Website> updateDealSiteList = new ArrayList<>();

    //初始化deal抓取的网站
    static {
        dealSiteList.add(Website.DESIDIME);
    }

    //deal更新的网站
    static {
        updateDealSiteList.add(Website.FLIPKART);
        updateDealSiteList.add(Website.SNAPDEAL);
        updateDealSiteList.add(Website.PAYTM);
        updateDealSiteList.add(Website.AMAZON);
        updateDealSiteList.add(Website.SHOPCLUES);
        updateDealSiteList.add(Website.EBAY);
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

            for (Website website : dealSiteList) {

                FetchDealResult fetchDealResult = fetchDubboService.getDealInfo(website);

                if (fetchDealResult == null) {
                    continue;
                }

                TaskStatus taskStatus = fetchDealResult.getTaskStatus();

                if (TaskStatus.FINISH.equals(taskStatus)) {

                    List<FetchedDealInfo> dealInfoList = fetchDealResult.getDealInfoList();

                    for (FetchedDealInfo fetchedDealInfo : dealInfoList) {

                        AppDeal deal = getDeal(fetchedDealInfo, dbm);

                        if (deal != null) {
                            dealService.createAppDealByPriceOff(deal);
                        }
                    }

                    break;
                }

            }

            try {
                TimeUnit.MINUTES.sleep(20);
            } catch (InterruptedException e) {

            }
        }
    }


    public AppDeal getDeal(FetchedDealInfo fetchedDealInfo, IDataBaseManager dbm) {

        AppDeal appdeal = new AppDeal();

        Website webSite = WebsiteHelper.getWebSite(fetchedDealInfo.getLink());

        String webSiteString = WebsiteHelper.getAllWebSiteString(fetchedDealInfo.getLink());

        if (StringUtils.isEmpty(webSiteString)) {
            return null;
        }

        if (webSite == null) {
            webSite = Website.UNKNOWN;
        }

        if (updateDealSiteList.contains(webSite)) {
            appdeal.setExpireTime(TimeUtils.addDay(TimeUtils.nowDate(), 365));
        } else {
            appdeal.setExpireTime(TimeUtils.add(TimeUtils.nowDate(), TimeUtils.MILLISECONDS_OF_1_HOUR * 4));
        }


        appdeal.setWebsite(webSite);
        appdeal.setAppdealSource(AppdealSource.DEAL_SITE);
        appdeal.setCreateTime(TimeUtils.nowDate());
        //这里暂时设置抓回来的设置成false
//        appdeal.setDisplay(true);
        appdeal.setDisplay(false);
        //question 这种deal只有涨价才失效，加他个365天
        appdeal.setLinkUrl(fetchedDealInfo.getLink());
        appdeal.setPush(false);
        appdeal.setTitle(fetchedDealInfo.getTitle());

        StringBuilder sb = new StringBuilder();
        if (false) {
            //has description {//100%-110%
//            //如果现价不低于史低价
//            //文案 Rs.现价 is almost history lowest price(History lowest price is Rs.更新前的史低价). Click here to check price history（点击此行展示价格走势浮层）. Good offer always expire in hours.Good time to get it,Hurry up!
//            sb.append("Rs.").append(newPrice).append(" is almost history lowest price(History lowest price is Rs.").append(minPrice).append(").Click here to check price history.Good offer always expire in hours.Good time to get it,Hurry up!");
        } else {//小于100%

            sb.append(webSiteString).append(" is offering ").append(fetchedDealInfo.getTitle()).append(" .\n");
            sb.append("\n");
            sb.append("Steps to order the item at ").append(webSiteString).append(" website: \n");
            sb.append("\n");
            sb.append("1. First, visit the offer page at ").append(webSiteString).append(" .\n");
            sb.append("2. Select your product according to the item variety.\n");
            sb.append("3. Then click on Buy Now option. \n");
            sb.append("4. Sign in/ Sign up at ").append(webSiteString).append(" and fill up your address. \n");
            sb.append("5. Choose your payment option and make payment your cart value.").append(" .\n");
        }

        appdeal.setDescription(sb.toString());
        appdeal.setPresentPrice(fetchedDealInfo.getPrice());
        appdeal.setPriceDescription("Rs." + fetchedDealInfo.getPrice());
        appdeal.setOriginPrice(fetchedDealInfo.getOriPrice());
        appdeal.setDiscount((int) ((1 - fetchedDealInfo.getPrice() / fetchedDealInfo.getOriPrice()) * 100));

        //url重复不创建
        boolean flag = true;
        List<AppDeal> appdealList = dbm.query("SELECT t FROM AppDeal t WHERE t.linkUrl = ?0", Arrays.asList(fetchedDealInfo.getLink()));
        if (appdealList != null && appdealList.size() != 0) {
            System.out.println("query by url get " + appdealList.size() + " sku");
            flag = false;
            System.out.println("flag " + flag + " then convert image");
            return null;
        }

        System.out.println("flag " + flag + " then convert image");

        //todo s3可能又内网的访问方式，不收费
        String imageUrl = fetchedDealInfo.getImageUrl();

        String dealPath = "";
        String dealBigPath = "";
        String dealSmallPath = "";

        try {
            File imageFile = ImageUtil.downloadImage(imageUrl);

            dealPath = ImageUtil.uploadImage(imageFile);
            dealBigPath = ImageUtil.uploadImage(imageFile, 316, 180);
            dealSmallPath = ImageUtil.uploadImage(imageFile, 180, 180);

        } catch (Exception e) {
            System.out.println("check get priceoff deal image download error");
        }

        appdeal.setImageUrl(dealPath);
        appdeal.setInfoPageImage(dealBigPath);
        appdeal.setListPageImage(dealSmallPath);

        return appdeal;

    }
}
