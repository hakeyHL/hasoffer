package hasoffer.api.controller;

import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageableResult;
import hasoffer.core.app.AppSearchService;
import hasoffer.core.app.vo.ResultVo;
import hasoffer.core.app.vo.mobile.SiteMapKeyVo;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.po.ptm.PtmStdSku;
import hasoffer.core.product.impl.PtmStdSKuServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
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

    Logger logger = LoggerFactory.getLogger(MobileController.class);

    @RequestMapping("siteMap")
    public ModelAndView siteMapHasoffer(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "2000") int pageSize) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "success");

        Map<String, List> keyMap = new HashMap();
        //key 1
        Map proMap = new HashMap();
        keyMap.put("Mobile Finder On Hasoffer", Arrays.asList(new SiteMapKeyVo("Latest Mobiles", 0)));

        //key 2
        proMap.clear();

        //获取categoryId 为5  level 为2 的所有商品
        List<SiteMapKeyVo> stdSkuKeyVoList = new ArrayList<>();
        stdSkuKeyVoList.add(new SiteMapKeyVo("Top Mobile Phones", 0));
        PageableResult<PtmStdSku> ptmStdSkuList = ptmStdSKuService.getPtmStdSkuListByMinId(0l, page, pageSize);
        for (PtmStdSku ptmStdSku : ptmStdSkuList.getData()) {
            stdSkuKeyVoList.add(new SiteMapKeyVo(ptmStdSku.getTitle(), 3).buildePid(ptmStdSku.getId()));
        }
        keyMap.put("All Mobile Models In India", stdSkuKeyVoList);
        //key 3
        keyMap.put("Top 10 Mobiles", Arrays.asList(
                new SiteMapKeyVo("Top 10  Mobiles  Below 5000", 2).builderProMap("price", "5000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 10000", 2).builderProMap("price", "10000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 15000", 2).builderProMap("price", "15000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 20000", 2).builderProMap("price", "20000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 25000", 2).builderProMap("price", "25000"),
                new SiteMapKeyVo("Top 10  Mobiles  Below 30000", 2).builderProMap("price", "30000"),

                new SiteMapKeyVo("Top 10 Htc Desire Series Mobiles", 2).buildeShortName("Htc Desire Series"),
                new SiteMapKeyVo("Top 10 Sony Xperia Series Mobiles", 2).buildeShortName("Sony Xperia Series"),
                new SiteMapKeyVo("Top 10 Samsung Galaxy Series Mobiles", 2).buildeShortName("Samsung Galaxy Series"),
                new SiteMapKeyVo("Top 10 Lg Optimus Series Mobiles", 2).buildeShortName("Lg Optimus Series"),
                new SiteMapKeyVo("Top 10 Nokia Lumia Series Mobiles", 2).buildeShortName("Nokia Lumia Series"),
                new SiteMapKeyVo("Top 10 Nokia Asha Series Mobiles", 2).buildeShortName("Nokia Asha Series"),
                new SiteMapKeyVo("Top 10 T Series Mobiles", 2).buildeShortName("T Series"),
                new SiteMapKeyVo("Top 10 T Series Camera Mobiles", 2).buildeShortName("T Series"),
                new SiteMapKeyVo("Top 10 T Series Dual Sim Mobiles", 2).buildeShortName("T Series"),
                //Top 10 + “品牌名称” + Mobiles + Below +“价格参数”
                new SiteMapKeyVo("Top 10 HTC Mobiles Below 50000", 2).builderProMap("price", "5000").builderProMap("brand", "HTC")
        ));
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
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "success");

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
                            if (key.equals("brand")) {
                                searchCriteria.setBrand(new String[]{key});
                            }
                            if (key.equals("price")) {
                                searchCriteria.setPriceFrom(1);
                                searchCriteria.setPriceTo(Integer.parseInt(value));
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
            modelAndView.addObject("errorCode", "10000");
            modelAndView.addObject("msg", "error ,please try again later.");
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
