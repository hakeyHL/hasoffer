package hasoffer.api.controller;

import hasoffer.core.cache.AppCacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by hs on 2016年12月19日.
 * 用于处理类目请求
 * Time 17:11
 */
@Controller
public class AppCategoryController {
    @Resource
    private AppCacheManager appCacheManager;

    /**
     * 商品类目
     *
     * @return
     */
    @RequestMapping(value = "app/category", method = RequestMethod.GET)
    public ModelAndView category(String categoryId) {
        ModelAndView mv = new ModelAndView();
        List categorys = null;
        categorys = appCacheManager.getCategorys(categoryId);
        mv.addObject("data", categorys);
        return mv;
    }

}
