package hasoffer.core.utils.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.vo.DealVo;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmStdSkuParamGroup;
import hasoffer.core.persistence.po.ptm.PtmStdSkuParamNode;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.core.utils.ConstantUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by hs on 2016年10月19日.
 * Time 14:28
 */
@Component
public class ApiUtils {
    @Resource
    static ICacheService iCacheService;
    @Resource
    AppServiceImpl appService;
    @Resource
    IPriceOffNoticeService iPriceOffNoticeService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;

    public static void filterProducts(List productList, String keyword) {
        if (productList != null && productList.size() > 0) {
            if (ProductModel2.class.isInstance(productList.get(0))) {
                Iterator<ProductModel2> ptmList = productList.iterator();
                while (ptmList.hasNext()) {
                    //筛选title
                    ProductModel2 next = ptmList.next();
                    boolean b = FilterProducts(next.getTitle(), keyword);
                    if (!b) {
                        //false移除
                        ptmList.remove();
                    }
                }
            } else if (PtmCmpSku.class.isInstance(productList.get(0))) {
                Iterator<PtmCmpSku> ptmList = productList.iterator();
                while (ptmList.hasNext()) {
                    //筛选title
                    PtmCmpSku next = ptmList.next();
                    boolean b = FilterProducts(next.getTitle(), keyword);
                    if (!b) {
                        //false移除
                        ptmList.remove();
                    }
                }
            }
        }
    }

