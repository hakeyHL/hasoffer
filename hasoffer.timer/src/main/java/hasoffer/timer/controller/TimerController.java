package hasoffer.timer.controller;

import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created on 2015/12/24.
 */
@Controller
@RequestMapping(value = "/timer")
public class TimerController {

    @Resource
    ISearchService searchService;
    @Resource
    IProductService productService;

    @RequestMapping(value = "/statsearchlog/{ymd}", method = RequestMethod.GET)
    public
    @ResponseBody
    String f(@PathVariable String ymd) {

        // 保存所有被搜索过的商品
        searchService.saveSearchCount(ymd);

        // top selling
        productService.expTopSellingsFromSearchCount(ymd);

        return "ok";
    }

}
