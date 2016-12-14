package hasoffer.core.utils.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.app.vo.BackDetailVo;
import hasoffer.core.app.vo.CmpProductListVo;
import hasoffer.core.app.vo.OrderVo;
import hasoffer.core.app.vo.ProductListVo;
import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.cache.AppCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.po.admin.OrderStatsAnalysisPO;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.urm.PriceOffNotice;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.persistence.po.urm.UrmUserDevice;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.data.solr.FilterQuery;
import jodd.util.NameValue;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Resource
    private ICmpSkuService cmpSkuService;
    @Resource
    private ProductCacheManager productCacheManager;
    @Resource
    private AppCacheManager appCacheManager;

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

    public static boolean FilterProducts(String title, String keyword) {
        String[] filterWords = new String[]{"case", "cover", "glass", "battery", "for", "back", "guard", "cable"};
        boolean flag = true;
        Boolean x = filterAccessories(title, keyword, filterWords);
        if (x != null) return x;
        //默认放行
        return flag;
    }

    public static Boolean filterAccessories(String title, String keyword, String[] filterWords) {
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
        return null;
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

    public static boolean emailCheck(String email) {
        boolean matched;
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher matcher = pattern.matcher(email);
        matched = matcher.matches();
        return matched;
    }

    public static int getNumberFromString(String stringNumber) {
        Pattern p = Pattern.compile("[0-9\\.]+");
        Matcher m = p.matcher(stringNumber);
        if (m.find()) {
            if (!m.group(0).contains(".")) {
                return Integer.parseInt(m.group(0));
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }

    public static String getStringNumberFromString(String stringNumber) {
        Pattern p = Pattern.compile("[0-9\\.]+");
        Matcher m = p.matcher(stringNumber);
        if (m.find()) {
            return m.group(0);
        } else {
            return "";
        }
    }

    public static void setPriceSearchScope(List<FilterQuery> fqList, int priceFrom, int priceTo, String priceToStr) {
        String priceFromStr;
        if (priceFrom < priceTo && priceFrom >= 0) {
            if (priceFrom <= 0) {
                priceFrom = 1;
            }
            priceFromStr = String.valueOf(priceFrom);
            if (priceTo > 0) {
                priceToStr = String.valueOf(priceTo);
            }
            fqList.add(new FilterQuery("minPrice", String.format("[%s TO %s]", priceFromStr, priceToStr)));
        } else {
            fqList.add(new FilterQuery("minPrice", String.format("[%s TO %s]", "1", "*")));
        }
    }

    public static void setBrandSorted(List<NameValue<String, Long>> value) {
        replaceBrandString(value);
        //按字典排序
        Collections.sort(value, new Comparator<NameValue<String, Long>>() {
            @Override
            public int compare(NameValue<String, Long> o1, NameValue<String, Long> o2) {
                if (o1.getName().compareToIgnoreCase(o2.getName()) > 0) {
                    return 1;
                }
                if (o1.getName().compareToIgnoreCase(o2.getName()) < 0) {
                    return -1;
                }
                return 0;
            }
        });
        replaceBrandString(value);
    }

    private static void replaceBrandString(List<NameValue<String, Long>> value) {
        Iterator<NameValue<String, Long>> brandIterator = value.iterator();
        while (brandIterator.hasNext()) {
            NameValue<String, Long> next = brandIterator.next();
            String name = next.getName();
            if (name.equalsIgnoreCase("SamSung")) {
                next.setName("1");
                continue;
            } else if (name.equalsIgnoreCase("1")) {
                next.setName("SamSung");
                continue;
            }


            if (name.equalsIgnoreCase("Xiaomi")) {
                next.setName("2");
                continue;
            } else if (name.equalsIgnoreCase("2")) {
                next.setName("Xiaomi");
                continue;
            }


            if (name.equalsIgnoreCase("Motorola")) {
                next.setName("3");
                continue;
            } else if (name.equalsIgnoreCase("3")) {
                next.setName("Motorola");
                continue;
            }


            if (name.equalsIgnoreCase("Lenovo")) {
                next.setName("4");
                continue;
            } else if (name.equalsIgnoreCase("4")) {
                next.setName("Lenovo");
                continue;
            }


            if (name.equalsIgnoreCase("Huawei")) {
                next.setName("5");
                continue;
            } else if (name.equalsIgnoreCase("5")) {
                next.setName("Huawei");
                continue;
            }


            if (name.equalsIgnoreCase("Micromax")) {
                next.setName("6");
                continue;
            } else if (name.equalsIgnoreCase("6")) {
                next.setName("Micromax");
                continue;
            }


            if (name.equalsIgnoreCase("Lava")) {
                next.setName("7");
                continue;
            } else if (name.equalsIgnoreCase("7")) {
                next.setName("Lava");
                continue;
            }


            if (name.equalsIgnoreCase("Gionee")) {
                next.setName("8");
                continue;
            } else if (name.equalsIgnoreCase("8")) {
                next.setName("Gionee");
                continue;
            }


        }
    }


    private static void replacePivos(List<NameValue<String, Long>> value) {
        Iterator<NameValue<String, Long>> brandIterator = value.iterator();
        while (brandIterator.hasNext()) {
            NameValue<String, Long> next = brandIterator.next();
            String name = next.getName();
            if (name.equalsIgnoreCase("SamSung")) {
                next.setName("1");
                continue;
            } else if (name.equalsIgnoreCase("1")) {
                next.setName("SamSung");
                continue;
            }


            if (name.equalsIgnoreCase("Xiaomi")) {
                next.setName("2");
                continue;
            } else if (name.equalsIgnoreCase("2")) {
                next.setName("Xiaomi");
                continue;
            }


            if (name.equalsIgnoreCase("Motorola")) {
                next.setName("3");
                continue;
            } else if (name.equalsIgnoreCase("3")) {
                next.setName("Motorola");
                continue;
            }


            if (name.equalsIgnoreCase("Lenovo")) {
                next.setName("4");
                continue;
            } else if (name.equalsIgnoreCase("4")) {
                next.setName("Lenovo");
                continue;
            }


            if (name.equalsIgnoreCase("Huawei")) {
                next.setName("5");
                continue;
            } else if (name.equalsIgnoreCase("5")) {
                next.setName("Huawei");
                continue;
            }


            if (name.equalsIgnoreCase("Micromax")) {
                next.setName("6");
                continue;
            } else if (name.equalsIgnoreCase("6")) {
                next.setName("Micromax");
                continue;
            }


            if (name.equalsIgnoreCase("Lava")) {
                next.setName("7");
                continue;
            } else if (name.equalsIgnoreCase("7")) {
                next.setName("Lava");
                continue;
            }


            if (name.equalsIgnoreCase("Gionee")) {
                next.setName("8");
                continue;
            } else if (name.equalsIgnoreCase("8")) {
                next.setName("Gionee");
                continue;
            }


        }
    }


    public static void parseString2Pageable(String jsonString, Class classzz, PageableResult pageableResult) {
        pageableResult = (PageableResult<PtmStdSkuModel>) JSON.parseObject(jsonString, PageableResult.class);
        pageableResult.setData(JSONArray.parseArray(JSON.toJSONString(pageableResult.getData()), classzz));
    }

    public static void resolvePivotFields(Map map, PageableResult products, Map<String, List<NameValue<String, Long>>> pivotFieldVals) {
        if (pivotFieldVals != null && pivotFieldVals.size() > 0) {
            Map<String, List<NameValue<String, Long>>> pivotFieldValMap = new HashMap<>();
            List<NameValue<String, Long>> netWorkNVList = new ArrayList<>();
            Set<Map.Entry<String, List<NameValue<String, Long>>>> entries = pivotFieldVals.entrySet();
            Iterator<Map.Entry<String, List<NameValue<String, Long>>>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<NameValue<String, Long>>> next = iterator.next();
                String key = next.getKey();
                List<NameValue<String, Long>> value = next.getValue();
                if (key.equals("Network3G") || key.equals("Network4G") || key.equals("Network")) {
                    netWorkNVList.addAll(value);
                }
                String cateFilterValue = ConstantUtil.API_CATEGORY_FILTER_PARAMS_MAP.get(key);
                //  //brand需要按照指定顺序返回
                //SamSung Xiaomi Motorola Lenovo Huawei Micromax Lava Gionee
                if (cateFilterValue != null && cateFilterValue.equals("brand")) {
                    ApiUtils.setBrandSorted(value);
                }
                if (cateFilterValue != null) {
                    pivotFieldValMap.put(cateFilterValue, value);
                }
            }
            if (netWorkNVList.size() > 0) {
                pivotFieldValMap.put("Network", netWorkNVList);
            }
            //处理下返回顺序
            pivotFieldValMap = sortedPivotFieldValMap(pivotFieldValMap);
            map.put("pivos", pivotFieldValMap);
            map.put("numberFound", products.getNumFund());
        }
    }

    //sort list area =================================================================
    public static void getSortedDealListByClicCountAsc(List<AppDeal> deals) {
        Collections.sort(deals, new Comparator<AppDeal>() {
            @Override
            public int compare(AppDeal o1, AppDeal o2) {
                if (o1.getDealClickCount() > o2.getDealClickCount()) {
                    return -1;
                } else if (o1.getDealClickCount() < o2.getDealClickCount()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public static void getSortedProListVoListByClicCountAsc(List<CmpProductListVo> comparedSkuVos) {
        Collections.sort(comparedSkuVos, new Comparator<CmpProductListVo>() {
            @Override
            public int compare(CmpProductListVo o1, CmpProductListVo o2) {
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                } else if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public static void getSortedStdPriceListByClicCountAsc(List<PtmStdPrice> data) {
        Collections.sort(data, new Comparator<PtmStdPrice>() {
            @Override
            public int compare(PtmStdPrice o1, PtmStdPrice o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public static void getSortedCateVoListByClicCountAsc(List<CategoryVo> tempThirdCategoryList) {
        Collections.sort(tempThirdCategoryList, new Comparator<CategoryVo>() {
            @Override
            public int compare(CategoryVo o1, CategoryVo o2) {
                if (o1.getRank() > o2.getRank()) {
                    return 1;
                } else if (o1.getRank() < o2.getRank()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    /**
     * 将指定的pivotFieldValMap换成按照执行顺序的map
     *
     * @param pivotFieldValMap
     * @return
     */
    private static Map<String, List<NameValue<String, Long>>> sortedPivotFieldValMap(Map<String, List<NameValue<String, Long>>> pivotFieldValMap) {
        Map<String, List<NameValue<String, Long>>> tempFieldValMap = new LinkedHashMap<>();

        Set<Map.Entry<String, List<NameValue<String, Long>>>> entries = pivotFieldValMap.entrySet();
        Iterator<Map.Entry<String, List<NameValue<String, Long>>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<NameValue<String, Long>>> next = iterator.next();
            String key = next.getKey();
            List<NameValue<String, Long>> value = next.getValue();
            replaceCategoryParmsKey(key, tempFieldValMap, value);
        }
        //将指定字符串换成数字
        if (tempFieldValMap.size() > 0) {
            String[] strings = tempFieldValMap.keySet().toArray(new String[]{});
            Arrays.sort(strings);
            Map<String, List<NameValue<String, Long>>> tempFieldValMap2 = new LinkedHashMap<>();
            for (int i = 0; i < strings.length; i++) {
                List<NameValue<String, Long>> nameValues = tempFieldValMap.get(strings[i]);
                if (nameValues != null) {
                    tempFieldValMap2.put(strings[i], nameValues);
                }
            }
            pivotFieldValMap = tempFieldValMap2;
            //按字典排序
            //将数字换成指定字符串
            tempFieldValMap = new LinkedHashMap<>();
            Set<Map.Entry<String, List<NameValue<String, Long>>>> entries2 = pivotFieldValMap.entrySet();
            Iterator<Map.Entry<String, List<NameValue<String, Long>>>> iterator2 = entries2.iterator();
            //TODO 待优化
            while (iterator2.hasNext()) {
                Map.Entry<String, List<NameValue<String, Long>>> next = iterator2.next();
                String key = next.getKey();
                List<NameValue<String, Long>> value = next.getValue();
                replaceCategoryParmsKey(key, tempFieldValMap, value);
            }
        }
        return tempFieldValMap;
    }

    /**
     * 将指定的key按其顺序换成数字
     * 返回的pivos列表
     */
    public static void replaceCategoryParmsKey(String key, Map<String, List<NameValue<String, Long>>> tempReplacePivos, List<NameValue<String, Long>> value) {
        if (key.equals("brand")) {
            tempReplacePivos.put("1", value);
        } else if (key.equals("1")) {
            tempReplacePivos.put("brand", value);
        }


        if (key.equals("Network")) {
            tempReplacePivos.put("3", value);
        } else if (key.equals("3")) {
            tempReplacePivos.put("Network", value);
        }

        if (key.equals("Screen Resolution")) {
            tempReplacePivos.put("5", value);
        } else if (key.equals("5")) {
            tempReplacePivos.put("Screen Resolution", value);
        }

        if (key.equals("Operating System")) {
            tempReplacePivos.put("8", value);
        } else if (key.equals("8")) {
            tempReplacePivos.put("Operating System", value);
        }

        if (key.equals("Ram")) {
            tempReplacePivos.put("2", value);
        } else if (key.equals("2")) {
            tempReplacePivos.put("Ram", value);
        }

        if (key.equals("Screen Size")) {
            tempReplacePivos.put("4", value);
        } else if (key.equals("4")) {
            tempReplacePivos.put("Screen Size", value);
        }

        if (key.equals("Secondary Camera")) {
            tempReplacePivos.put("6", value);
        } else if (key.equals("6")) {
            tempReplacePivos.put("Secondary Camera", value);
        }

        if (key.equals("Battery Capacity")) {
            tempReplacePivos.put("7", value);
        } else if (key.equals("7")) {
            tempReplacePivos.put("Battery Capacity", value);
        }

        if (key.equals("Primary Camera")) {
            tempReplacePivos.put("9", value);
        } else if (key.equals("9")) {
            tempReplacePivos.put("Primary Camera", value);
        }

        if (key.equals("Internal Memory")) {
            tempReplacePivos.put("a0", value);
        } else if (key.equals("a0")) {
            tempReplacePivos.put("Internal Memory", value);
        }

        if (key.equals("Expandable Memory")) {
            tempReplacePivos.put("a1", value);
        } else if (key.equals("a1")) {
            tempReplacePivos.put("Expandable Memory", value);
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

    public void sendEmail(String to, String content, boolean isHtml) throws MessagingException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true"); // 将这个参数设为true，让服务器进行认证,认证用户名和密码是否正确
        prop.put("mail.smtp.timeout", "25000");
        mailSender.setJavaMailProperties(prop);
        mailSender.setUsername("zhouwendong@hasoffer.com"); // 根据自己的情况,设置username
        mailSender.setPassword("Zhou1008");
        mailSender.setHost("smtp.exmail.qq.com");
        // 构建简单邮件对象，见名知意
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "utf-8");
        // 设定邮件参数
        messageHelper.setFrom("zhouwendong@hasoffer.com");
        messageHelper.setTo(to);
        messageHelper.setSubject("欢迎使用");
//        messageHelper.setText("Click ‘完成验证’ to finish auth:<a href=\"http://localhost:8080/u/ev/" + userId + "/" + activeCode + "\">验证</a>", true);
        // 发送邮件
        mailSender.send(mailMessage);
    }

    public void addProductVo2List(List desList, List sourceList) {

        if (sourceList != null && sourceList.size() > 0) {
            if (PtmProduct.class.isInstance(sourceList.get(0))) {
                Iterator<PtmProduct> ptmList = sourceList.iterator();
                while (ptmList.hasNext()) {
                    PtmProduct ptmProduct = ptmList.next();
                    int count = cmpSkuService.getSkuSoldStoreNum(ptmProduct.getId());
                    if (count > 0) {
                        ProductListVo productListVo = new ProductListVo();
                        productListVo.setId(ptmProduct.getId());
                        productListVo.setImageUrl(productCacheManager.getProductMasterImageUrl(ptmProduct.getId()));
                        productListVo.setName(ptmProduct.getTitle());
                        productListVo.setPrice(Math.round(ptmProduct.getPrice()));
                        productListVo.setStoresNum(count);
                        setCommentNumAndRatins(productListVo);
                        desList.add(productListVo);
                    }
                }
            } else if (ProductModel2.class.isInstance(sourceList.get(0))) {
                Iterator<ProductModel2> ptmList = sourceList.iterator();
                while (ptmList.hasNext()) {
                    ProductModel2 ptmProduct = ptmList.next();
                    ProductListVo productListVo = new ProductListVo();
                    productListVo.setId(ptmProduct.getId());
                    productListVo.setImageUrl(productCacheManager.getProductMasterImageUrl(ptmProduct.getId()));
                    productListVo.setName(ptmProduct.getTitle());
                    productListVo.setPrice(Math.round(ptmProduct.getMinPrice()));
                    productListVo.setRatingNum(ptmProduct.getRating());
                    productListVo.setCommentNum(Long.valueOf(ptmProduct.getReview()));
                    productListVo.setStoresNum(ptmProduct.getStoreCount());
                    desList.add(productListVo);
                }
            } else if (PtmStdSkuModel.class.isInstance(sourceList.get(0))) {
                Iterator<PtmStdSkuModel> ptmList = sourceList.iterator();
                while (ptmList.hasNext()) {
                    PtmStdSkuModel ptmStdSkuModel = ptmList.next();
                    ProductListVo productListVo = new ProductListVo();
                    productListVo.setId(ptmStdSkuModel.getId());
                    productListVo.setImageUrl(productCacheManager.getPtmStdSkuImageUrl(ApiUtils.rmoveBillion(ptmStdSkuModel.getId())));
                    productListVo.setName(ptmStdSkuModel.getTitle());
                    productListVo.setPrice(Math.round(ptmStdSkuModel.getMinPrice()));
                    productListVo.setRatingNum(ptmStdSkuModel.getRating());
                    productListVo.setCommentNum(Long.valueOf(ptmStdSkuModel.getReview()));
                    productListVo.setStoresNum(ptmStdSkuModel.getStoreCount());
                    desList.add(productListVo);
                }
            }
        }
    }

    public void setCommentNumAndRatins(ProductListVo productListVo) {
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listPagedCmpSkus(productListVo.getId(), 1, 20);
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            List<PtmCmpSku> tempSkuList = pagedCmpskus.getData();
            //计算评论数*星级的总和
            int sum = 0;
            //统计site
            Set<Website> websiteSet = new HashSet<Website>();
            for (PtmCmpSku ptmCmpSku : tempSkuList) {
                websiteSet.add(ptmCmpSku.getWebsite());
            }
            Long totalCommentNum = Long.valueOf(0);
            for (PtmCmpSku ptmCmpSku2 : tempSkuList) {
                if (websiteSet.size() <= 0) {
                    break;
                }
                if (websiteSet.contains(ptmCmpSku2.getWebsite())) {
                    websiteSet.remove(ptmCmpSku2.getWebsite());
//                    System.out.println("count comment ans stats exclude  ebay ");
                    if (!ptmCmpSku2.getWebsite().equals(Website.EBAY)) {
                        //评论数*星级 累加 除以评论数和
                        sum += ptmCmpSku2.getRatings() * ptmCmpSku2.getCommentsNumber();
                        //去除列表中除此之外的其他此site的数据
                        totalCommentNum += ptmCmpSku2.getCommentsNumber();
                    }
                }
            }
            productListVo.setCommentNum(totalCommentNum);
            int rating = returnNumberBetween0And5(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(totalCommentNum == 0 ? 1 : totalCommentNum), 0, BigDecimal.ROUND_HALF_UP).longValue());
            productListVo.setRatingNum(rating <= 0 ? 90 : rating);
        }
    }

    public void getSkuListByKeyword(Map map, PageableResult p) {
        if (p.getPivotFieldVals() != null && p.getPivotFieldVals().size() > 0) {
            // List<CategoryVo>
            List<CategoryVo> secondCategoryList = new ArrayList();
            List<CategoryVo> categorys = new ArrayList();
            List<CategoryVo> thirdCategoryList = new ArrayList();
            Map pivotFieldVals = p.getPivotFieldVals();
            Set<Map.Entry> set = pivotFieldVals.entrySet();
            Iterator<Map.Entry> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry next = iterator.next();
                List<NameValue> nameValues = (List<NameValue>) next.getValue();
                System.out.println("cate " + next.getKey() + " ::: nameValues  :" + nameValues.size());
                for (NameValue nameValue : nameValues) {
                    Long cateId = Long.valueOf(nameValue.getName() + "");
                    //可能是二级也可能是三级 ,二级的放一块,三级的放一块
                    if (cateId > 0) {
                        PtmCategory ptmCategory = appCacheManager.getCategoryById(cateId);
                        if (ptmCategory != null && ptmCategory.getLevel() == 2) {
                            //处理二级类目
                            CategoryVo categoryVo = getCategoryVo(ptmCategory);
                            secondCategoryList.add(categoryVo);
                        } else if (ptmCategory != null && ptmCategory.getLevel() == 3) {
                            //处理三级类目
                            CategoryVo categoryVo3 = getCategoryVo(ptmCategory);
                            thirdCategoryList.add(categoryVo3);
                        }
                    }
                }
            }
            //获取到类目id appCacheManager.getCategorys(categoryId);
            //先获取一级类目列表
            List<CategoryVo> firstCategoryList = appCacheManager.getCategorys("");
            //对二级类目按照rank排序
            getSortedCateVoListByClicCountAsc(secondCategoryList);

            //遍历一级类目将二级类目匹配排序
            for (CategoryVo firstPtmCategory : firstCategoryList) {
                for (CategoryVo cate : secondCategoryList) {
                    //遍历所有,如果父类id是其则加入list
                    if (cate.getParentId().equals(firstPtmCategory.getId())) {
                        categorys.add(cate);
                    }
                }
            }

            //遍历二级类目,将三级类目匹配排序和归类
            Iterator<CategoryVo> iterator1 = categorys.iterator();
            while (iterator1.hasNext()) {
                List<CategoryVo> tempThirdCategoryList = new ArrayList();
                CategoryVo next = iterator1.next();
                for (CategoryVo cate : thirdCategoryList) {
                    //遍历所有,如果父类id是其则加入list
                    if (cate.getParentId().equals(next.getId())) {
                        tempThirdCategoryList.add(cate);
                    }
                }

                //对三级类目按照rank排序
                getSortedCateVoListByClicCountAsc(tempThirdCategoryList);
                if (tempThirdCategoryList.size() > 0) {
                    next.setHasChildren(1);
                }
                next.setCategorys(tempThirdCategoryList);
            }
            map.put("categorys", categorys);
        }
    }
    //sort list area =================================================================

    /**
     * 获取类目Vo
     *
     * @param ptmCategory
     * @return
     */
    private CategoryVo getCategoryVo(PtmCategory ptmCategory) {
        CategoryVo categoryVo = new CategoryVo();
        categoryVo.setId(ptmCategory.getId());
        categoryVo.setLevel(ptmCategory.getLevel());
        categoryVo.setParentId(ptmCategory.getParentId());
        categoryVo.setRank(ptmCategory.getRank());
        categoryVo.setName(ptmCategory.getName());
        categoryVo.setHasChildren(0);
        return categoryVo;
    }

    /**
     * 计算hasofferCoin
     *
     * @param users
     * @param data
     */
    public void calculateHasofferCoin(List<UrmUser> users, BackDetailVo data) {
        List<OrderVo> transcations = new ArrayList<OrderVo>();
        BigDecimal pendingCoins = BigDecimal.ZERO;
        BigDecimal verifiedCoins = BigDecimal.ZERO;
        for (UrmUser user : users) {
            List<OrderStatsAnalysisPO> orders = appService.getBackDetails(user.getId().toString());
            for (OrderStatsAnalysisPO orderStatsAnalysisPO : orders) {
                if (orderStatsAnalysisPO.getWebSite().equals(Website.FLIPKART.name())) {
                    OrderVo orderVo = new OrderVo();
                    BigDecimal tempPrice = orderStatsAnalysisPO.getSaleAmount().multiply(BigDecimal.valueOf(0.075)).min(orderStatsAnalysisPO.getTentativeAmount());
                    //乘以10再取整
                    tempPrice = tempPrice.multiply(BigDecimal.TEN);
                    orderVo.setAccount(tempPrice.divide(BigDecimal.ONE, 1, BigDecimal.ROUND_HALF_UP));
                    orderVo.setChannel(orderStatsAnalysisPO.getChannel());
                    orderVo.setOrderId(orderStatsAnalysisPO.getOrderId());
                    orderVo.setOrderTime(orderStatsAnalysisPO.getOrderTime());
                    orderVo.setWebsite(orderStatsAnalysisPO.getWebSite());
                    orderVo.setStatus(orderStatsAnalysisPO.getOrderStatus());
                    transcations.add(orderVo);
                    if (orderStatsAnalysisPO.getOrderStatus() != null) {
                        if (!orderStatsAnalysisPO.getOrderStatus().equals("cancelled") && !orderStatsAnalysisPO.getOrderStatus().equals("disapproved")) {
                            if (!orderStatsAnalysisPO.getOrderStatus().equals("approved")) {
                                pendingCoins = pendingCoins.add(tempPrice);
                            }
                        }
                        if (orderStatsAnalysisPO.getOrderStatus().equals("approved")) {
                            verifiedCoins = verifiedCoins.add(tempPrice);
                        }
                    }

                }
            }
        }
        //待定的
        data.setPendingCoins(pendingCoins.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        //可以使用的
        verifiedCoins = verifiedCoins.multiply(BigDecimal.TEN);
        data.setVerifiedCoins(verifiedCoins.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
        data.setTranscations(transcations);
    }


}