    private static boolean FilterProducts(String title, String keyword) {
        String[] filterWords = new String[]{"case", "cover", "glass", "battery", "for", "back", "guard", "cable"};
        boolean flag = true;
        if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(keyword)) {
            for (String str : filterWords) {
                if (title.trim().toLowerCase().contains(str)) {
                    //如果搜索结果中包含配件名称,看关键词中有没有
                    if (keyword.trim().contains(str)) {
                        //如果关键词中也有,ok
                        return true;
                    } else {
                        //关键词中没有,filter
                        return false;
                    }
                } else {
                    //如果搜索结果中不包含配件名称,看关键词中有没有
                    if (keyword.trim().toLowerCase().contains(str)) {
                        //如果关键词中有,filter
                        return false;
                    } else {
                        //关键词中没有,ok
                        continue;
                    }
                }
            }
        }
        //默认放行
        return flag;
    }

    //去十亿
    public static long rmoveBillion(long operatoredNumber) {
        long tempNumber = operatoredNumber;
        tempNumber = tempNumber - ConstantUtil.API_ONE_BILLION_NUMBER;
        return tempNumber;
    }

    //加十亿
    public static long addBillion(long operatoredNumber) {
        long tempNumber = operatoredNumber;
        tempNumber = tempNumber + ConstantUtil.API_ONE_BILLION_NUMBER;
        return tempNumber;
    }

    public static void bindUserAndDevices(UrmUser urmUser, List<String> ids, List<String> deviceIds, List<UrmUserDevice> urmUserDevices) {
        for (String id : ids) {
            boolean flag = false;
            for (String dId : deviceIds) {
                if (id.equals(dId)) {
                    flag = true;
                }
            }
            if (!flag) {
                UrmUserDevice urmUserDevice = new UrmUserDevice();
                urmUserDevice.setDeviceId(id);
                urmUserDevice.setUserId(urmUser.getId() + "");
                urmUserDevices.add(urmUserDevice);
            }
        }
    }

    public static void setParameters(Map<String, String> specsMap, List<PtmStdSkuParamGroup> paramGroups) {
        for (PtmStdSkuParamGroup ptmStdSkuParamGroup : paramGroups) {
            List<PtmStdSkuParamNode> params = ptmStdSkuParamGroup.getParams();
            for (PtmStdSkuParamNode ptmStdSkuParamNode : params) {
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(ptmStdSkuParamNode.getName()) && org.apache.commons.lang3.StringUtils.isNotEmpty(ptmStdSkuParamNode.getValue())) {
                    specsMap.put(ptmStdSkuParamNode.getName(), ptmStdSkuParamNode.getValue());
                }
            }
        }
    }

    public static PageableResult<PtmCmpSku> setPtmCmpSkuPageableResult(String cmpSkusJson) throws java.io.IOException {
        PageableResult<PtmCmpSku> pagedCmpskus;
        PageableResult datas = JSON.parseObject(cmpSkusJson, PageableResult.class);
        List<JSONObject> data = datas.getData();
        List<PtmCmpSku> cmpSkus = new ArrayList<>();
        for (JSONObject jsonObject : data) {
            String s = jsonObject.toJSONString();
            PtmCmpSku ptmCmpSku = JSON.parseObject(s, PtmCmpSku.class);
            cmpSkus.add(ptmCmpSku);
        }
        pagedCmpskus = new PageableResult<>(cmpSkus, datas.getNumFund(), datas.getCurrentPage(), datas.getPageSize());
        return pagedCmpskus;
    }

    public static int returnNumberBetween0And5(Long number) {
        //取得其余数
        Long tempNumber = number % 10;
        if (tempNumber > 0 && tempNumber <= 5) {
            number = (number / 10) * 10 + 5;
        } else if (tempNumber > 5) {
            number = (number / 10) * 10 + 10;
        }
        return number.intValue();
    }

    public static void getDealsFromCache(List list, int page, int size) {
        String key = ConstantUtil.API_DEALS_ + page + size;
        String dealsString = iCacheService.get(key, 0);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(dealsString)) {
            List<DealVo> dealVos = JSONArray.parseArray(dealsString, DealVo.class);
            if (dealVos != null && dealVos.size() > 0) {
                list.addAll(dealVos);
            }
        }
    }

    public static void setDeals2Cache(List list, int page, int size) {
        String key = ConstantUtil.API_DEALS_ + page + size;
        iCacheService.add(key, JSONArray.toJSONString(list), TimeUtils.SECONDS_OF_1_MINUTE * 5);
    }

    /**
     * 在数据对象返回客户端之前检测其域是否都有值,除对象成员外都赋初始值
     *
     * @param object
     * @throws Exception
     */

    public void resloveClass(Object object) {
        //加载这个类的成员
        //如果它是list且为null就初始化一下
        //如果是基本类型就算了,它们有初始值
        //如果是包装类型打破权限为其赋值
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field declaredField = declaredFields[i];
            declaredField.setAccessible(true);
            try {
                if (declaredField.getType().equals(List.class)) {
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, new ArrayList<>());
                    }
                }
                if (declaredField.getType().equals(Long.class)) {
                    //if null ,set
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, 0l);
                    }
                }
                if (declaredField.getType().equals(Integer.class)) {
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, 0);
                    }
                }
                if (declaredField.getType().equals(Float.class)) {
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, 0f);
                    }
                }
            } catch (Exception e) {
                System.out.println("set field exception : " + e.getMessage());
            }
        }
    }

    public void transferJson2Object(List<LinkedHashMap> dataList, List desList) throws Exception {
        for (LinkedHashMap linkedHashMap1 : dataList) {
            String string = JSON.toJSONString(linkedHashMap1);
            PtmCmpSku ptmCmpSku = JSONUtil.toObject(string, PtmCmpSku.class);
            desList.add(ptmCmpSku);
        }
    }

    public String getStringNum(String source) {
        source = source.trim();
        String str2 = "";
        if (source != null && !"".equals(source)) {
            for (int i = 0; i < source.length(); i++) {
                if (source.charAt(i) >= 48 && source.charAt(i) <= 57) {
                    str2 += source.charAt(i);
                }
            }

        }
        return str2;
    }

    public boolean isPriceOffAlert(String userToken, Long skuId) {
        if (!StringUtils.isEmpty(userToken)) {
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                PriceOffNotice priceOffNotice = iPriceOffNoticeService.getPriceOffNotice(urmUser.getId() + "", skuId);
                if (priceOffNotice != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
