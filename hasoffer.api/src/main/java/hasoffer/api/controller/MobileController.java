package hasoffer.api.controller;

import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageableResult;
import hasoffer.core.app.AppSearchService;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.app.vo.mobile.SiteMapKeyVo;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.impl.PtmStdSKuServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.ApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * Created by hs on 2016年12月20日.
 * H5 Controller
 * Time 16:38
 */
@Controller
@RequestMapping("m")
public class MobileController {
    @Autowired
    AppSearchService appSearchService;
    @Autowired
    ApiUtils apiUtils;
    @Autowired
    PtmStdSKuServiceImpl ptmStdSKuService;
    @Autowired
    PtmStdSkuIndexServiceImpl stdSkuIndexService;
    Logger logger = LoggerFactory.getLogger(MobileController.class);

    public static void main(String[] args) {
        System.out.println(new Date().getTime());
    }

    @RequestMapping("siteMap")
    public ModelAndView siteMapHasoffer(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "2000") int pageSize) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_ERRORCODE_SUCCESS_MSG);
        List<Integer> priceList = new LinkedList<>();
        priceList.add(5000);
        priceList.add(10000);
        priceList.add(15000);
        priceList.add(20000);
        priceList.add(25000);
        priceList.add(30000);


        //获取distinct的品牌列表
//        List<String> brandList = ptmStdSKuService.getPtmStdSkuBrandList();
        List<String> brandList = Arrays.asList("Samsung", "Lenovo", "Motorola", "Xiaomi", "Oppo", "Lyf", "Apple", "LeEco", "Coolpad", "ZUK");
        //--特征1
        //FM radio --FM_Radio 能与不能
        //SIM_SLOT 包含Dual Sim
        //Network_Support 包含 3G 4G
        //OperatingSystem  包含windows
        //Touch Screen 能与不能
        //Bluetooth 能与不能
        //Camera  不管前置还是后置
        Map<String, Map<String, String>> characteristicMap1 = new HashMap();
        characteristicMap1.put("Wireless Fm", ApiUtils.getBuilderMap("FM_Radio", "yes", new HashMap()));
        characteristicMap1.put("Dual Sim", ApiUtils.getBuilderMap("SIM_Slot", "Dual Sim", new HashMap()));
        characteristicMap1.put("3g", ApiUtils.getBuilderMap("Network_Support", "3G", new HashMap()));
        characteristicMap1.put("Camera", ApiUtils.getBuilderMap("Camera", "yes", new HashMap()));
        characteristicMap1.put("Windows", ApiUtils.getBuilderMap("Operating_System", "Windows", new HashMap()));
        characteristicMap1.put("Touch Screen", ApiUtils.getBuilderMap("Touch_Screen", "yes", new HashMap()));
        characteristicMap1.put("Bluetooth", ApiUtils.getBuilderMap("Bluetooth", "yes", new HashMap()));
        characteristicMap1.put("4g", ApiUtils.getBuilderMap("Network_Support", "4G", new HashMap()));

        //--特征2
        //Camera  不管前置还是后置
        //SIM_SLOT 包含Dual Sim
        //Network_Support 包含 3G
        //Bluetooth 能与不能
        //OperatingSystem  包含Android
        //Touch Screen 能与不能
        //Processor 为双核Dual Core
        //Wi Fi 能与不能

        Map<String, Map<String, String>> characteristicMap2 = new HashMap();
        characteristicMap2.put("Wireless Fm", ApiUtils.getBuilderMap("FM_Radio", "yes", new HashMap()));
        characteristicMap2.put("Dual Sim", ApiUtils.getBuilderMap("SIM_Slot", "Dual Sim", new HashMap()));
        characteristicMap2.put("3g", ApiUtils.getBuilderMap("Network_Support", "3G", new HashMap()));
        characteristicMap2.put("Camera", ApiUtils.getBuilderMap("Camera", "yes", new HashMap()));
        characteristicMap2.put("Android", ApiUtils.getBuilderMap("Operating_System", "Android", new HashMap()));
        characteristicMap2.put("WiFi", ApiUtils.getBuilderMap("WiFi", "yes", new HashMap()));
        characteristicMap2.put("Touch Screen", ApiUtils.getBuilderMap("Touch_Screen", "yes", new HashMap()));
        characteristicMap2.put("Bluetooth", ApiUtils.getBuilderMap("Bluetooth", "yes", new HashMap()));
        characteristicMap2.put("Processor", ApiUtils.getBuilderMap("Processor", "Dual Core", new HashMap()));

        Map<String, List> keyMap = new HashMap();
        //key 1
        Map proMap = new HashMap();
        keyMap.put("Mobile Finder On Hasoffer", Arrays.asList(new SiteMapKeyVo("Latest Mobiles", 0)));

        //key 2
        proMap.clear();

        //获取categoryId 为5  level 为2 的所有商品
        List<SiteMapKeyVo> stdSkuKeyVoList = new ArrayList<>();
        stdSkuKeyVoList.add(new SiteMapKeyVo("Top Mobile Phones", 0));
