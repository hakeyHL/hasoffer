package hasoffer.spider.result.service.impl;

import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.spider.result.api.ISkuResultDubboService;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

public class SkuResultDubboServiceImpl implements ISkuResultDubboService {

    private static final Logger logger = LoggerFactory.getLogger(SkuResultDubboServiceImpl.class);

    private int i;

    @Resource
    ICmpSkuService cmpSkuService;

    @Override
    public void updateSku(FetchedProduct fetchedProduct) {

        long skuId = fetchedProduct.getSkuId();
        PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(skuId);

        logger.info(fetchedProduct.getWebsite() + " " + (i++) + ":" + fetchedProduct.toString().replace("\r", ""));

        try {
            //更新ptmcmpsku
            cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuId, fetchedProduct);
        } catch (Exception e) {
            logger.error("updateCmpSkuBySpiderFetchedProduct:{}", e);
        }

        try {
            //多图
            cmpSkuService.createPtmCmpSkuImage(skuId, fetchedProduct);
        } catch (Exception e) {
            logger.error("createPtmCmpSkuImage:{}", e);
        }

        try {
            //描述
            cmpSkuService.createDescription(ptmCmpSku, fetchedProduct);
        } catch (Exception e) {
            logger.error("createDescription:{}", e);
        }
    }
}
