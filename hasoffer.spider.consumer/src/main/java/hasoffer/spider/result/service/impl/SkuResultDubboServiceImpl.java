package hasoffer.spider.result.service.impl;

import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.spider.result.api.ISkuResultDubboService;
import hasoffer.spider.model.FetchedProduct;

import javax.annotation.Resource;

public class SkuResultDubboServiceImpl implements ISkuResultDubboService {

    @Resource
    ICmpSkuService cmpSkuService;

    @Override
    public void updateSku(FetchedProduct fetchedProduct) {
        System.out.println(fetchedProduct.getTitle() + ":" + fetchedProduct.getPrice());

        long skuId = fetchedProduct.getSkuId();
        PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(skuId);

        //更新ptmcmpsku
        cmpSkuService.updateCmpSkuBySpiderFetchedProduct(skuId, fetchedProduct);
        //多图
        cmpSkuService.createPtmCmpSkuImage(skuId, fetchedProduct);
        //描述
        cmpSkuService.createDescription(ptmCmpSku, fetchedProduct);
    }
}
