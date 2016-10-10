package hasoffer.spider.result.service.impl;

import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.dubbo.spider.result.api.ISkuResultDubboService;
import hasoffer.spider.model.FetchedProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Transactional
public class SkuResultDubboServiceImpl implements ISkuResultDubboService {

    private static final Logger logger = LoggerFactory.getLogger(SkuResultDubboServiceImpl.class);
    private static final Logger spiderLogger = LoggerFactory.getLogger("spider.sku.success");
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IDataBaseManager dbm;

    @Override
    public void updateSku(FetchedProduct fetchedProduct) {

        long skuId = fetchedProduct.getSkuId();
        PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(skuId);

        spiderLogger.info("update sku:{}, detail:{}", skuId, fetchedProduct.toString().replace("\r", ""));

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

//        try {
//            //描述
//            cmpSkuService.createDescription(ptmCmpSku, fetchedProduct);
//        } catch (Exception e) {
//            logger.error("createDescription:{}", e);
//        }

        try {

            PtmProduct ptmProduct = dbm.get(PtmProduct.class, ptmCmpSku.getProductId());

            if (ptmProduct != null) {

                //保存sku的描述信息
                cmpSkuService.createSkuDescription(ptmCmpSku, fetchedProduct);

                String productTitle = ptmProduct.getTitle();

                if (StringUtils.isEqual(productTitle, ptmCmpSku.getTitle())) {
                    //保存product的描述信息
                    cmpSkuService.createProductDescription(ptmCmpSku, fetchedProduct);
                    System.out.println("update product spec success for " + ptmProduct.getId());
                } else {
                    System.out.println("product spec should remove " + ptmProduct.getId());
                }
            } else {
                System.out.println(skuId + " product is null");
            }
        } catch (Exception e) {
            logger.info("createDescription fail " + skuId);
        }
    }
}
