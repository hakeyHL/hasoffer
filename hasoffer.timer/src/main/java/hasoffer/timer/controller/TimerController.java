package hasoffer.timer.controller;

import hasoffer.core.cache.SearchLogCacheManager;
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
    @Resource
    SearchLogCacheManager searchLogCacheManager;

    // http://web3:8020/timer/statsearchlog3/20160710

    @RequestMapping(value = "/statsearchlog1/{ymd}", method = RequestMethod.GET)
    public
    @ResponseBody
    String f1(@PathVariable String ymd) {

        for (int i = 0; i < 100; i++) {
            searchLogCacheManager.countSearchedProduct(i);
        }

        // 保存所有被搜索过的商品
        searchService.saveSearchCount(ymd);

        return "ok";
    }

    @RequestMapping(value = "/statsearchlog2/{ymd}", method = RequestMethod.GET)
    public
    @ResponseBody
    String f2(@PathVariable String ymd) {

        // top selling
        productService.expTopSellingsFromSearchCount(ymd);

        return "ok";
    }

    @RequestMapping(value = "/statsearchlog3/{ymd}", method = RequestMethod.GET)
    public
    @ResponseBody
    String f3(@PathVariable String ymd) {

        searchService.statSearchCount(ymd);

        return "ok";
    }


}
