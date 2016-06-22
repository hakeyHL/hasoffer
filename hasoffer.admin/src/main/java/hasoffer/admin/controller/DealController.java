package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.po.app.AppDeal;
import hasoffer.webcommon.helper.PageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lihongde on 2016/6/21 12:47
 */

@Controller
@RequestMapping(value="/deal")
public class DealController {

    @Resource
    IDealService dealService;

    @RequestMapping(value="/list", method = RequestMethod.GET)
    public ModelAndView listDealData(HttpServletRequest request, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size){
        ModelAndView mav = new ModelAndView("deal/list");
        PageableResult<AppDeal> pageableResult = dealService.findDealList(page, size);
        mav.addObject("page", PageHelper.getPageModel(request, pageableResult));
        mav.addObject("datas", pageableResult.getData());
        return mav;
    }

    /**
     * excel导入
     * @param multiFile
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> importExcel(MultipartFile multiFile){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            result = dealService.importExcelFile(multiFile);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value="/getDealById/{id}", method = RequestMethod.GET)
    public ModelAndView editDeal(@PathVariable(value = "id") Long dealId){
        ModelAndView mav = new ModelAndView("deal/edit");
        mav.addObject("deal", dealService.getDealById(dealId));
        return mav;
    }

}
