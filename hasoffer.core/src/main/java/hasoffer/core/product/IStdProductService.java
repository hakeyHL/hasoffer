package hasoffer.core.product;

import hasoffer.affiliate.model.FlipkartSkuInfo;

import java.util.Map;

/**
 * Created by chevy on 2016/8/12.
 */
public interface IStdProductService {

    void createStd(Map<String, FlipkartSkuInfo> skuInfoMap);

    Map<String, FlipkartSkuInfo> searchSku(String keyword);

}
