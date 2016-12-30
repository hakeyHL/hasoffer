package hasoffer.job.bean.deal;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.utils.ImageUtil;
import hasoffer.data.redis.IRedisListService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created on 2016/9/14.
 */
@Deprecated
public class CheckGetPriceOffDealJobBean extends QuartzJobBean {

    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(CheckGetPriceOffDealJobBean.class);
    private static final String PRICE_DROP_SKUID_QUEUE = "PRICE_DROP_SKUID_QUEUE";

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IDealService dealService;
    @Resource
    IRedisListService redisListService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("CheckGetPriceOffDealWorker is run at {}", new Date());

        while (true) {

            try {

//          新加逻辑push逻辑使用true---rightPush
//          新加逻辑pop辑使用true---rightPop
//          Date：2016-12-05 12:41
                Object pop = redisListService.pop(PRICE_DROP_SKUID_QUEUE, true);

                if (pop == null) {
                    logger.info("queue size =" + redisListService.size(PRICE_DROP_SKUID_QUEUE));
                    logger.info("CheckGetPriceOffDealJobBean pop get 0 skuid go to die");
                    break;
                }

                long skuid = Long.parseLong((String) pop);
                System.out.println("CheckGetPriceOffDealJobBean pop get " + skuid);

                PtmCmpSku sku = dbm.get(PtmCmpSku.class, skuid);
                if (sku == null) {
                    System.out.println("CheckGetPriceOffDealJobBean pop get " + skuid + " is null");
                    continue;
                }

                //暂时 todo   (该方法废弃，不再使用)
                float minPrice = 0.0f;

                float newPrice = sku.getPrice();
                float oriPrice = sku.getOriPrice();

                if (newPrice < minPrice * 1.1) {//符合条件，创建deal

                    AppDeal appdeal = new AppDeal();

                    appdeal.setWebsite(sku.getWebsite());
                    appdeal.setAppdealSource(AppdealSource.PRICE_OFF);
                    appdeal.setCreateTime(TimeUtils.nowDate());
                    appdeal.setDisplay(true);
                    //question 这种deal只有涨价才失效，加他个365天
                    appdeal.setExpireTime(TimeUtils.addDay(TimeUtils.nowDate(), 365));
                    appdeal.setLinkUrl(sku.getUrl());
                    appdeal.setPush(false);
                    appdeal.setTitle(sku.getTitle());
                    appdeal.setPtmcmpskuid(sku.getId());
                    StringBuilder sb = new StringBuilder();
                    if (newPrice >= minPrice) {//100%-110%
                        //如果现价不低于史低价
                        //文案 Rs.现价 is almost history lowest price(History lowest price is Rs.更新前的史低价). Click here to check price history（点击此行展示价格走势浮层）. Good offer always expire in hours.Good time to get it,Hurry up!
                        sb.append("Rs.").append(BigDecimal.valueOf(newPrice)).append(" is almost history lowest price(History lowest price is Rs.").append(minPrice).append(").Click here to check price history.Good offer always expire in hours.Good time to get it,Hurry up!");
                    } else {//小于100%
                        //否则
                        //Rs.现价 is the newest history lowest price(Previous lowest price is Rs.更新前的史低价).Click here to check price history（以高亮可点击文案展示点击唤出价格走势浮层 具体逻辑见price history）. Good offer always expire in hours.Good time to get it,Hurry up!
                        sb.append("Rs.").append(BigDecimal.valueOf(newPrice)).append(" is newest history lowest price(Previous lowest price is Rs.").append(minPrice).append(").Click here to check price history.Good offer always expire in hours.Good time to get it,Hurry up!");
                    }

                    appdeal.setDescription(sb.toString());
                    appdeal.setPresentPrice(newPrice);
                    appdeal.setPriceDescription("Rs." + (int) newPrice);
                    appdeal.setOriginPrice(oriPrice);
                    appdeal.setDiscount((int) ((1 - newPrice / oriPrice) * 100));

                    //url重复不创建
                    boolean flag = true;
                    List<AppDeal> appdealList = dbm.query("SELECT t FROM AppDeal t WHERE t.linkUrl = ?0", Arrays.asList(sku.getUrl()));
                    if (appdealList != null && appdealList.size() != 0) {
                        System.out.println("query by url get " + appdealList.size() + " sku");
                        flag = false;
                        continue;
                    }

                    //当天title不能重名
                    String title = sku.getTitle();
                    Website website = sku.getWebsite();
                    appdealList = dbm.query("SELECT t FROM AppDeal t WHERE t.title = ?0 AND t.website = ?1 ", Arrays.asList(title, website));
                    if (appdealList != null && appdealList.size() != 0) {
                        System.out.println("query by title website get " + appdealList.size() + " sku");
                        flag = false;
                        continue;
                    }

                    System.out.println("flag " + flag + " then convert image");

                    //todo s3可能又内网的访问方式，不收费
                    String imagePath = sku.getImagePath();
                    imagePath = ImageUtil.getImageUrl(imagePath);
                    String dealPath = "";
                    String dealBigPath = "";
                    String dealSmallPath = "";

                    try {
                        File imageFile = ImageUtil.downloadImage(imagePath);

                        dealPath = ImageUtil.uploadImage(imageFile);
                        dealBigPath = ImageUtil.uploadImage(imageFile, 316, 180);
                        dealSmallPath = ImageUtil.uploadImage(imageFile, 180, 180);

                    } catch (Exception e) {
                        System.out.println("check get priceoff deal image download error");
                        continue;
                    }

                    System.out.println("flag " + flag + " convert image success");

                    appdeal.setImageUrl(dealPath);
                    appdeal.setInfoPageImage(dealBigPath);
                    appdeal.setListPageImage(dealSmallPath);

                    if (flag) {
                        dealService.createAppDealByPriceOff(appdeal);
                        System.out.println("create deal info id " + appdeal.getId() + "_now parice " + appdeal.getPresentPrice());
                        //创建成功一个就跳出
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.info("CheckGetPriceOffDealWorker will stop at {}", new Date());

    }
}
