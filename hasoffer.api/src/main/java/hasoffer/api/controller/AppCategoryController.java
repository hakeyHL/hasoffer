package hasoffer.api.controller;

import hasoffer.core.app.AppCategoryService;
import hasoffer.core.bo.product.CategoryVo;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.utils.ConstantUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by hs on 2016年12月19日.
 * 用于处理类目请求
 * Time 17:11
 */
@Controller
public class AppCategoryController {
    @Resource
    AppServiceImpl appService;
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
    @RequestMapping(value = "app/category/topcates", method = RequestMethod.GET)
    public ModelAndView getTopCategory() {
        ModelAndView mv = new ModelAndView();
        mv.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        mv.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_ERRORCODE_SUCCESS_MSG);
        List<CategoryVo> categoryVos = appCategoryService.getTopCategoryList();
        Map dataMap = new HashMap<>();
        //只给8个
        dataMap.put("topcates", categoryVos.subList(0, 8));
        mv.addObject("data", dataMap);
        return mv;
    }

    /**
     * 获取所有二级类目列表
     *
     * @return
     */
    @RequestMapping(value = "app/category/secondary", method = RequestMethod.GET)
    public ModelAndView getSecondaryCate() {
        ModelAndView mv = new ModelAndView();
        mv.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        mv.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_ERRORCODE_SUCCESS_MSG);
        List<CategoryVo> topSecondaryCates = new LinkedList<>();
        //只要前7个
        List<CategoryVo> topCates = appCategoryService.getTopCategoryList();

        topSecondaryCates.addAll(topCates.subList(0, 7));

        //如果list中存在top里面的名称,则去掉
        List<PtmCategory> secondaryList = appCategoryService.getSecondaryList();
        secondaryList.sort(new Comparator<PtmCategory>() {
            @Override
            public int compare(PtmCategory o1, PtmCategory o2) {
                if (o1.getName().compareTo(o2.getName()) < 0) {
                    return -1;
                }
                if (o1.getName().compareTo(o2.getName()) > 0) {
                    return 1;
                }
                return 0;
            }
        });
        long sss = System.currentTimeMillis();
        Iterator<PtmCategory> iterator = secondaryList.iterator();
        while (iterator.hasNext()) {
            PtmCategory next = iterator.next();
            //处理成Vo
            CategoryVo categoryVo = new CategoryVo(next);
            int childCount = appCategoryService.getChildCates(next.getId());
            if (childCount < 1) {
                categoryVo.setHasChildren(0);
            }
            if (!topSecondaryCates.contains(categoryVo)) {
                //最后合并
                topSecondaryCates.add(categoryVo);
            }
        }
        Map dataMap = new HashMap<>();
        dataMap.put("secondaryCate", topSecondaryCates);
        mv.addObject("data", dataMap);
        long eee = System.currentTimeMillis();
        System.out.println(eee - sss);
        return mv;
    }
}
