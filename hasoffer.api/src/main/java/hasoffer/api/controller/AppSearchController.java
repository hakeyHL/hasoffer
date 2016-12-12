package hasoffer.api.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.app.AppSearchService;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.utils.api.ApiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年12月12日.
 * Time 11:06
 * 专为搜索服务
 */
@Controller
@RequestMapping("app/search")
public class AppSearchController {
    @Autowired
    AppSearchService appSearchService;
    @Autowired
    ApiUtils apiUtils;

    @RequestMapping("catefilter")
    public ModelAndView stdSkuCategoryFilter(@RequestBody SearchCriteria searchCriteria) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "success");
        List ptmStdSkuList = new ArrayList();
        Map map = new HashMap<>();
        //1. 非空校验
        if (searchCriteria.getCategoryId() == null) {
            modelAndView.addObject("errorCode", "10000");
            modelAndView.addObject("msg", "failed , categoryId can not be empty .");
            return modelAndView;
        }
        //2. 合法性校验
        if (searchCriteria.getLevel() < 1 || searchCriteria.getLevel() > 3) {
            modelAndView.addObject("errorCode", "10000");
            modelAndView.addObject("msg", "failed , level less than one .");
            return modelAndView;
        }

        //3. 业务逻辑
        PageableResult<PtmStdSkuModel> pageableResult = appSearchService.filterByParams(searchCriteria);

        if (pageableResult != null && pageableResult.getData().size() > 0) {
            apiUtils.addProductVo2List(ptmStdSkuList, pageableResult.getData());
            map.put("numberFound", pageableResult.getNumFund());
            map.put("currentPage", pageableResult.getCurrentPage());
            map.put("totalPage", pageableResult.getTotalPage());
            map.put("pageSize", pageableResult.getPageSize());
        }

        if (ptmStdSkuList.size() < 1) {
            modelAndView.addObject("errorCode", "10000");
            modelAndView.addObject("msg", "failed , size is zero .");
            return modelAndView;
        }
        map.put("product", ptmStdSkuList);
        //4. 返回结果
        modelAndView.addObject("data", map);
        return modelAndView;
    }
}
