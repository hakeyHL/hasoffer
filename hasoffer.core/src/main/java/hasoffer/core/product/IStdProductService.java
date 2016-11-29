package hasoffer.core.product;

import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.core.bo.stdsku.StdSkuBo;

/**
 * Created by chevy on 2016/8/12.
 */
public interface IStdProductService {

    boolean createStdSku(StdSkuBo skuBo);

    StdSkuBo findStdSku(long skuId);

//    PtmStdProduct createStd(Map<String, FlipkartSkuInfo> skuInfoMap);
//
//    Map<String, FlipkartSkuInfo> searchSku(String keyword) throws Exception;

    /*
    该方法用来修复PtmStdImage的图片url
    将thumb转乘成large
     */
    void fixImage(long imageId);

    void downLoadImage(long imageId) throws ImageDownloadOrUploadException;

}
