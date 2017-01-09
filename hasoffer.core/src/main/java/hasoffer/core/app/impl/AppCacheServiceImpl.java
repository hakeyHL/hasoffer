package hasoffer.core.app.impl;

import com.alibaba.fastjson.JSON;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016年12月13日.
 * Time 11:49
 */
@Service
public class AppCacheServiceImpl implements AppCacheService {
    @Autowired
    ICacheService iCacheService;
    @Resource
    IDataBaseManager dbm;

    @Override
    public String getCacheValueByKey(String key) {
        return iCacheService.get(key, 0);
    }

    @Override
    public String getCacheValueByKey(String key, long seconds) {
        return iCacheService.get(key, seconds);
    }

    @Override
    public boolean addStringCache(String key, String value, long seconds) {
        return iCacheService.add(key, value, seconds);
    }

    @Override
    public boolean addObjectCache(String key, Object value, long seconds) {
        return iCacheService.add(key, value, seconds);
    }

    /**
     * 从缓存中获取商品或者sku列表
     *
     * @param oCacheValueList
     * @param seconds
     * @return
     */
    @Override
    public List getObjectListFromCache(Object object, String oCacheValueList, long seconds) {
        List list = new ArrayList();
        //1. 从缓存中获取id列表
        List<Long> ids = JSON.parseArray(oCacheValueList, Long.class);
        if (ids == null || ids.size() < 1) {
            return list;
        }
        if (object instanceof PtmStdSku) {
            for (long id : ids) {
                list.add(getPtmStdSku(id));
            }
        }

        if (object instanceof PtmStdPrice) {
            for (long id : ids) {
                list.add(getPtmStdPrice(id));
            }
        }

        if (object instanceof PtmProduct) {
            for (long id : ids) {
                list.add(getPtmProduct(id));
            }
        }

        if (object instanceof PtmCmpSku) {
            for (long id : ids) {
                list.add(getPtmCmpSku(id));
            }
        }
        return list;
    }

    /**
     * @param ptmStdSkuId
     * @param operateType 传任意整数即为从缓存中删除此Object
     * @return
     */
    @Override
    public PtmStdSku getPtmStdSku(long ptmStdSkuId, int... operateType) {
        if (ptmStdSkuId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMSTDSKU_ + ptmStdSkuId;
        if (operateType.length > 0) {
            //删除
            iCacheService.del(cacheKey);
            return null;
        }
        PtmStdSku ptmStdSku = (PtmStdSku) iCacheService.get(PtmStdSku.class, cacheKey, 0);
        if (ptmStdSku == null) {
            ptmStdSku = dbm.get(PtmStdSku.class, ptmStdSkuId);
            if (ptmStdSku != null) {
                iCacheService.add(cacheKey, ptmStdSku, TimeUtils.MILLISECONDS_OF_1_DAY);
            }
        }
        return ptmStdSku;
    }

    /**
     * @param operateType 传任意整数即为从缓存中删除此Object
     * @return
     */
    @Override
    public PtmStdPrice getPtmStdPrice(long ptmStdPriceId, int... operateType) {
        if (ptmStdPriceId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMSTDPRICE_ + ptmStdPriceId;
        if (operateType.length > 0) {
            //删除
            iCacheService.del(cacheKey);
            return null;
        }
        PtmStdPrice ptmStdPrice = (PtmStdPrice) iCacheService.get(PtmStdPrice.class, cacheKey, 0);
        if (ptmStdPrice == null) {
            ptmStdPrice = dbm.get(PtmStdPrice.class, ptmStdPriceId);
            if (ptmStdPrice != null) {
                iCacheService.add(cacheKey, ptmStdPrice, TimeUtils.MILLISECONDS_OF_1_DAY);
            }
        }
        return ptmStdPrice;
    }

    /**
     * @param operateType 传任意整数即为从缓存中删除此Object
     * @return
     */
    @Override
    public PtmProduct getPtmProduct(long ptmProductId, int... operateType) {
        if (ptmProductId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMPRODUCT_ + ptmProductId;
        if (operateType.length > 0) {
            //删除
            iCacheService.del(cacheKey);
            return null;
        }
        PtmProduct ptmProduct = (PtmProduct) iCacheService.get(PtmProduct.class, cacheKey, 0);
        if (ptmProduct == null) {
            ptmProduct = dbm.get(PtmProduct.class, ptmProductId);
            if (ptmProduct != null) {
                iCacheService.add(cacheKey, ptmProduct, TimeUtils.MILLISECONDS_OF_1_DAY);
            }
        }
        return ptmProduct;
    }

    /**
     * @param operateType 传任意整数即为从缓存中删除此Object
     * @return
     */
    @Override
    public PtmCmpSku getPtmCmpSku(long ptmCmpSkuId, int... operateType) {
        if (ptmCmpSkuId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMSKU_ + ptmCmpSkuId;
        if (operateType.length > 0) {
            //删除
            iCacheService.del(cacheKey);
            return null;
        }
        PtmCmpSku ptmCmpSku = (PtmCmpSku) iCacheService.get(PtmCmpSku.class, cacheKey, 0);
        if (ptmCmpSku == null) {
            ptmCmpSku = dbm.get(PtmCmpSku.class, ptmCmpSkuId);
            if (ptmCmpSku != null) {
                iCacheService.add(cacheKey, ptmCmpSku, TimeUtils.MILLISECONDS_OF_1_DAY);
            }
        }
        return ptmCmpSku;
    }

}
