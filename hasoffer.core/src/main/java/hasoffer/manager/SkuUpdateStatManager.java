package hasoffer.manager;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.product.SkuUpdateResult;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by chevy on 2016/11/9.
 */
@Component
public class SkuUpdateStatManager {

    @Resource
    IDataBaseManager dbm;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;


    @DataSource(value = DataSourceType.Slave)
    public SkuUpdateResult statUpdateResult(String ymd) {
        final long deadLineDate = TimeUtils.stringToDate(ymd, "yyyyMMdd").getTime() - TimeUtils.MILLISECONDS_OF_1_DAY;

        final String SQL_FIND_BY_YMD = "SELECT t FROM SrmProductSearchCount t WHERE t.ymd = '" + ymd + "'";

        final SkuUpdateResult skuUpdateResult = new SkuUpdateResult(ymd);

        ListProcessTask<SrmProductSearchCount> listProcessTask = new ListProcessTask<>(
                new ILister() {
                    @Override
                    public PageableResult getData(int page) {
                        System.out.println(page + " : " + skuUpdateResult.toString());
                        return dbm.queryPage(SQL_FIND_BY_YMD, page, 2000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcessor<SrmProductSearchCount>() {
                    @Override
                    public void process(SrmProductSearchCount o) {
                        long proId = o.getProductId();
                        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(proId);
                        for (PtmCmpSku cmpSku : cmpSkus) {
                            countSkuUpdate(skuUpdateResult, cmpSku, deadLineDate);
                        }
                    }
                }
        );

        listProcessTask.go();

        boolean next = true;

        while (next) {
            if (listProcessTask.isAllFinished()) {
                next = false;
            }

            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return skuUpdateResult;
    }


    public SkuUpdateResult statUpdateResultByHour(String ymd_hh) {
        final long deadLineDate = TimeUtils.stringToDate(ymd_hh, "yyyyMMdd_HH").getTime() - TimeUtils.MILLISECONDS_OF_1_DAY;

        Map<Long, Long> proMap = searchLogCacheManager.getProductCountByHour(ymd_hh);

        final SkuUpdateResult skuUpdateResult = new SkuUpdateResult(ymd_hh);

        if (proMap != null) {
            for (Map.Entry<Long, Long> kv : proMap.entrySet()) {
                long proId = kv.getKey();
                List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(proId);
                for (PtmCmpSku cmpSku : cmpSkus) {
                    countSkuUpdate(skuUpdateResult, cmpSku, deadLineDate);
                }
            }

            searchLogCacheManager.cacheStatResult(skuUpdateResult);
        }

        return skuUpdateResult;
    }

    private void countSkuUpdate(SkuUpdateResult skuUpdateResult, PtmCmpSku cmpSku, long deadLineDate) {
        if (cmpSku.getStatus() == SkuStatus.ONSALE && cmpSku.getWebsite() != null) {

            boolean success = cmpSku.getUpdateTime().getTime() > deadLineDate;

            skuUpdateResult.addAllTotal();
            if (success) {
                skuUpdateResult.addAllSuccess();
            }

            switch (cmpSku.getWebsite()) {
                case FLIPKART:
                    skuUpdateResult.addFlipkartTotal();
                    if (success) {
                        skuUpdateResult.addFlipkartSuccess();
                    }
                    break;
                case AMAZON:
                    skuUpdateResult.addAmazonTotal();
                    if (success) {
                        skuUpdateResult.addAmazonSuccess();
                    }
                    break;
                case EBAY:
                    skuUpdateResult.addEbayotal();
                    if (success) {
                        skuUpdateResult.addEbaySuccess();
                    }
                    break;
                case SHOPCLUES:
                    skuUpdateResult.addShopcluesTotal();
                    if (success) {
                        skuUpdateResult.addShopcluesSuccess();
                    }
                    break;
                case SNAPDEAL:
                    skuUpdateResult.addSnapdealTotal();
                    if (success) {
                        skuUpdateResult.addSnapdealSuccess();
                    }
                    break;
                case INFIBEAM:
                    skuUpdateResult.addInfibeamTotal();
                    if (success) {
                        skuUpdateResult.addInfibeamSuccess();
                    }
                    break;
                case PAYTM:
                    skuUpdateResult.addPaytmTotal();
                    if (success) {
                        skuUpdateResult.addPaytmSuccess();
                    }
                    break;
                case MYNTRA:
                    skuUpdateResult.addMyntraTotal();
                    if (success) {
                        skuUpdateResult.addMyntraSuccess();
                    }
                    break;
            }
        }
    }

}
