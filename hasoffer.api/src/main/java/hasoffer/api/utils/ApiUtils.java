package hasoffer.api.utils;

import com.alibaba.fastjson.JSON;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by hs on 2016年10月19日.
 * Time 14:28
 */
public class ApiUtils {
    @Resource
    static AppServiceImpl appService;
    @Resource
    static IPriceOffNoticeService iPriceOffNoticeService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;

    /**
     * 在数据对象返回客户端之前检测其域是否都有值,除对象成员外都赋初始值
     *
     * @param object
     * @throws Exception
     */

    public static void resloveClass(Object object) {
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
                    System.out.println("list");
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, new ArrayList<>());
                    }
                }
                if (declaredField.getType().equals(Long.class)) {
                    System.out.println("Long ");
                    //if null ,set
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, 0l);
                    }
                }
                if (declaredField.getType().equals(Integer.class)) {
                    System.out.println("Integer ");
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, 0);
                    }
                }
                if (declaredField.getType().equals(Float.class)) {
                    System.out.println("Float ");
                    if (declaredField.get(object) == null) {
                        declaredField.set(object, 0f);
                    }
                }
            } catch (Exception e) {
                System.out.println("set field exception : " + e.getMessage());
            }
        }
        System.out.println("over");
    }

    public static void transferJson2Object(List<LinkedHashMap> dataList, List desList) throws Exception {
        for (LinkedHashMap linkedHashMap1 : dataList) {
            String string = JSON.toJSONString(linkedHashMap1);
            PtmCmpSku ptmCmpSku = JSONUtil.toObject(string, PtmCmpSku.class);
            desList.add(ptmCmpSku);
        }
    }

    public static String getStringNum(String source) {
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

    public static boolean isPriceOffAlert(String userToken, Long skuId) {
        if (!StringUtils.isEmpty(userToken)) {
            System.out.println("userToken is :" + userToken);
            UrmUser urmUser = appService.getUserByUserToken(userToken);
            if (urmUser != null) {
                System.out.println("this userToken has user ");
                PriceOffNotice priceOffNotice = iPriceOffNoticeService.getPriceOffNotice(urmUser.getId() + "", skuId);
                if (priceOffNotice != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
