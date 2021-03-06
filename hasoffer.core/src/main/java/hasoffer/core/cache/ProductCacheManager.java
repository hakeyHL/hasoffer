package hasoffer.core.cache;

import com.alibaba.fastjson.JSONArray;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.IPtmStdImageService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.IRedisSetService;
import hasoffer.spider.constants.RedisKeysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Date : 2016/5/7
 * Function :
 */
@Component
public class ProductCacheManager {

    private static final Class CACHE_CLASS = PtmProduct.class;
    private static final String CACHE_KEY_PRE = "API_PRODUCT_";
    private static final long CACHE_EXPIRE_TIME = TimeUtils.SECONDS_OF_1_DAY;

    @Resource
    ICacheService<PtmProduct> cacheService;
    @Resource
    CmpSkuCacheManager skuCacheService;
    @Resource
    IProductService productService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IRedisListService redisListService;
    @Resource
    IRedisSetService redisSetService;
    @Resource
    AppCacheService appCacheService;
    Logger logger = LoggerFactory.getLogger(ProductCacheManager.class);
    @Resource
    private IPtmStdImageService stdImageService;

    public static void main(String[] args) {
        BigDecimal a = BigDecimal.valueOf(10);
        String price = a + ConstantUtil.API_DATA_EMPTYSTRING;
        System.out.println(price);
        float floatAblePrice = Float.valueOf(price);
        System.out.println(floatAblePrice);
    }

    /**
     * 根据商品ID查询商品
     *
     * @param id
     * @return
     */
    public PtmProduct getProduct(long id) {
        String key = CACHE_KEY_PRE + id;

        PtmProduct product = cacheService.get(CACHE_CLASS, key, 0);

        if (product == null) {
            product = productService.getProduct(id);
            if (product != null) {
                cacheService.add(key, product, CACHE_EXPIRE_TIME);
            }
        }

        return product;
    }

    /**
     * 根据商品ID查询商品主图
     *
     * @param id
     * @return
     */
    public String getProductMasterImageUrl(long id) {
        String key = CACHE_KEY_PRE + "_getProductMasterImageUrl_" + id;

        String imageUrl = cacheService.get(key, 0);
        if (imageUrl == null) {
            imageUrl = productService.getProductMasterImageUrl(id);
            if (imageUrl != null) {
                cacheService.add(key, imageUrl, CACHE_EXPIRE_TIME);
            }
        }

        return imageUrl;
    }

    public String getPtmStdSkuImageUrl(long id) {
        String key = CACHE_KEY_PRE + "_getPtmStdSkuImageUrl_" + id;
        String imageUrl = cacheService.get(key, 0);
        if (imageUrl == null) {
            List<PtmStdImage> imageList = stdImageService.getStdSkuImageBySkuId(id);
            if (imageList != null && imageList.size() > 0) {
                imageUrl = ImageUtil.getImageUrl(imageList.get(0).getSmallImagePath());
            }
            if (imageUrl != null) {
                cacheService.add(key, imageUrl, CACHE_EXPIRE_TIME);
            }
        }

        return imageUrl;
    }

    /**
     * 获取ptmStdPrice的图片
     * 默认是取小图,如果没有则返回商品的小图
     *
     * @param ptmStdPrice
     * @return
     */
    public String getPtmStdPriceImageUrl(PtmStdPrice ptmStdPrice, boolean getBigImage) {
        String key = CACHE_KEY_PRE + "getPtmStdPriceImageUrl_" + ptmStdPrice.getId();
        if (getBigImage) {
            key = CACHE_KEY_PRE + "big_getPtmStdPriceImageUrl_" + ptmStdPrice.getId();
        }
        String imageUrl = cacheService.get(key, 0);
        List<PtmStdImage> imageList;
        if (org.apache.commons.lang3.StringUtils.isEmpty(imageUrl)) {
            imageList = stdImageService.getStdPriceImageByPriceId(ptmStdPrice.getId());
            if (imageList != null && imageList.size() > 0) {
                imageUrl = ImageUtil.getImageUrl(imageList.get(0).getSmallImagePath());
                if (getBigImage) {
                    imageUrl = ImageUtil.getImageUrl(imageList.get(0).getBigImagePath());
                }
            }
            if (imageUrl != null) {
                cacheService.add(key, imageUrl, CACHE_EXPIRE_TIME);
            }
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(imageUrl)) {
            key = CACHE_KEY_PRE + "getPtmStdPriceImageUrl_SKUID_" + ptmStdPrice.getStdSkuId();
            if (getBigImage) {
                key = CACHE_KEY_PRE + "big_getPtmStdPriceImageUrl_SKUID_" + ptmStdPrice.getStdSkuId();
            }
            imageUrl = cacheService.get(key, 0);
            if (org.apache.commons.lang3.StringUtils.isEmpty(imageUrl)) {
                imageList = stdImageService.getStdSkuImageBySkuId(ptmStdPrice.getStdSkuId());
                if (imageList != null && imageList.size() > 0) {
                    imageUrl = ImageUtil.getImageUrl(imageList.get(0).getSmallImagePath());
                    if (getBigImage) {
                        imageUrl = ImageUtil.getImageUrl(imageList.get(0).getBigImagePath());
                    }
                }
                if (imageUrl != null) {
                    cacheService.add(key, imageUrl, CACHE_EXPIRE_TIME);
                }
            }
        }
        return imageUrl;
    }

