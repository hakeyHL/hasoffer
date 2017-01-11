package hasoffer.job.listener;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmStdSkuService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/8/16.
 */
public class PtmProductPriceUpdateWorker implements Runnable {

    private IDataBaseManager dbm;
    private IPtmStdSkuService ptmStdSkuService;
    private AppCacheService cacheService;
    private ICmpSkuService cmpSkuService;

    public PtmProductPriceUpdateWorker(IDataBaseManager dbm, IPtmStdSkuService ptmStdSkuService, AppCacheService cacheService, ICmpSkuService cmpSkuService) {
        this.dbm = dbm;
        this.ptmStdSkuService = ptmStdSkuService;
        this.cacheService = cacheService;
        this.cmpSkuService = cmpSkuService;
    }

    @Override
    public void run() {

        //可以写，记得加索引
        Date t1 = TimeUtils.addDay(TimeUtils.nowDate(), -1);
        Date t2 = TimeUtils.add(t1, TimeUtils.MILLISECONDS_OF_1_MINUTE * 10);


        while (true) {

//            System.out.println("product price udpateworker start time from " + t1 + "-to-" + t2);

            //保证更新时间与当前时间有1小时时差
            if (TimeUtils.now() - t1.getTime() < TimeUtils.MILLISECONDS_OF_1_HOUR) {
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            //PtmProduct重新导入solr
            List<Long> productIdList = dbm.query("SELECT distinct t.productId FROM PtmCmpSku t WHERE t.updateTime > ?0 and t.updateTime < ?1", Arrays.asList(t1, t2));

            for (long productid : productIdList) {

                List<PtmCmpSku> skuList = cmpSkuService.listCmpSkus(productid);

                if (skuList == null || skuList.size() <= 0) {
                    continue;
                }
                for (PtmCmpSku sku : skuList) {
                    cacheService.getPtmCmpSku(sku.getId(), 0);
                }

                cacheService.getPtmCmpSku(skuList.get(0).getId(), 1);


//                try {
//                    productService.updatePtmProductPrice(productid);
//                } catch (Exception e) {
//                    System.out.println("update success then reimport product to solr fail for " + productid);
//                }


                //清除商品缓存
//                cacheService.del("PRODUCT_" + productid);
                //清除sku缓存        PRODUCT__listPagedCmpSkus_3198_1_10
//                Set<String> keys = cacheService.keys("PRODUCT__listPagedCmpSkus_" + productid + "_*");

//                for (String key : keys) {
//                    cacheService.del(key);
//                }

                //清除topselling缓存        PRODUCT__listPagedCmpSkus_TopSelling_0_20
//                Set<String> topsellingCache = cacheService.keys("PRODUCT__listPagedCmpSkus_TopSelling" + "_*");
//
//                for (String topCache : topsellingCache) {
//                    cacheService.del(topCache);
//                }

            }

            //PtmStdSku 重新导入solr
            List<Long> stdSkuIdList = dbm.query("SELECT distinct t.stdSkuId FROM PtmStdPrice t WHERE t.updateTime > ?0 and t.updateTime < ?1", Arrays.asList(t1, t2));
            for (long stdSkuId : stdSkuIdList) {

                List<PtmStdPrice> stdPriceList = ptmStdSkuService.listStdPrice(stdSkuId);

                if (stdPriceList == null || stdPriceList.size() <= 0) {
                    continue;
                }

                for (PtmStdPrice stdPrice : stdPriceList) {
                    cacheService.getPtmStdPrice(stdPrice.getId(), 0);
                }
                cacheService.getPtmStdPrice(stdPriceList.get(0).getId(), 1);

//                try {
//                    ptmStdSkuService.importPtmStdSku2Solr(ptmStdSku);
//                } catch (Exception e) {
//                    System.out.println("update success then reimport PtmStdSku to solr fail for " + stdSkuId);
//                }
            }


            t1 = t2;
            t2 = TimeUtils.add(t2, TimeUtils.MILLISECONDS_OF_1_MINUTE * 10);
        }

    }
}
