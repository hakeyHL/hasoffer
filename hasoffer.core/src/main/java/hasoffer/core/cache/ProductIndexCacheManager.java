package hasoffer.core.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.core.redis.ICacheService;
import jodd.util.NameValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by chevy on 2016/9/13.
 */
public class ProductIndexCacheManager {
    private final String SOLR_CACHE_KEY = "SOLR_";
    Logger logger = LoggerFactory.getLogger(ProductIndexCacheManager.class);
    @Resource
    ICacheService cacheService;

    @Resource
    ProductIndex2ServiceImpl productIndex2Service;

    /**
     * 根据关键词搜索
     *
     * @param sc
     * @return
     */
    public PageableResult<ProductModel2> searchProducts(SearchCriteria sc) {
        String key = this.getSearchCacheKey(sc);
        PageableResult<ProductModel2> model2PageableResult = null;
        String pageableResultString = cacheService.get(key, 0);
        if (pageableResultString != null) {
            try {
                JSONObject jsonObject = JSON.parseObject(pageableResultString);
                Map<String, JSONArray> pivotFieldVals = jsonObject.getObject("pivotFieldVals", Map.class);
                Map<String, List<NameValue>> pivotFieldVals2 = new HashMap<String, List<NameValue>>();
                Set<Map.Entry<String, JSONArray>> entries = pivotFieldVals.entrySet();
                Iterator<Map.Entry<String, JSONArray>> iterator = entries.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, JSONArray> next = iterator.next();
                    pivotFieldVals2.put(next.getKey(), JSONArray.parseArray(next.getValue().toJSONString(), NameValue.class));
                }
                model2PageableResult = new PageableResult<ProductModel2>(JSONArray.parseArray(jsonObject.getString("data"), ProductModel2.class), jsonObject.getLong("numFund"), jsonObject.getLong("currentPage"), jsonObject.getLong("pageSize"), pivotFieldVals2);
                return model2PageableResult;
            } catch (Exception e) {
                logger.error(" search products , get products from cache :" + e.getMessage());
                return null;
            }
        } else {
            return productIndex2Service.searchProducts(sc);
        }
    }

    public String getSearchCacheKey(SearchCriteria sc) {
        StringBuilder key = new StringBuilder();
        key.append(SOLR_CACHE_KEY);
        if (!StringUtils.isEmpty(sc.getKeyword())) {
            key.append("_").append(sc.getKeyword());
        }
        if (!StringUtils.isEmpty(sc.getCategoryId())) {
            key.append("_").append(sc.getCategoryId());
        }
        if (!StringUtils.isEmpty(sc.getLevel() + "")) {
            key.append("_").append(sc.getLevel());
        }
        if (sc.getPriceFrom() != -1) {
            key.append("_").append(sc.getPriceFrom());
        }
        if (sc.getPriceTo() != -1) {
            key.append("_").append(sc.getPriceTo());
        }
        if (sc.getPivotFields() != null && sc.getPivotFields().size() > 0) {
            for (String pivotField : sc.getPivotFields()) {
                key.append("_").append(pivotField);
            }

        }
        if (sc.getSort() != null) {
            key.append("_").append(sc.getSort());
        }
        key.append("_").append(sc.getPage()).append("_").append(sc.getPageSize());
        return key.toString();
    }

}
