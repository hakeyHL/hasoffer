package hasoffer.admin.worker;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.log.ShopcluesFixLog;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IDataFixService;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.shopclues.ShopcluesHelper;
import hasoffer.fetch.sites.shopclues.ShopcluesListProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/4/20.
 */
public class ShopcluesOffsaleUpdateWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ShopcluesOffsaleUpdateWorker.class);

    ConcurrentLinkedQueue<PtmCmpSku> skuQueue;
    ICmpSkuService cmpSkuService;
    IDataBaseManager dbm;
    IDataFixService dataFixService;

    public ShopcluesOffsaleUpdateWorker(ConcurrentLinkedQueue<PtmCmpSku> skuQueue, ICmpSkuService cmpSkuService, IDataBaseManager dbm, IDataFixService dataFixService) {
        this.skuQueue = skuQueue;
        this.cmpSkuService = cmpSkuService;
        this.dbm = dbm;
        this.dataFixService = dataFixService;
    }

    @Override
    public void run() {

        ShopcluesListProcessor processor = new ShopcluesListProcessor();

        while (true) {

            PtmCmpSku sku = skuQueue.poll();

            if (sku == null) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    logger.debug("update job has no jobs. go to sleep!");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

            //设置log初始状态
            ShopcluesFixLog log = dbm.get(ShopcluesFixLog.class, sku.getId());
            if (log == null) {

                ShopcluesFixLog fixLog = new ShopcluesFixLog();
                fixLog.setId(sku.getId());
                fixLog.setOriStatus(sku.getStatus());
                fixLog.setOriUrl(sku.getUrl());

                log = dataFixService.createShopcluesFixLog(fixLog);
            } else {
                log.setOriUrl(sku.getUrl());
                log.setOriStatus(sku.getStatus());
            }

            //获取关键字 1.getTitle（过滤掉左右空格等等特殊字符） 2.解析url获得
            String keyword = ShopcluesHelper.getKeywordFromSkuUrl(sku.getUrl());

            //重新搜索
            try {
                List<ListProduct> productList = processor.getProductSetByKeyword(keyword, 10);

                Map<ListProduct, Float> resultMap = new HashMap<ListProduct, Float>();

                // 匹配价格
                double priceOrigin = sku.getPrice();

                //过滤结果
                if (ArrayUtils.hasObjs(productList)) {

                    logger.debug("listproduct get " + productList.size() + " product for [" + sku.getId() + "]");

                    for (ListProduct listProduct : productList) {
                        float newPrice = listProduct.getPrice();
                        if (newPrice < priceOrigin * 0.7 || newPrice > priceOrigin * 1.3) {
                            logger.debug("price error continue");
                            continue;
                        }

                        //匹配第一个单词
                        String title = listProduct.getTitle();
                        if (!StringUtils.matchedFirstWord(keyword, title)) {
                            logger.debug("first word not match continue");
                            continue;
                        }

                        //过滤特殊字符
                        title = StringUtils.filterAndTrim(title, Arrays.asList("[", "]", ";", "%", "$", "@", "#", "(", ")")).replace("-", " ");
                        keyword = StringUtils.filterAndTrim(keyword, Arrays.asList("[", "]", ";", "%", "$", "@", "#", "(", ")")).replace("-", " ");

                        //求相似度
                        float mc = StringUtils.wordMatchD(title, keyword);

                        if (mc > 0.6) {
                            resultMap.put(listProduct, mc);
                        } else {
                            logger.debug("float mc small than 0.6");
                        }
                    }
                }

                //获取title相似度最高的
                float maxMc = 0;
                ListProduct listProduct = null;

                logger.debug("resultMap size" + resultMap.size() + " for [" + sku.getId() + "]");

                if (resultMap.size() != 0) {
                    for (Map.Entry<ListProduct, Float> resultMapEntry : resultMap.entrySet()) {
                        if (resultMapEntry.getValue() > maxMc) {
                            maxMc = resultMapEntry.getValue();
                            listProduct = resultMapEntry.getKey();
                        }
                    }
                }

                if (listProduct != null) {

                    ProductStatus status = listProduct.getStatus();
                    if (ProductStatus.OFFSALE.equals(status)) {
                        throw new RuntimeException("fetch get offsale product");
                    } else {
                        log.setNewUrl(sku.getUrl());
                        if (ProductStatus.OUTSTOCK.equals(status)) {
                            log.setNewStatus(SkuStatus.OUTSTOCK);
                        }
                        if (ProductStatus.ONSALE.equals(status)) {
                            log.setNewStatus(SkuStatus.ONSALE);
                        }
                    }

                    cmpSkuService.updateCmpSku(sku.getId(), listProduct.getUrl(), listProduct.getPrice(), log.getNewStatus());

                    dataFixService.updateFixLog(log);

                    logger.debug("keyword:" + keyword);
                    logger.debug("prodcut title:" + listProduct.getTitle());
                    logger.debug("update " + sku.getId());
                }

            } catch (Exception e) {
                logger.debug("listProcessor [" + keyword + "] fail +" + e.toString());
            }
        }
    }
}
