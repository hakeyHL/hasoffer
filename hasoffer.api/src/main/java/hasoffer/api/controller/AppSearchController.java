package hasoffer.api.controller;

import hasoffer.core.app.AppSearchService;
import hasoffer.core.app.vo.ProductListVo;
import hasoffer.core.bo.system.SearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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

    @RequestMapping("catefilter")
    public ModelAndView stdSkuCategoryFilter(@RequestBody SearchCriteria searchCriteria) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("errorCode", "00000");
        modelAndView.addObject("msg", "success");

        //1. 非空校验
        if (searchCriteria.getCategoryId() == null) {
            modelAndView.addObject("errorCode", "10000");
            modelAndView.addObject("msg", "failed , categoryId can not be empty .");
        }
        //2. 合法性校验
        if (searchCriteria.getLevel() < 1) {
            modelAndView.addObject("errorCode", "10000");
            modelAndView.addObject("msg", "failed , level less than one .");
        }

        //3. 业务逻辑
        List<ProductListVo> productListVoList = appSearchService.filterByParams(searchCriteria);
        //4. 返回结果
        return null;
    }
}
