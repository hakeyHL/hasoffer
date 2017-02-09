package hasoffer.core.cache;

import com.alibaba.fastjson.JSON;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.cache.DeviceFlowControllRecord;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.JsonHelper;
import hasoffer.data.redis.IRedisListService;
import hasoffer.spider.constants.RedisKeysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Date : 2016/5/7
 * Function :
 */
@Component
public class CmpSkuCacheManager {

    private static final String CACHE_KEY_PRE = "CmpSku_";
    private static final long CACHE_EXPIRE_TIME = TimeUtils.SECONDS_OF_1_HOUR;
    @Resource
    ICacheService<PtmCmpSku> cacheService;

    @Resource
    ICacheService<Map> skuCacheServiceMap;

    @Resource
    IRedisListService redisListService;

    @Resource
    ICmpSkuService cmpSkuService;

    private Logger logger = LoggerFactory.getLogger(CmpSkuCacheManager.class);

    /**
     * cmpsku 的 缓存
     * todo 每次更新cmpsku的时候要更新缓存
     *
     * @param id
     * @return
     */
    public PtmCmpSku getCmpSkuById(long id) {

        String key = CACHE_KEY_PRE + "_getCmpSkuById_" + id;

        PtmCmpSku cmpSku = cacheService.get(PtmCmpSku.class, key, 0);

        if (cmpSku == null) {
            cmpSku = cmpSkuService.getCmpSkuById(id);
            if (cmpSku != null) {
                cacheService.add(key, cmpSku, CACHE_EXPIRE_TIME);
            }
        }

        return cmpSku;
    }

    public List<PtmCmpSku> listCmpSkus(long productId, SkuStatus skuStatus) {
        String key = ConstantUtil.API_PREFIX_CACAHE_CMP_CMPLIST_ + "statusCmpSkus_" + String.valueOf(productId) + skuStatus.name();

        String cmpSkusJson = cacheService.get(key, 0);

        //先不读缓存,也不存缓存
        List<PtmCmpSku> cmpSkus = new ArrayList<PtmCmpSku>();
        try {
            if (StringUtils.isEmpty(cmpSkusJson)) {
                cmpSkus = cmpSkuService.listCmpSkus(productId, skuStatus);

                System.out.println("--------- listCmpSkus  -----------" + cmpSkus.size());

                if (ArrayUtils.hasObjs(cmpSkus)) {
                    cacheService.add(key, JSONUtil.toJSON(cmpSkus), TimeUtils.SECONDS_OF_1_HOUR * 4);
                }
            } else {
                cmpSkus = JsonHelper.toList(cmpSkusJson, PtmCmpSku.class);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            return cmpSkus;
        }
    }

    /**
     * cmpsku的索引
     * todo 更新sku时要更新
     *
     * @param cliSite
     * @param sourceId
     * @param keyword
     * @return
     */
    public PtmCmpSkuIndex2 getCmpSkuIndex2(String deviceId, Website cliSite, String sourceId, String keyword) {
        boolean isCtrled = isFlowControlled(deviceId, cliSite);
        if (isCtrled) {
            return null;
        }

        String key = CACHE_KEY_PRE + "_getCmpSkuIndex_" + cliSite.name() + "_" + sourceId + "_" + keyword;

        String cmpSkuIndexJson = cacheService.get(key, 0);

        PtmCmpSkuIndex2 cmpSkuIndex = null;
        try {

            if (StringUtils.isEmpty(cmpSkuIndexJson)) {
                cmpSkuIndex = cmpSkuService.getCmpSkuIndex2(cliSite, sourceId, keyword);
                if (cmpSkuIndex == null) {
                    cmpSkuIndex = new PtmCmpSkuIndex2();
                }
                cacheService.add(key, JSONUtil.toJSON(cmpSkuIndex), CACHE_EXPIRE_TIME);
                return cmpSkuIndex;
            } else {
                cmpSkuIndex = JSONUtil.toObject(cmpSkuIndexJson, PtmCmpSkuIndex2.class);
                return cmpSkuIndex;
            }

        } catch (Exception e) {
            logger.debug(e.getMessage());
            cmpSkuIndex = null;
        }

        return cmpSkuIndex;
    }

    /**
     * @param deviceId
     * @param cliSite
     * @return
     */
    public boolean isFlowControlled(String deviceId, Website cliSite) {

        String key = CACHE_KEY_PRE + "flow_controlled_" + deviceId + "_" + cliSite.name();

        String deviceFlowControllRecord = cacheService.get(key, 0);

        if (StringUtils.isEmpty(deviceFlowControllRecord)) {
            return false;
        } else {
            return true;
        }
    }

    public void recordFlowControll(String deviceId, Website cliSite) {

        String key = CACHE_KEY_PRE + "flow_controlled_" + deviceId + "_" + cliSite.name();

        String deviceFlowControllRecord = cacheService.get(key, 0);

        if (StringUtils.isEmpty(deviceFlowControllRecord)) {
            cacheService.add(key, JSONUtil.toJSON(new DeviceFlowControllRecord(deviceId, cliSite.name(), true)), TimeUtils.SECONDS_OF_1_DAY);
        }
    }

    public List<PtmCmpSku> getCmpSkusBySiteAndPrice(float price, Website website, Long productId) {
        List<PtmCmpSku> ptmCmpSkus = null;
        String key = CACHE_KEY_PRE + "getSkuByPriceAndSite" + price + "_" + website.name();
        String s = null;
        s = skuCacheServiceMap.get(key, 0);
//        ptmCmpSkus = cmpSkuService.getCmpSkusBySiteAndPrice(Float.valueOf(price + ConstantUtil.API_DATA_EMPTYSTRING), website, productId);
        if (StringUtils.isEmpty(s)) {
            System.out.println(" get productId porducdtId : " + productId);
            ptmCmpSkus = cmpSkuService.getCmpSkusBySiteAndPrice(Float.valueOf(price + ConstantUtil.API_DATA_EMPTYSTRING), website, productId);
            System.out.println(" ptmCmpskus : prmcmpskus :" + ptmCmpSkus.size());
            Map<String, List> map = new HashMap();
            map.put(key, ptmCmpSkus);
            skuCacheServiceMap.add(key, JSONUtil.toJSON(map), TimeUtils.SECONDS_OF_1_HOUR * 4);
        } else {
            System.out.println("getget");
            try {
                Map map = JSONUtil.toObject(s, Map.class);
                List<LinkedHashMap> li = (List) map.get(key);
                ptmCmpSkus = new ArrayList<>();
                for (LinkedHashMap linkedHashMap1 : li) {
                    String string = JSON.toJSONString(linkedHashMap1);
                    PtmCmpSku ptmCmpSku = JSONUtil.toObject(string, PtmCmpSku.class);
                    ptmCmpSkus.add(ptmCmpSku);
                }
            } catch (Exception e) {
                System.out.println("getCmpSkusBySiteAndPrice  get skus by siteAndPrice exception " + e.getMessage());
            }
        }
        return ptmCmpSkus;
    }

    public void push2failedUpdate(PtmCmpSku cmpSku) {
        String key = CACHE_KEY_PRE + "FailedUpdate";

        if (cacheService.exists(key)) {
            cacheService.expire(key, TimeUtils.SECONDS_OF_1_HOUR * 2);
        }

        redisListService.push(key, String.valueOf(cmpSku.getId()), RedisKeysUtils.DEFAULT_EXPIRE_TIME);
    }

    public List getFailedUpdate(int start, int count) {
        String key = CACHE_KEY_PRE + "FailedUpdate";
        return redisListService.range(key, start, start + count);
    }
}
