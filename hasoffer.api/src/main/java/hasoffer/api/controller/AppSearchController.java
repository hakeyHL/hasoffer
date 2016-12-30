package hasoffer.api.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.app.AppSearchService;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.utils.ConstantUtil;
import hasoffer.core.utils.api.ApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

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
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        List ptmStdSkuList = new ArrayList();
        Map map = new HashMap<>();
        //1. 非空校验
        if (searchCriteria.getCategoryId() == null) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "failed , categoryId can not be empty .");
            return modelAndView;
        }
        //2. 合法性校验
        if (searchCriteria.getLevel() < 1 || searchCriteria.getLevel() > 3) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "failed , level less than one .");
            return modelAndView;
        }

        //3. 业务逻辑
        searchCriteria.setPivotFields(Arrays.asList("Network_Support",
                "Screen_Resolution", "Operating_System", "queryRam",
                "queryScreenSize", "querySecondaryCamera",
                "queryBatteryCapacity", "queryPrimaryCamera",
                "queryInternalMemory", "Brand"));
        PageableResult pageableResult = appSearchService.filterByParams(searchCriteria);

        if (pageableResult != null && pageableResult.getData().size() > 0) {
            apiUtils.addProductVo2List(ptmStdSkuList, pageableResult.getData());
            map.put("numberFound", pageableResult.getNumFund());
            map.put("currentPage", pageableResult.getCurrentPage() <= 1 ? 1 : pageableResult.getCurrentPage());
            map.put("totalPage", pageableResult.getTotalPage());
            map.put("pageSize", pageableResult.getPageSize());

            //处理facet返回
            map.put("pivos", pageableResult.getPivotFieldVals());
            ApiUtils.resolvePivotFields(map, pageableResult, pageableResult.getPivotFieldVals());
        }
        if (ptmStdSkuList.size() < 1) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "failed , size is zero .");
            return modelAndView;
        }
        map.put("product", ptmStdSkuList);
        //4. 返回结果
        modelAndView.addObject(ConstantUtil.API_NAME_DATA, map);
        return modelAndView;
    }

    /**
     * 获取筛选参数
     *
     * @param searchCriteria
     * @return
     */
    @RequestMapping("filterParams")
    public ModelAndView getFilterParams(SearchCriteria searchCriteria) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        Map map = new HashMap<>();
        map.put("access", false);
        //1. 正确的逻辑是按照facet参数查询有没有结果来告知是否可以参数筛选
        //2. 现在是只是类目id是5,级别是2的筛选
        if (StringUtils.isEmpty(searchCriteria.getCategoryId()) || searchCriteria.getLevel() < 1) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "filed , categoryId or level required.");
            return modelAndView;
        }

        if (!"5".equals(searchCriteria.getCategoryId()) && searchCriteria.getLevel() != 2) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "filed , categoryId or level not accessed.");
            return modelAndView;
        }
        // 关键词为空也拒绝
        if (StringUtils.isEmpty(searchCriteria.getKeyword())) {
            modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
            modelAndView.addObject(ConstantUtil.API_NAME_MSG, "filed , keyword is required.");
            return modelAndView;
        }
        //3. 在关键词搜索、筛选时预先放入缓存,在这里获取
        searchCriteria.setPivotFields(Arrays.asList("Network",
                "Screen_Resolution", "Operating_System", "queryRam",
                "queryScreenSize", "querySecondaryCamera",
                "queryBatteryCapacity", "queryPrimaryCamera",
                "queryInternalMemory", "brand"));

        PageableResult pageableResult = appSearchService.filterByParams(searchCriteria);
        if (pageableResult != null && pageableResult.getPivotFieldVals() != null && pageableResult.getPivotFieldVals().size() > 0) {
            map.put("access", true);
            map.put("pivos", pageableResult.getPivotFieldVals());
            ApiUtils.resolvePivotFields(map, pageableResult, pageableResult.getPivotFieldVals());
        }
        modelAndView.addObject(ConstantUtil.API_NAME_DATA, map);
        return modelAndView;
    }
}