//        PageableResult<PtmStdSku> ptmStdSkuList = ptmStdSKuService.getPtmStdSkuListByMinId(0l, page, pageSize);

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setPage(page);
        searchCriteria.setPageSize(pageSize);
        searchCriteria.setCategoryId("5");
        searchCriteria.setLevel(2);
        searchCriteria.setSort(SearchResultSort.RATING);

        PageableResult<PtmStdSkuModel> pageableResult = stdSkuIndexService.filterStdSkuOnCategoryByCriteria(searchCriteria);

        for (PtmStdSkuModel ptmStdSkuModel : pageableResult.getData()) {
            stdSkuKeyVoList.add(new SiteMapKeyVo(ApiUtils.removeSpecialSymbol(ptmStdSkuModel.getTitle()), 3).buildePid(ptmStdSkuModel.getId()));
        }
        keyMap.put("All Mobile Models In India", stdSkuKeyVoList);
        //key 3
        List<SiteMapKeyVo> top10MobilesList = new LinkedList<>();
        top10MobilesList.addAll(Arrays.asList(
                //1. Top 10 + Mobiles + Below +“价格参数”
                new SiteMapKeyVo("Top 10  Mobiles  Below 5000", 2).builderProMap("minPrice", "5000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 10000", 2).builderProMap("minPrice", "10000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 15000", 2).builderProMap("minPrice", "15000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 20000", 2).builderProMap("minPrice", "20000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 25000", 2).builderProMap("minPrice", "25000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 30000", 2).builderProMap("minPrice", "30000"),

                new SiteMapKeyVo("SamSung mobile", 2).builderProMap("Brand", "Samsung"),
                new SiteMapKeyVo("SamSung mobile Below 5000", 2).builderProMap("minPrice", "5000"),
                new SiteMapKeyVo("SamSung mobile Below 10000", 2).builderProMap("minPrice", "10000"),
                new SiteMapKeyVo("SamSung mobile Below 15000", 2).builderProMap("minPrice", "15000"),
                new SiteMapKeyVo("SamSung mobile Below 20000", 2).builderProMap("minPrice", "20000"),
                new SiteMapKeyVo("SamSung mobile Below 25000", 2).builderProMap("minPrice", "25000"),
                new SiteMapKeyVo("SamSung mobile Below 30000", 2).builderProMap("minPrice", "30000"),

                new SiteMapKeyVo("Redmi Note 3 mobile", 2).builderProMap("Model", "Redmi Note 3"),
                new SiteMapKeyVo("Redmi Note 3 mobile Below 5000", 2).builderProMap("minPrice", "5000"),
                new SiteMapKeyVo("Redmi Note 3 mobile Below 10000", 2).builderProMap("minPrice", "10000"),
                new SiteMapKeyVo("Redmi Note 3 mobile Below 15000", 2).builderProMap("minPrice", "15000"),
                new SiteMapKeyVo("Redmi Note 3 mobile Below 20000", 2).builderProMap("minPrice", "20000"),
                new SiteMapKeyVo("Redmi Note 3 mobile Below 25000", 2).builderProMap("minPrice", "25000"),
                new SiteMapKeyVo("Redmi Note 3 mobile Below 30000", 2).builderProMap("minPrice", "30000")));
        //2. Top 10 + “品牌名称” + Mobiles
        for (String brand : brandList) {
            SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo(brand, 2);
            Map map = new HashMap<>();
            map.put("Brand", brand);
            siteMapKeyVo.setPros(map);
            top10MobilesList.add(siteMapKeyVo);
        }

        //3. Top 10 + “品牌名称” + Mobiles + Below +“价格参数”
