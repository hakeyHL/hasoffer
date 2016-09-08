package hasoffer.job.bean.deal;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.AppdealSource;
import hasoffer.core.persistence.mongo.PriceNode;
import hasoffer.core.persistence.mongo.PtmCmpSkuHistoryPrice;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckGetPriceOffDealJobBean extends QuartzJobBean {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(CheckGetPriceOffDealJobBean.class);
    private static int aliveThreadCount = 10;

    @Resource
    IMongoDbManager mdm;
    @Resource
    private IDealService dealService;
    @Resource
    private IDataBaseManager dbm;

    @Override
    protected void executeInternal(final JobExecutionContext context) throws JobExecutionException {
        logger.info("FetchJobBean is run at {}", new Date());

        //每隔10分钟，检测价格降低的商品
        Date endDate = TimeUtils.nowDate();
        Date startDate = TimeUtils.add(endDate, TimeUtils.MILLISECONDS_OF_1_MINUTE * 10);

        ExecutorService es = Executors.newCachedThreadPool();
        final ConcurrentLinkedQueue<PtmCmpSku> skuIdQueue = new ConcurrentLinkedQueue<PtmCmpSku>();
//select id from ptmcmpsku  where updateTime > '2016-09-05 11:38:00' and updateTime < '2016-09-05 11:48:00';  1.27s
        List<PtmCmpSku> skuList = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.updateTime >= ?0 AND t.updateTime < ?1", Arrays.asList(startDate, endDate));

        if (skuList.size() == 0) {
            System.out.println("updateTime bigger than " + startDate + " small than " + endDate + "get zero sku");
        } else {

            for (PtmCmpSku sku : skuList) {
                skuIdQueue.add(sku);
            }

            //查询mongo获取最低价格，判断是否生成deal
            for (int i = 0; i < 10; i++) {
                es.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {

                            PtmCmpSku sku = skuIdQueue.poll();

                            if (sku == null) {
                                break;
                            }

                            Long skuId = sku.getId();
                            float newPrice = sku.getPrice();
                            float oriPrice = sku.getOriPrice();

                            //如果没有原价，不创建deal
                            if (oriPrice <= 0.0) {
                                continue;
                            }
                            //对当前价格进行判断，小于等于0，下一个
                            if (newPrice <= 0.0) {
                                continue;
                            }

                            PtmCmpSkuHistoryPrice historyPrice = mdm.queryOne(PtmCmpSkuHistoryPrice.class, skuId);

                            float minPrice = getMinPrice(historyPrice);
                            if (newPrice < 1.1 * minPrice) {//符合条件，创建deal

                                AppDeal appdeal = new AppDeal();

                                appdeal.setImageUrl(sku.getImagePath());
                                appdeal.setInfoPageImage(sku.getImagePath());
                                appdeal.setListPageImage(sku.getSmallImagePath());
                                appdeal.setWebsite(sku.getWebsite());
                                appdeal.setAppdealSource(AppdealSource.PRICE_OFF);
                                appdeal.setCreateTime(TimeUtils.nowDate());
                                appdeal.setDisplay(true);
                                //question 这种deal只有涨价才失效，加他个365天
                                appdeal.setExpireTime(TimeUtils.add(TimeUtils.nowDate(), 365));
                                appdeal.setLinkUrl(sku.getUrl());
                                appdeal.setPush(false);
                                appdeal.setTitle(sku.getTitle());
                                appdeal.setPtmcmpskuid(sku.getId());

                                String description = "";
                                if (newPrice > minPrice) {//100%-110%
                                    //网站名// is offering //deal标题// for Rs.//SKU价格//. The price is nearly the history lowest price Rs.(//SKU最低价格//).It’s good time to get the item.
                                    description = sku.getWebsite().toString() + " is offering " + sku.getTitle() + " for Rs." + (int) sku.getPrice() + ". The price is nearly the history lowest price Rs." + (int) minPrice + ".It’s good time to get the item.";
                                } else if (newPrice == minPrice) {//等于100%
                                    //网站名// is offering //deal标题// for Rs.//SKU价格//. The price is the history lowest price. Good offer always expire soon.Hurry up!
                                    description = sku.getWebsite().toString() + " is offering " + sku.getTitle() + " for Rs." + (int) sku.getPrice() + ".The price is the history lowest price. Good offer always expire soon.Hurry up!";
                                } else {//小于100%
                                    //网站名// is offering //deal标题// for Rs.//SKU价格//. The price is new history lowest price !  Good offer always expire soon.Hurry up!
                                    description = sku.getWebsite().toString() + " is offering " + sku.getTitle() + " for Rs." + (int) sku.getPrice() + ".The price is new history lowest price !  Good offer always expire soon.Hurry up!";
                                }

                                appdeal.setDescription(description);
                                appdeal.setPriceDescription("Rs." + (int) newPrice);
                                appdeal.setOriginPrice(oriPrice);
                                appdeal.setDiscount((int) (1 - newPrice / oriPrice) * 100);

                                if (appdeal != null) {
                                    dealService.createAppDealByPriceOff(appdeal);
                                }

                            }
                        }
                        aliveThreadCount--;
                        System.out.println("CheckGetPriceOffDealJobBean process thread die -1");
                    }
                });
            }
        }

        //如果解析线程没有死光，就死循环
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(10);
                System.out.println("main thread slepping wait process thread dead out");
            } catch (InterruptedException e) {

            }
            if (aliveThreadCount == 0) {
                System.out.println("CheckGetPriceOffDealJobBean alive process threadNum " + aliveThreadCount);
                break;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("executeInternal(JobExecutionContext context={}) - end", context);
        }
    }

    private float getMinPrice(PtmCmpSkuHistoryPrice historyPrice) {

        float minPrice = 0.0f;

        //获取历史最低价格
        List<PriceNode> priceNodes = historyPrice.getPriceNodes();
        for (int i = 0; i < priceNodes.size(); i++) {

            if (i == 0) {
                minPrice = priceNodes.get(i).getPrice();
            } else {
                float anotherPrice = priceNodes.get(i).getPrice();
                if (anotherPrice < minPrice) {
                    minPrice = anotherPrice;
                }
            }
        }

        return minPrice;
    }
}
