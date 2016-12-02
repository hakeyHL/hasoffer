package hasoffer.job.bean.deal;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.spider.enums.TaskTarget;
import hasoffer.spider.model.FetchUrlResult;
import hasoffer.spider.model.FetchedProduct;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/9/8.
 */
public class CheckPriceOffDealStatusJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(CheckPriceOffDealStatusJobBean.class);
    //updateTime :2016-11-09 10:27
//    private static final String Q_PRICEOFF_DEAL = "SELECT t From AppDeal t WHERE t.appdealSource = 'PRICE_OFF' AND t.expireTime > ?0 ORDER BY t.createTime DESC";
    private static final String Q_PRICEOFF_DEAL = "SELECT t From AppDeal t WHERE t.appdealSource <> 'MANUAL_INPUT' AND t.expireTime > ?0 ORDER BY t.createTime DESC";

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    private IDealService dealService;
    @Resource
    private IDataBaseManager dbm;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        logger.info("CheckPriceOffDealStatusJobBean is run at {}", new Date());

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int curPage = 1;
                    int pageSize = 1000;

                    PageableResult<AppDeal> pageableResult = dbm.queryPage(Q_PRICEOFF_DEAL, curPage, pageSize, Arrays.asList(TimeUtils.nowDate()));

                    long totalPage = pageableResult.getTotalPage();
                    logger.info("price off deal status total page =" + totalPage);

                    while (curPage <= totalPage) {

                        logger.info("price off deal status curpage =" + curPage);

                        if (curPage > 1) {
                            pageableResult = dbm.queryPage(Q_PRICEOFF_DEAL, curPage, pageSize, Arrays.asList(TimeUtils.nowDate()));
                        }

                        List<AppDeal> dealList = pageableResult.getData();
                        logger.info("find appdeal size =" + dealList.size());

                        for (AppDeal deal : dealList) {

                            //如果是price_off的判断sku的存在
                            if (AppdealSource.PRICE_OFF.equals(deal.getAppdealSource())) {
                                long ptmcmpskuid = deal.getPtmcmpskuid();
                                PtmCmpSku ptmCmpSku = dbm.get(PtmCmpSku.class, ptmcmpskuid);
                                if (ptmCmpSku == null) {
                                    logger.info("get null sku,id = ptmcmpskuid");
                                    continue;
                                }
                                Website website = ptmCmpSku.getWebsite();
                                String url = ptmCmpSku.getUrl();
                                fetchDubboService.sendUrlTask(website, url, TaskTarget.DEAL_UPDATE, TaskLevel.LEVEL_2);
                            }

                            //如果是Deal_site，判断website
                            if (AppdealSource.DEAL_SITE.equals(deal.getAppdealSource())) {
                                if (Website.UNKNOWN.equals(deal.getWebsite())) {
                                    continue;
                                }
                                fetchDubboService.sendUrlTask(deal.getWebsite(), deal.getLinkUrl(), TaskTarget.DEAL_UPDATE, TaskLevel.LEVEL_2);
                            }


                            logger.info("add price off deal to update queue success " + deal.getId());
                            logger.info("add price off deal to update queue success type is " + deal.getAppdealSource());
                        }

                        curPage++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        for (int i = 0; i < 10; i++) {

            es.execute(new Runnable() {
                @Override
                public void run() {

                    long startTime = TimeUtils.now();

                    while (true) {

                        long now = TimeUtils.now();

                        //如果当前时间和启动时间相差超过50分钟，就自杀了吧少年
                        if (now - startTime > TimeUtils.MILLISECONDS_OF_1_MINUTE * 50) {
                            break;
                        }

                        String pop = fetchDubboService.popFetchUrlResult(TaskTarget.DEAL_UPDATE);

                        if (StringUtils.isEmpty(pop)) {
                            try {
                                logger.info("pop deal update list get null sleep 5 seconds");
                                TimeUnit.MINUTES.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            continue;
                        }

                        try {

                            FetchUrlResult fetchUrlResult1 = JSONUtil.toObject(pop, FetchUrlResult.class);
                            FetchedProduct fetchedProduct = fetchUrlResult1.getFetchProduct();
                            if (fetchedProduct == null) {
                                logger.info("deal update fetchedProduct is null");
                            } else {
                                logger.info("deal update fetchedProduct : " + (fetchedProduct).toString());
                            }
                            float nowPrice = fetchedProduct.getPrice();

                            //抓取结果价格为0，过滤下一次
                            if (nowPrice <= 0) {
                                continue;
                            }

                            String url = fetchUrlResult1.getUrl();

                            //data 2016-11-10 10:50
                            //暂时修改更新逻辑，适配deal网站抓取的deal更新
                            PageableResult<AppDeal> pageableResult = dealService.findDealList(1, Integer.MAX_VALUE, 3, "id");
                            List<AppDeal> dealList = pageableResult.getData();
                            for (AppDeal appdeal : dealList) {
                                String linkUrl = appdeal.getLinkUrl();
                                if (StringUtils.isEqual(linkUrl, url)) {
                                    if (appdeal.getPriceDescription() != null) {
                                        String oriPriceString = StringUtils.filterAndTrim(appdeal.getPriceDescription(), Arrays.asList("Rs."));//deal之前的价格
                                        if (NumberUtils.isNumber(oriPriceString)) {
                                            float oriPrice = Float.parseFloat(oriPriceString);//deal之前的价格

                                            //data 2016-11-30 11:30
                                            //降价，生成新deal；涨价，失效不显示
                                            if (nowPrice < oriPrice && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
                                                dealService.updateDealExpire(appdeal.getId(), nowPrice);
                                                logger.info("deal site deal update delete old and create a new deal success");
                                            }
                                            if (nowPrice > oriPrice || !SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
                                                dealService.updateDealExpire(appdeal.getId());
                                                logger.info("deal site deal update orideal expire");
                                            }
                                        }
                                    }
                                }
                            }

                            String urlKey = HexDigestUtil.md5(url);

                            List<PtmCmpSku> skuList = cmpSkuService.getPtmCmpSkuListByUrlKey(urlKey);

                            for (PtmCmpSku sku : skuList) {

                                // try update sku
                                Website website = WebsiteHelper.getWebSite(url);

                                if (website == null) {
                                    continue;
                                }

                                AppDeal appdeal = dealService.getDealBySourceId(sku.getId());

                                if (appdeal != null) {
                                    //data 2016-11-30 11:30
                                    //降价，生成新deal；涨价，失效不显示
                                    if (nowPrice < sku.getPrice() && SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
                                        dealService.updateDealExpire(appdeal.getId(), nowPrice);
                                        logger.info("price off deal update delete old and create a new deal success");
                                    }

                                    if (nowPrice > sku.getPrice() || !SkuStatus.ONSALE.equals(fetchedProduct.getSkuStatus())) {
                                        dealService.updateDealExpire(appdeal.getId());
                                        logger.info("price off deal update orideal expire");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.info("deal update pop string parse error");
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        logger.info("CheckPriceOffDealStatusJobBean will stop at {}", new Date());
    }
}
