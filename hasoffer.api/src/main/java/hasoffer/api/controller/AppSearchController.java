package hasoffer.api.controller;

import hasoffer.core.bo.system.SearchCriteria;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by hs on 2016年12月12日.
 * Time 11:06
 * 专为搜索服务
 */
@Controller
@RequestMapping("app/search")
public class AppSearchController {
    @RequestMapping("catefilter")
    public ModelAndView stdSkuCategoryFilter(@RequestBody SearchCriteria searchCriteria) {

        return null;
    }
}
