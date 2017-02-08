package hasoffer.core.third.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.product.impl.CmpSkuServiceImpl;
import hasoffer.core.system.IAppService;
import hasoffer.core.third.ThirdService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.JsonHelper;
import hasoffer.fetch.helper.WebsiteHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hs on 2016/7/4.
 */
@Service
public class ThirdServiceImple implements ThirdService {
    private static String THIRD_GMOBI_DEALS = "SELECT t from AppDeal t where t.createTime <=?0  and t.expireTime >= ?1  ";
    @Resource
    Hibernate4DataBaseManager hdm;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    CmpSkuServiceImpl cmpSkuService;
    @Resource
    IAppService appService;
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
            logger.error("has sites");
            sb.append(" and t.website=?2 ");
            sb.append(" order by createTime desc  ");
            for (int i = 0; i < sites.size(); i++) {
                List li = new ArrayList();
                Website website = Website.valueOf((String) sites.get(i));
                li.add(createTime);
                li.add(new Date());
                li.add(website);
                List<AppDeal> deals = hdm.query(sb.toString(), li);
                if (deals != null && deals.size() > 0) {
                    dataList.addAll(deals);
                }
            }
        } else {
            logger.error("no sites");
            sb.append(" order by createTime desc  ");
            List<AppDeal> deals = hdm.query(sb.toString(), Arrays.asList(createTime, new Date()));
            if (deals != null && deals.size() > 0) {
                dataList.addAll(deals);
            }
        }
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"push", "display"});
        for (AppDeal appDeal : (List<AppDeal>) dataList) {
            appDeal.setImageUrl(ImageUtil.getImageUrl(appDeal.getImageUrl()));
        }
        resJson.put("deals", dataList);
        resJson.put("errorCode", "00000");
        resJson.put("msg", "ok");
        return JSON.toJSONString(resJson, propertyFilter);
    }

    @Override
    public String getDealsForIndia(int page, int pageSize) {
        Map resultMap = new HashMap();
        Map dataMap = new HashMap();
          /* 返回数据为
                * id,deal类目名称、图片、名称、折扣值、deal的价格描述、点赞数、site、创建时间
           */
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultMap.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        //获取的是 有效的,display的,列表页图不为空的
        PageableResult<AppDeal> result = appService.getDeals(page, pageSize);
        //ArrayList,内部为数组实现,对元素快速随机访问
        List dealList = new ArrayList();
        for (AppDeal appDeal : result.getData()) {
            JSONObject dealJson = new JSONObject();
            getDealModel(appDeal, dealJson);
            dealList.add(dealJson);
        }
        dataMap.put("dealList", dealList);
        resultMap.put(ConstantUtil.API_NAME_DATA, dataMap);
        return JSON.toJSONString(resultMap);
    }

    private void getDealModel(AppDeal appDeal, JSONObject dealJson) {
        dealJson.put("id", appDeal.getId());
        dealJson.put("category", appDeal.getCategory());
        dealJson.put("imageUrl", appDeal.getListPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getListPageImage()));
        dealJson.put("title", appDeal.getTitle());
        dealJson.put("discount", appDeal.getDiscount());
        dealJson.put("priceDes", appDeal.getPriceDescription() == null ? "" : appDeal.getPriceDescription());
        dealJson.put("thumbCount", appDeal.getDealThumbNumber());
        dealJson.put("site", appDeal.getWebsite() == Website.UNKNOWN ? WebsiteHelper.getAllWebSiteString(appDeal.getLinkUrl()) : appDeal.getWebsite().name());
        dealJson.put("createTime", TimeUtils.getDifference2Date(new Date(), appDeal.getCreateTime()));
    }

    @Override
    public String getDealInfoForIndia(String id) {
        Map resultMap = new HashMap();
        Map dataMap = new HashMap();

         /* 返回数据为
                * id,deal类目名称、详情页图片、名称、折扣值、deal的价格描述、点赞数、site、创建时间,跳转链接
           */
        resultMap.put(ConstantUtil.API_NAME_DATA, dataMap);
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        if (StringUtils.isEmpty(id)) {
            resultMap.put(ConstantUtil.API_NAME_MSG, "id required");
            return JSON.toJSONString(resultMap);
        }
        Long dealId = Long.valueOf(id);
        if (dealId > 0) {
            AppDeal appDeal = appService.getDealDetail(dealId);
            if (appDeal != null) {
                JSONObject dealJson = new JSONObject();
                getDealModel(appDeal, dealJson);
                dealJson.put("imageUrl", appDeal.getInfoPageImage() == null ? "" : ImageUtil.getImageUrl(appDeal.getInfoPageImage()));
                dealJson.put("description", "");
                dealJson.put("", "");
            } else {
                resultMap.put(ConstantUtil.API_NAME_MSG, "not found this deal, with id " + id);
                return JSON.toJSONString(resultMap);
            }
        }
        resultMap.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultMap.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        return null;
    }


}
