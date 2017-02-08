package hasoffer.api.controller;

import hasoffer.api.helper.Httphelper;
import hasoffer.core.third.impl.ThirdServiceImple;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by hs on 2016/7/4.
 */
@Controller
@RequestMapping(value = "/third")
public class ThirdPartyController {
    @Resource
    ThirdServiceImple thridPartyService;

    /**
     * provide API to get deals for Gmobi
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/api/deals", method = RequestMethod.POST)
    public String config(HttpServletRequest request, HttpServletResponse response) {
        String acceptJson = Httphelper.getJsonFromRequest(request);
//        logger.info("accept content is " + acceptJson);
        String result = thridPartyService.getDeals(acceptJson);
        Httphelper.sendJsonMessage(result, response);
        return null;
    }

    //offer for india

    /**
     * get方式获取deal列表数据
     *
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/india", method = RequestMethod.GET)
    public String getDealsForIndia(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   HttpServletResponse response) {
        String dealsForIndia = thridPartyService.getDealsForIndia(page, pageSize);
        Httphelper.sendJsonMessage(dealsForIndia, response);
        return null;
    }

    @RequestMapping(value = "/offer/dealInfo/{id}", method = RequestMethod.GET)
    public String getDealsForIndia(@RequestParam(defaultValue = "1") @PathVariable String id,
                                   HttpServletResponse response) {
        String dealInfoForIndia = thridPartyService.getDealInfoForIndia(id);
        Httphelper.sendJsonMessage(dealInfoForIndia, response);
        return null;
    }

    /**
     * mexico offer list
     *
     * @param page
     * @param pageSize
     * @param response
     * @return
     */
    @RequestMapping(value = "/offers/mexico", method = RequestMethod.GET)
    public String getDealsForMexico(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int pageSize,
                                    HttpServletResponse response) {
        String dealsForMexico = thridPartyService.getDealsForMexico(page, pageSize, new String[]{"discount"});
        Httphelper.sendJsonMessage(dealsForMexico, response);
        return null;
    }
}
