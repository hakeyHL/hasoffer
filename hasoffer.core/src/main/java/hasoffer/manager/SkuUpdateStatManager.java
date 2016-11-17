package hasoffer.manager;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.product.SkuUpdateResult;
import hasoffer.core.cache.CmpSkuCacheManager;
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
    @Resource
    CmpSkuCacheManager cmpSkuCacheManager;

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

    public SkuUpdateResult statUpdateResultToday() {
        final long deadLineDate = TimeUtils.yesterday();

        String ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);

        Map<Long, Long> proMap = searchLogCacheManager.getProductCount(ymd);

        final SkuUpdateResult skuUpdateResult = new SkuUpdateResult(ymd);

        if (proMap != null) {
            for (Map.Entry<Long, Long> kv : proMap.entrySet()) {
                long proId = kv.getKey();
                List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(proId);
                for (PtmCmpSku cmpSku : cmpSkus) {
                    boolean successUpdate = countSkuUpdate(skuUpdateResult, cmpSku, deadLineDate);
                    if (!successUpdate) {
                        cmpSkuCacheManager.push2failedUpdate(cmpSku);
                    }
                }
            }

            searchLogCacheManager.cacheStatResult(skuUpdateResult);
        }

        return skuUpdateResult;
    }

    public boolean countSkuUpdate(SkuUpdateResult skuUpdateResult, PtmCmpSku cmpSku, long deadLineDate) {
        if (cmpSku.getWebsite() != null) {

            boolean successUpdate = cmpSku.getUpdateTime().getTime() > deadLineDate;

            // 如果sku状态被更新成下架offsale,则需要判断更新时间才能决定是否更新成功
            if (cmpSku.getStatus() == SkuStatus.OFFSALE) {
                if (!successUpdate) {
                    return false;
                }
            }

            skuUpdateResult.addAllTotal();
            if (successUpdate) {
                skuUpdateResult.addAllSuccess();
            }

            switch (cmpSku.getWebsite()) {
                case FLIPKART:
                    skuUpdateResult.addFlipkartTotal();
                    if (successUpdate) {
                        skuUpdateResult.addFlipkartSuccess();
                    }
                    break;
                case AMAZON:
                    skuUpdateResult.addAmazonTotal();
                    if (successUpdate) {
                        skuUpdateResult.addAmazonSuccess();
                    }
                    break;
                case EBAY:
                    skuUpdateResult.addEbayotal();
                    if (successUpdate) {
                        skuUpdateResult.addEbaySuccess();
                    }
                    break;
                case SHOPCLUES:
                    skuUpdateResult.addShopcluesTotal();
                    if (successUpdate) {
                        skuUpdateResult.addShopcluesSuccess();
                    }
                    break;
                case SNAPDEAL:
                    skuUpdateResult.addSnapdealTotal();
                    if (successUpdate) {
                        skuUpdateResult.addSnapdealSuccess();
                    }
                    break;
                case INFIBEAM:
                    skuUpdateResult.addInfibeamTotal();
                    if (successUpdate) {
                        skuUpdateResult.addInfibeamSuccess();
                    }
                    break;
                case PAYTM:
                    skuUpdateResult.addPaytmTotal();
                    if (successUpdate) {
                        skuUpdateResult.addPaytmSuccess();
                    }
                    break;
                case MYNTRA:
                    skuUpdateResult.addMyntraTotal();
                    if (successUpdate) {
                        skuUpdateResult.addMyntraSuccess();
                    }
                    break;
            }

            return successUpdate;
        }

        return false;
    }
}
