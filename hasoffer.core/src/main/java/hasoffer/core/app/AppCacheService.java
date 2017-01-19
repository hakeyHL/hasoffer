package hasoffer.core.app;

import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSku;

import java.util.List;

/**
 * Created by hs on 2016年12月13日.
 * Time 11:48
 */
public interface AppCacheService {
    String getCacheValueByKey(String key);

    String getCacheValueByKey(String key, long seconds);

    boolean addStringCache(String key, String value, long seconds);

    boolean addObjectCache(String key, Object object, long seconds);

    List getObjectListFromCache(Object object, String idArrayString, long seconds);

    PtmStdSku getPtmStdSku(long ptmStdSkuId, int... operateType);

    PtmStdPrice getPtmStdPrice(long ptmStdPriceId, int... operateType);

    PtmProduct getPtmProduct(long ptmProductId, int... operateType);

    PtmCmpSku getPtmCmpSku(long ptmCmpSkuId, int... operateType);

    void removePtmStdSkuAndPricesCache(long ptmStdSkuId);

    void removePtmProductAndSkusCache(long ptmProductId);

}
