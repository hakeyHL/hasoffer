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
import hasoffer.data.redis.IRedisListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckGetPriceOffDealWorker implements Runnable {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(CheckGetPriceOffDealWorker.class);
    private static final String PRICE_DROP_SKUID_QUEUE = "PRICE_DROP_SKUID_QUEUE";

    private IMongoDbManager mdm;
    private IDataBaseManager dbm;
    private IDealService dealService;
    private IRedisListService redisListService;

    public CheckGetPriceOffDealWorker(IMongoDbManager mdm, IDataBaseManager dbm, IDealService dealService, IRedisListService redisListService) {
        this.mdm = mdm;
        this.dbm = dbm;
        this.dealService = dealService;
        this.redisListService = redisListService;
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

    @Override
    public void run() {
        logger.info("CheckGetPriceOffDealWorker is run at {}", new Date());

        while (true) {

            try {


                Object pop = redisListService.pop(PRICE_DROP_SKUID_QUEUE);

                if (pop == null) {
                    try {
                        TimeUnit.MINUTES.sleep(10);
                    } catch (InterruptedException e) {

                    }
                    System.out.println("CheckGetPriceOffDealJobBean has no more skuid sleep 10 min");
                    continue;
                }

                long skuid = Long.parseLong((String) pop);

                PtmCmpSku sku = dbm.get(PtmCmpSku.class, skuid);
                if (sku == null) {
                    continue;
                }

                float newPrice = sku.getPrice();
                float oriPrice = sku.getOriPrice();

                //SKU的原价不为空
                if (oriPrice <= 0.0) {
                    continue;
                }
                //SKU现价不为0
                if (newPrice <= 0.0) {
                    continue;
                }
                //SKU的现价小于原价
                if (newPrice >= oriPrice) {
                    continue;
                }

                PtmCmpSkuHistoryPrice historyPrice = mdm.queryOne(PtmCmpSkuHistoryPrice.class, skuid);

                float minPrice = getMinPrice(historyPrice);
                if (newPrice < 1.1 * minPrice) {//符合条件，创建deal

                    AppDeal appdeal = new AppDeal();

                    appdeal.setImageUrl(sku.getImagePath());
                    appdeal.setInfoPageImage(sku.getImagePath());
                    appdeal.setListPageImage(sku.getSmallImagePath());
                    appdeal.setWebsite(sku.getWebsite());
                    appdeal.setAppdealSource(AppdealSource.PRICE_OFF);
                    appdeal.setCreateTime(TimeUtils.nowDate());
                    appdeal.setDisplay(false);
                    //question 这种deal只有涨价才失效，加他个365天
                    appdeal.setExpireTime(TimeUtils.addDay(TimeUtils.nowDate(), 365));
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
                    appdeal.setDiscount((int) ((1 - newPrice / oriPrice) * 100));

                    if (appdeal != null) {
                        dealService.createAppDealByPriceOff(appdeal);
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
