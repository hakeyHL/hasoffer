package hasoffer.core.product;

import hasoffer.core.persistence.po.ptm.PtmStdImage;

import java.util.List;

/**
 * Created by hs on 2016年11月29日.
 * Time 15:05
 */
public interface IPtmStdImageService {
    List<PtmStdImage> getStdSkuImageBySkuId(Long stdSkuId);
}