    /**
     * 根据商品ID，分页查询关联的比价sku
     * todo sku有价格更新时，需要更新其关联的比价列表
     *
     * @param proId
     * @param page
     * @param size
     * @return
     */
    public PageableResult<PtmCmpSku> listPagedCmpSkus(long proId, int page, int size) {
        String key = ConstantUtil.API_PREFIX_CACAHE_CMP_CMPLIST_ + proId + "_" + page + "_" + size;
        String cmpSkusJson = cacheService.get(key, 0);
        //先不读缓存,也不存缓存
        PageableResult<PtmCmpSku> pagedCmpskus;
        if (StringUtils.isEmpty(cmpSkusJson)) {
            try {
                pagedCmpskus = productService.listOnsaleCmpSkus(proId, page, size);
                List<PtmCmpSku> data = pagedCmpskus.getData();
                if (data != null && data.size() > 0) {
                    cacheService.add(key, JSONArray.toJSONString(ApiUtils.getIdList(pagedCmpskus.getData())), TimeUtils.SECONDS_OF_1_HOUR * 8);
                } else {
                    pagedCmpskus = new PageableResult<>();
                    pagedCmpskus.setData(new ArrayList<PtmCmpSku>());
                }
            } catch (Exception e) {
                logger.error("listPagedCmpSkus(): listOnsaleCmpSkus error.", e);
                return null;
            }
        } else {
            try {
                pagedCmpskus = new PageableResult<>();
                List cmpSkuList = appCacheService.getObjectListFromCache(new PtmCmpSku(), cmpSkusJson, 0);
                pagedCmpskus.setData(cmpSkuList);
            } catch (Exception e) {
                logger.error("listPagedCmpSkus(): getObjectListFromCache error. cmpSkusJson:{}", cmpSkusJson, e);
                return null;
            }
        }
        return pagedCmpskus;
    }

