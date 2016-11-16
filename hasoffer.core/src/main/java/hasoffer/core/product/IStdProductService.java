package hasoffer.core.product;

import hasoffer.core.bo.stdsku.StdSkuBo;

/**
 * Created by chevy on 2016/8/12.
 */
public interface IStdProductService {

    boolean createStdSku(StdSkuBo skuBo);

//    PtmStdProduct createStd(Map<String, FlipkartSkuInfo> skuInfoMap);
//
//    Map<String, FlipkartSkuInfo> searchSku(String keyword) throws Exception;

}
