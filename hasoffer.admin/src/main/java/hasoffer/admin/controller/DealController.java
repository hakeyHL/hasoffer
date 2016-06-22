package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.po.app.AppDeal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public ModelAndView listDealData(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int size){
        ModelAndView mov = new ModelAndView("deal/list");
        PageableResult<AppDeal> pageableResult = dealService.findDealList(page, size);

        return mov;
    }

    /**
     * excel导入
     * @param multiFile
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> importExcel(MultipartFile multiFile, HttpServletRequest request){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            dealService.importExcelFile(multiFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