//                new SiteMapKeyVo("Top 10 HTC Mobiles Below 5000", 2).builderProMap("minPrice", "5000").builderProMap("Brand", "HTC"),
        for (String brand : brandList) {
            for (Integer price : priceList) {
                SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo("Top 10 " + brand + " Mobiles Below " + price, 2);
                Map map = new HashMap<>();
                map.put("minPrice", price + "");
                map.put("Brand", brand);
                siteMapKeyVo.setPros(map);
                top10MobilesList.add(siteMapKeyVo);
            }
        }

        //4. Top 10 + 手机特征1+ Mobiles
     /*   String[] map1Keys = characteristicMap1.keySet().toArray(new String[]{});
        for (String key : map1Keys) {
            SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo("Top 10 " + key + " Mobiles", 2);
            Map map = new HashMap<>();
            map.putAll(characteristicMap1.get(key));
            siteMapKeyVo.setPros(map);
            top10MobilesList.add(siteMapKeyVo);
        }*/
        //5. Top 10 +手机特征2 + Smart Phones
    /*    String[] map2Keys = characteristicMap2.keySet().toArray(new String[]{});
        for (String key : map2Keys) {
            SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo("Top 10 " + key + " Smart Phones", 2);
            Map map = new HashMap<>();
            map.putAll(characteristicMap2.get(key));
            siteMapKeyVo.setPros(map);
            top10MobilesList.add(siteMapKeyVo);
        }*/
        //6. Top 10 +品牌+手机特征2+Mobiles
       /* for (String brand : brandList) {
            for (String key : map2Keys) {
                SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo("Top 10 " + brand + " " + key + " Mobiles", 2);
                Map map = new HashMap<>();
                map.putAll(characteristicMap2.get(key));
                map.put("Brand", brand);
                siteMapKeyVo.setPros(map);
                top10MobilesList.add(siteMapKeyVo);
            }
        }*/
        //7. Top 10+手机特征2 + Mobiles+Below+价格参数
        /*for (String key : map2Keys) {
            for (Integer price : priceList) {
                SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo("Top 10 " + key + " Mobiles Below " + price, 2);
                Map map = new HashMap<>();
                map.putAll(characteristicMap2.get(key));
                map.put("minPrice", price + "");
                siteMapKeyVo.setPros(map);
                top10MobilesList.add(siteMapKeyVo);
            }
        }*/
        //8. Top 10 +品牌+手机特征2+Mobiles+Below+价格参数
       /* for (String brand : brandList) {
            for (String key : map2Keys) {
                for (Integer price : priceList) {
                    SiteMapKeyVo siteMapKeyVo = new SiteMapKeyVo("Top 10 " + brand + " " + key + " Mobiles Below " + price, 2);
                    Map map = new HashMap<>();
                    map.putAll(characteristicMap2.get(key));
                    map.put("minPrice", price + "");
                    map.put("Brand", brand);
                    siteMapKeyVo.setPros(map);
                    top10MobilesList.add(siteMapKeyVo);
                }
            }
        }*/
        top10MobilesList.addAll(Arrays.asList(
                new SiteMapKeyVo("Top 10 Htc Desire Series Mobiles", 1).buildeShortName("Htc Desire"),
                new SiteMapKeyVo("Top 10 Sony Xperia Series Mobiles", 1).buildeShortName("Sony Xperia"),
                new SiteMapKeyVo("Top 10 Samsung Galaxy Series Mobiles", 1).buildeShortName("Samsung Galaxy"),
                new SiteMapKeyVo("Top 10 Lg Optimus Series Mobiles", 1).buildeShortName("Lg Optimus"),
                new SiteMapKeyVo("Top 10 Nokia Lumia Series Mobiles", 1).buildeShortName("Nokia Lumia"),
                new SiteMapKeyVo("Top 10 Nokia Asha Series Mobiles", 1).buildeShortName("Nokia Asha")
        ));
        keyMap.put("Top 10 Mobiles", top10MobilesList);
        modelAndView.addObject("data", keyMap);
        return modelAndView;
    }

    /**
     * 处理前端的关键字"搜索"
     *
     * @return
     */
    @RequestMapping("keySearch")
    public ModelAndView resolveKeyWordsSearch(@RequestBody SiteMapKeyVo siteMapKeyVo, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pageSize) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_ERRORCODE_SUCCESS_MSG);

        ResultVo resultVo = new ResultVo();
        PageableResult<PtmStdSkuModel> pageableResult = null;
        List ProductList = new ArrayList();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setCategoryId("5");
        searchCriteria.setLevel(2);
        searchCriteria.setPage(page);
        searchCriteria.setPageSize(pageSize);
        searchCriteria.setSort(SearchResultSort.RATING);
        switch (siteMapKeyVo.getType()) {
            case 0:
                //0 是把name 发回来
                if (StringUtils.isNotEmpty(siteMapKeyVo.getName())) {
                    if (siteMapKeyVo.getName().equals("Latest Mobiles")) {
                        //获取发布日期为最近的10部手机 -- 创建时间降序
                        //暂时为评论数降序排列的列表 sort 为 POPULARITY 或者null即可
                        searchCriteria.setSort(SearchResultSort.POPULARITY);
                    }
                   /* if (name.equals("Top Mobile Phone")) {
                        //评分数前十的手机
                        //默认是评分数前十的手机
                    }*/
                }
                break;
            case 1:
                //1 是把shortName 发回来
                if (StringUtils.isNotEmpty(siteMapKeyVo.getShortName())) {
                    searchCriteria.setKeyword(siteMapKeyVo.getShortName());
                }
                break;
            case 2:
                // 2 是把pros中的数据发回来
                if (siteMapKeyVo.getPros() != null) {
                    Set<Map.Entry<String, String>> set = siteMapKeyVo.getPros().entrySet();
                    Iterator<Map.Entry<String, String>> iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, String> next = iterator.next();
                        //key 有 price  brand  --特征1  特征2
                        String key = next.getKey();
                        String value = next.getValue();
                        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                            switch (key) {
                                case "Brand":
                                    searchCriteria.setBrand(new String[]{value});
                                    break;
                                case "minPrice":
                                    searchCriteria.setPriceFrom(1);
                                    searchCriteria.setPriceTo(Integer.parseInt(value));
                                    break;
                                case "Model":
                                    searchCriteria.setModel(new String[]{value});
                                    break;
                                case "FM_Radio":
                                    searchCriteria.setFmRadio(value);
                                    break;
                                case "SIM_Slot":
                                    searchCriteria.setSimSlot(value);
                                    break;
                                case "Network_Support":
                                    searchCriteria.setNetworkSupport(new String[]{value});
                                    break;
                                case "Camera":
        /*                            searchCriteria.setQueryPrimaryCamera(ConstantUtil.SOLR_DEFAULT_VALUE_NOTEMPTY_FIELD);
                                    searchCriteria.setQuerySecondaryCamera(ConstantUtil.SOLR_DEFAULT_VALUE_NOTEMPTY_FIELD);*/
                                    searchCriteria.setQueryPrimaryCamera("*");
                                    searchCriteria.setQuerySecondaryCamera("*");
                                    break;
                                case "Operating_System":
                                    searchCriteria.setOpreatingSystem(new String[]{value});
                                    break;
                                case "Touch_Screen":
                                    searchCriteria.setTouchScreen(value);
                                    break;
                                case "Bluetooth":
                                    searchCriteria.setBluetooth(value);
                                    break;
                                case "WiFi":
                                    searchCriteria.setWiFi(value);
                                    break;
                                case "Processor":
                                    searchCriteria.setProcessor(value);
                                    break;
                            }
                        }
                    }
                }
                break;
          /*  case 3:
                // 3 是获取商品详情页数据
                searchCriteria.setKeyword(siteMapKeyVo.getName());
                break;*/
            default:
        }
        try {
            pageableResult = appSearchService.filterByParams(searchCriteria);
        } catch (Exception e) {
            logger.error(" error  message : {}  threadId :  time: ", e.getMessage(), Thread.currentThread().getId(), new Date());
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, "10000");
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "error ,please try again later.");
            return modelAndView;
        }
        if (pageableResult != null && pageableResult.getData().size() > 0) {
            apiUtils.addProductVo2List(ProductList, pageableResult.getData());
            resultVo.getData().put("pList", ProductList);
            modelAndView.addObject("data", resultVo.getData());

        }
        return modelAndView;
    }
}
