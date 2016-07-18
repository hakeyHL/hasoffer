package hasoffer.core.third.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.base.model.Website;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.product.iml.CmpSkuServiceImpl;
import hasoffer.core.third.ThirdService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by hs on 2016/7/4.
 */
@Service
public class ThirdServiceImple implements ThirdService {
    private static String THIRD_GMOBI_DEALS = "SELECT t from AppDeal t where t.createTime <=?0  and t.expireTime >= ?0  ";
    @Resource
    Hibernate4DataBaseManager hdm;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    CmpSkuServiceImpl cmpSkuService;
    Logger logger = LoggerFactory.getLogger(ThirdServiceImple.class);

    @Override
    public String getDeals(String acceptJson) {
        JSONObject resJson = new JSONObject();
        StringBuilder sb = new StringBuilder();
        sb.append(THIRD_GMOBI_DEALS);
        if (StringUtils.isEmpty(acceptJson)) {
            logger.error(String.format("json parseException , %s is not a json String", acceptJson));
            resJson.put("errorCode", "10001");
            resJson.put("msg", "you should send a json String ,start with '{' and end with '}' ");
            return resJson.toJSONString();
        }
        JSONObject jsonObject = JSONObject.parseObject(acceptJson);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date createTime = new Date();
        try {
            if (StringUtils.isNotEmpty(jsonObject.getString("createTime"))) {
                createTime = sf.parse(jsonObject.getString("createTime"));
            }
        } catch (ParseException e) {
            logger.error("dataFormat  " + jsonObject.getString("createTime") + " to format yyyyMMddHHmmss failed ");
            resJson.put("errorCode", "10001");
            resJson.put("msg", "can't parse your createTime " + jsonObject.getString("createTime") + "  , because it is not the pattern as yyyyMMddHHmmss ");
            return resJson.toJSONString();
        }
        JSONArray sites = null;
        try {
            sites = jsonObject.getJSONArray("sites");
        } catch (Exception e) {
            logger.error(" sites is not a JsonArray String ");
            resJson.put("errorCode", "10001");
            resJson.put("msg", "required a Array like [\"a\",\"b\"] ");
            return resJson.toJSONString();
        }
        List dataList = new ArrayList();
        if (sites != null) {
            sb.append(" and t.website=?1 ");
            sb.append(" order by createTime desc  ");
            for (int i = 0; i < sites.size(); i++) {
                List li = new ArrayList();
                Website website = Website.valueOf((String) sites.get(i));
                li.add(createTime);
                li.add(website);
                List<AppDeal> deals = hdm.query(sb.toString(), li);
                if (deals != null && deals.size() > 0) {
                    dataList.addAll(deals);
                }
            }
        } else {
            sb.append(" order by createTime desc  ");
            List<AppDeal> deals = hdm.query(sb.toString(), Arrays.asList(createTime));
            if (deals != null && deals.size() > 0) {
                dataList.addAll(deals);
            }
        }
        //add topSelling to list
//        List<PtmProduct> products = productCacheManager.getTopSellingProductsByDate(new SimpleDateFormat("yyyyMMdd").format(createTime), 1, 20);
//        for (PtmProduct product : products) {
//            int count = cmpSkuService.getSkuSoldStoreNum(product.getId());
//            if (count > 0) {
//                AppDeal appDeal = new AppDeal();
//                appDeal.setTitle(product.getTitle());
//                appDeal.setWebsite(Website.valueOf(product.getSourceSite()));
//                appDeal.setCreateTime(product.getCreateTime());
//                appDeal.setDescription(product.getDescription());
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(createTime);
//                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + 2, 0, 0, 0);
//                appDeal.setExpireTime(calendar.getTime());
//                appDeal.setId(product.getId());
//                appDeal.setImageUrl(productCacheManager.getProductMasterImageUrl(product.getId()));
//                appDeal.setLinkUrl(product.getSourceUrl() == null ? "" : product.getSourceUrl());
//                dataList.add(appDeal);
//            }
//        }
        PropertyFilter propertyFilter = new PropertyFilter() {
            @Override
            public boolean apply(Object o, String s, Object o1) {
                if (s.equals("push")) {
                    return false;
                }
                return true;
            }
        };
        resJson.put("deals", dataList);
        resJson.put("errorCode", "00000");
        resJson.put("msg", "ok");
        return JSON.toJSONString(resJson, propertyFilter);
    }


}