    public List<PtmProduct> getTopSellins(int page, int size) {
        String key = CACHE_KEY_PRE + "_listPagedCmpSkus_TopSelling" + "_" + page + "_" + size;
        String ptmProductJson = cacheService.get(key, 0);
        List<PtmProduct> products = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(ptmProductJson)) {
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                calendar.setTime(date);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 00, 00, 00);
                long todayStart = calendar.getTimeInMillis();
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 1, 00, 00, 00);
                long yesterdayStart = calendar.getTimeInMillis();
                List<PtmTopSelling> ptmTopSellings = productService.getTopSellings(yesterdayStart, todayStart, page, size);
                for (PtmTopSelling ptmTopSelling : ptmTopSellings) {
                    PageableResult<PtmCmpSku> cmpSkuList = listPagedCmpSkus(ptmTopSelling.getId(), page, size);
                    if (cmpSkuList != null && cmpSkuList.getData().size() > 0) {
                        PtmProduct product = productService.getProduct(ptmTopSelling.getId());
                        if (product != null && product.getPrice() > 0) {
                            products.add(product);
                        }
                    }
                }
                if (products != null && products.size() > 0) {
                    //处理下products,为id列表
                    cacheService.add(key, JSONArray.toJSONString(ApiUtils.getIdList(products)), TimeUtils.SECONDS_OF_1_HOUR * 8);
                }
            } else {
                products = appCacheService.getObjectListFromCache(new PtmProduct(), ptmProductJson, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    public PageableResult<PtmCmpSku> listCmpSkus(long proId, int page, int size) {
        String key = ConstantUtil.API_PREFIX_CACAHE_CMP_CMPLIST_ + proId + "_" + page + "_" + size;
//        String key = CACHE_KEY_PRE + "_listCmpSkus_" + String.valueOf(proId) + "_" + page + "_" + size;
        String cmpSkusJson = cacheService.get(key, 0);
        PageableResult<PtmCmpSku> pagedCmpskus = null;
        try {
            if (StringUtils.isEmpty(cmpSkusJson)) {
                pagedCmpskus = productService.listNotOffSaleCmpSkus(proId, page, size);
                if (pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
                    cacheService.add(key, JSONArray.toJSONString(ApiUtils.getIdList(pagedCmpskus.getData())), TimeUtils.SECONDS_OF_1_HOUR * 8);
                }
            } else {
//                pagedCmpskus = ApiUtils.setPtmCmpSkuPageableResult(cmpSkusJson);
                pagedCmpskus = new PageableResult<>();
                List cmpSkuList = appCacheService.getObjectListFromCache(new PtmCmpSku(), cmpSkusJson, 0);
                pagedCmpskus.setData(cmpSkuList);
            }
        } catch (Exception e) {
            logger.error("deal skus from cache error {}", e.getMessage(), e);
            return null;
        }
        return pagedCmpskus;
    }

    //现在前台不返回offsale的sku,此方法暂时停用,且此方法查询效率低
    public List<PtmCmpSku> getOnsaleSkuList(List data, long productId) {
//        System.out.println("get productId is : " + productId);
        List<PtmCmpSku> tempPtmCmpSkus = new ArrayList<>();
        int i = 0;
        for (Object object : data) {
//            System.out.println(" get onsale ptmcmpsku : i " + i);
            JSONArray jsonArray = JSONArray.parseArray(JSONArray.toJSONString(object));
            String website = (String) jsonArray.get(0);
            String price = jsonArray.get(1) + ConstantUtil.API_DATA_EMPTYSTRING;
            //根据price和site定位需要的sku
            List<PtmCmpSku> cmpSkus = skuCacheService.getCmpSkusBySiteAndPrice(Float.valueOf(price), Website.valueOf(website), productId);
            if (cmpSkus != null) {
                //TODO  优选选择onsale的sku,否则返回outstock的sku
                PtmCmpSku onsaleSku = getOnsaleSku(cmpSkus, productId);
                if (onsaleSku != null && onsaleSku.getProductId() == productId) {
                    tempPtmCmpSkus.add(onsaleSku);
                    System.out.println("title " + onsaleSku.getTitle() + " price " + onsaleSku.getPrice() + " productId  " + onsaleSku.getProductId());
                }
            }
            i++;
        }
        return tempPtmCmpSkus;
    }

    public PtmCmpSku getOnsaleSku(List<PtmCmpSku> ptmCmpSkus, long ProductId) {
        PtmCmpSku OnsalePtmCmpSku = null;
        PtmCmpSku OutStockPtmCmpSku = null;
        for (PtmCmpSku ptmCmpSku : ptmCmpSkus) {
            if (ptmCmpSku.getStatus().name().equals("ONSALE") && ptmCmpSku.getProductId() == ProductId) {
                OnsalePtmCmpSku = ptmCmpSku;
                break;
            } else {
                OutStockPtmCmpSku = ptmCmpSku;
            }
        }
        return OnsalePtmCmpSku == null ? OutStockPtmCmpSku : OnsalePtmCmpSku;
    }

    public void put2UpdateQueue(long productId) {

        String ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);

        String key_updated = CACHE_KEY_PRE + "UPDATE_PROCESSED_" + ymd;
        String key_added = CACHE_KEY_PRE + "ADDED_" + ymd;
        String key = CACHE_KEY_PRE + "WAIT_4_UPDATE_" + ymd;

        // 如果处理过，那就不用加入到队列
//        if (redisSetService.contains(key_updated, String.valueOf(productId))) {
//            return;
//        }

        // 如果没有添加过，就再次加入
//        if (!redisSetService.contains(key_added, String.valueOf(productId))) {
//            redisSetService.add(key_added, String.valueOf(productId));
//            redisListService.push(key, String.valueOf(productId));
//        }

        boolean addFlag = redisSetService.contains(key_added, String.valueOf(productId));
        logger.info("put2UpdateQueue addFlag " + addFlag + "_" + productId);
        if (!addFlag) {

            long add = redisSetService.add(key_added, RedisKeysUtils.DEFAULT_EXPIRE_TIME, String.valueOf(productId));
            logger.info("put2UpdateQueue add to added queue success " + add + "_" + productId);

            redisListService.push(key, String.valueOf(productId), RedisKeysUtils.DEFAULT_EXPIRE_TIME);
            logger.info("put2UpdateQueue push to wait4update queue success ");
            logger.info("put2UpdateQueue push to queue " + key + "_" + productId);
        }
    }

    public void put2UpdateProcessedSet(long productId, String ymd) {
        //String ymd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);

        String key_updated = CACHE_KEY_PRE + "UPDATE_PROCESSED_" + ymd;

        redisSetService.add(key_updated, RedisKeysUtils.DEFAULT_EXPIRE_TIME, String.valueOf(productId));
    }

    public long getWait4UpdateProductCount(String ymd) {
        String key_updated = CACHE_KEY_PRE + "WAIT_4_UPDATE_" + ymd;
        return redisListService.size(key_updated);
    }

    public long getUpdateProcessdProductCount(String ymd) {
        String key_updated = CACHE_KEY_PRE + "UPDATE_PROCESSED_" + ymd;
        return redisSetService.size(key_updated);
    }


}
