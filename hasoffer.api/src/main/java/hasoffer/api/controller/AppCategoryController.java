package hasoffer.api.controller;

import hasoffer.core.app.AppCategoryService;
import hasoffer.core.bo.product.CategoryVo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年12月19日.
 * 用于处理类目请求
 * Time 17:11
 */
@Controller
public class AppCategoryController {
    @Resource
    private AppCategoryService appCategoryService;

    /**
     * 商品类目
     *
     * @return
     */
    @RequestMapping(value = "app/category", method = RequestMethod.GET)
    public ModelAndView category(String categoryId) {
        ModelAndView mv = new ModelAndView();
        List categorys;
        categorys = appCategoryService.getCategorys(categoryId);
        mv.addObject("data", categorys);
        return mv;
    }

    /**
     * 获取热门类目列表
     *
     * @return
     */
    @RequestMapping(value = "app/topCategory", method = RequestMethod.GET)
    public ModelAndView getTopCategory() {
        ModelAndView mv = new ModelAndView();
        mv.addObject("errorCode", "00000");
        mv.addObject("msg", "success");
        List<CategoryVo> categoryVos = appCategoryService.getTopCategoryList();
        Map dataMap = new HashMap<>();
        dataMap.put("topcates", categoryVos);
        mv.addObject("data", dataMap);
        return mv;
    }
}
