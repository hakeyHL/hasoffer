package hasoffer.admin.controller;

import hasoffer.core.admin.IDealService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
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
        ModelAndView mov = new ModelAndView("deal/listData");


        return mov;
    }

    /**
     * excel导入
     * @param multiFile
     * @return
     */
    @RequestMapping(value = "import")
    @ResponseBody
    public Map<String, Object> importExcel(MultipartFile multiFile){
        Map<String, Object> result = new HashMap<String, Object>();

        return result;
    }

}
