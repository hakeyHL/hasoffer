package hasoffer.core.app.impl;

import com.alibaba.fastjson.JSON;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.impl.PtmStdPriceServiceImpl;
import hasoffer.core.product.impl.PtmStdSKuServiceImpl;
import hasoffer.core.product.solr.CmpSkuIndexServiceImpl;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Resource
    PtmStdSKuServiceImpl ptmStdSKuService;
    @Resource
    PtmStdPriceServiceImpl ptmStdPriceService;
    @Resource
    IProductService productService;
    @Resource
    CmpSkuIndexServiceImpl cmpskuIndexService;
    @Resource
    ICmpSkuService cmpSkuService;

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
     *                    当operateType [0]的值为2时会删除此商品下所有sku的缓存
     * @return
     */
    @Override
    public PtmStdSku getPtmStdSku(long ptmStdSkuId, int... operateType) {
        if (ptmStdSkuId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMSTDSKU_ + ptmStdSkuId;
        if (operateType.length > 0) {
            //重新导入
            PtmStdSku ptmStdSku = dbm.get(PtmStdSku.class, ptmStdSkuId);
            if (ptmStdSku != null) {
                ptmStdSKuService.importPtmStdSku2Solr(ptmStdSku);
            }
            if (operateType[0] == 2) {
                removePtmStdSkuAndPricesCache(ptmStdSkuId);
            } else {
                iCacheService.delKeys("*" + ptmStdSkuId + "*");
            }
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
        PtmStdPrice ptmStdPrice = (PtmStdPrice) iCacheService.get(PtmStdPrice.class, cacheKey, 0);
        if (ptmStdPrice == null) {
            ptmStdPrice = dbm.get(PtmStdPrice.class, ptmStdPriceId);
            if (ptmStdPrice != null) {
                iCacheService.add(cacheKey, ptmStdPrice, TimeUtils.MILLISECONDS_OF_1_DAY);
            }
        }
        if (operateType.length > 0) {
            //删除sku缓存时有必要和责任做如下事情
            //1.更新商品信息
            //2.商品重新导入solr
            //3. 将此sku重新导入到solr
            //4.删除此sku的缓存--最后清除
            if (ptmStdPrice != null && operateType[0] == 1) {
                ptmStdSKuService.updatePtmStdSkuPrice(ptmStdPrice.getStdSkuId());
            }
            ptmStdPriceService.importPtmStdPrice2Solr(ptmStdPrice);
            iCacheService.del(cacheKey);
            return null;
        }
        return ptmStdPrice;
    }

    /**
     * @param operateType 传任意整数即为从缓存中删除此Object
     *                    当operateType [0]的值为2时会删除此商品下所有sku的缓存
     * @return
     */
    @Override
    public PtmProduct getPtmProduct(long ptmProductId, int... operateType) {
        PtmProduct ptmProduct;
        if (ptmProductId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMPRODUCT_ + ptmProductId;
        if (operateType.length > 0) {
            //删除
            ptmProduct = dbm.get(PtmProduct.class, ptmProductId);
            if (ptmProduct != null) {
                productService.importProduct2Solr2(ptmProduct);
            }
            if (operateType[0] == 2) {
                removePtmProductAndSkusCache(ptmProductId);
            } else {
                //清除商品id的keys缓存
                iCacheService.delKeys("*" + ptmProductId + "*");
            }
            return null;
        } else {
            ptmProduct = (PtmProduct) iCacheService.get(PtmProduct.class, cacheKey, 0);
            if (ptmProduct == null) {
                ptmProduct = dbm.get(PtmProduct.class, ptmProductId);
                if (ptmProduct != null) {
                    iCacheService.add(cacheKey, ptmProduct, TimeUtils.MILLISECONDS_OF_1_DAY);
                }
            }
        }
        return ptmProduct;
    }

    /**
     * @param operateType 传任意整数即为从缓存中删除此Object
     * @param operateType 当 operateType 的数量为1 且数值为0时代表从缓存中删除此sku,此sku重新导入solr
     * @param operateType 当 operateType 的数量为1 且数值为1时代表删除此sku对应的商品的缓存信息以及更新商品信息和重新导入solr
     * @return
     */
    @Override
    public PtmCmpSku getPtmCmpSku(long ptmCmpSkuId, int... operateType) {
        if (ptmCmpSkuId < 1) {
            return null;
        }
        String cacheKey = ConstantUtil.API_PREFIX_CACAHE_PTMSKU_ + ptmCmpSkuId;
        PtmCmpSku ptmCmpSku = (PtmCmpSku) iCacheService.get(PtmCmpSku.class, cacheKey, 0);
        if (ptmCmpSku == null) {
            ptmCmpSku = dbm.get(PtmCmpSku.class, ptmCmpSkuId);
            if (ptmCmpSku != null) {
                iCacheService.add(cacheKey, ptmCmpSku, TimeUtils.MILLISECONDS_OF_1_DAY);
            }
        }
        if (operateType.length > 0) {
            //删除sku缓存时有必要和责任做如下事情
            //1.更新商品信息
            //2.商品重新导入solr
            //3. 将此sku重新导入到solr
            //4.删除此sku的缓存--最后清除
            if (ptmCmpSku != null) {
                if (ptmCmpSku.getProductId() > 0 && operateType[0] == 1) {
                    productService.updatePtmProductPrice(ptmCmpSku.getProductId());
                }
            }
            cmpskuIndexService.remove(ptmCmpSkuId + ConstantUtil.API_DATA_EMPTYSTRING);
            iCacheService.del(cacheKey);
            return null;
        }
        return ptmCmpSku;
    }

    /**
     * 清除PtmStdSku的id系列缓存以及price缓存列表
     *
     * @param ptmStdSkuId
     */
    public void removePtmStdSkuAndPricesCache(long ptmStdSkuId) {
        //1. 清除所有sku缓存
        List<Long> stdSkuIdList = dbm.query("SELECT distinct t.stdSkuId FROM PtmStdPrice t WHERE t.stdSkuId=?0", Arrays.asList(ptmStdSkuId));
        for (long stdSkuId : stdSkuIdList) {
            List<PtmStdPrice> stdPriceList = ptmStdSKuService.listStdPrice(stdSkuId);
            if (stdPriceList == null || stdPriceList.size() <= 0) {
                continue;
            }
            for (PtmStdPrice stdPrice : stdPriceList) {
                System.out.println("22 " + stdPrice.getTitle() + " " + stdPrice.getId() + " " + stdPrice.getStdSkuId());
                getPtmStdPrice(stdPrice.getId(), 0);
            }
        }
        //2. 清除商品id的keys缓存
        iCacheService.delKeys("*" + ptmStdSkuId + "*");
    }


    public void removePtmProductAndSkusCache(long ptmProductId) {
        //1. 清除所有sku缓存
        List<Long> productIdList = dbm.query("SELECT distinct t.productId FROM PtmCmpSku t WHERE t.productId = ?0", Arrays.asList(ptmProductId));
        if (productIdList != null) {
            System.out.println("get update list size:" + productIdList.size());
        }
        for (long productIdd : productIdList) {
            List<PtmCmpSku> skuList = cmpSkuService.listCmpSkus(productIdd);
            if (skuList == null || skuList.size() <= 0) {
                continue;
            }
            for (PtmCmpSku sku : skuList) {
                System.out.println("233 " + sku.getTitle() + " " + sku.getId() + " " + sku.getProductId());
                getPtmCmpSku(sku.getId(), 0);
            }
        }
        //2. 清除商品id的keys缓存
        iCacheService.delKeys("*" + ptmProductId + "*");
    }
}
