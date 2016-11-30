package hasoffer.core.utils.api;

import com.alibaba.fastjson.JSON;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.core.utils.ConstantUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by hs on 2016年10月19日.
 * Time 14:28
 */
@Component
public class ApiUtils {
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
