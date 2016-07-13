package hasoffer.timer.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.product.IFetchService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.task.ListAndProcessTask2;
import hasoffer.core.task.worker.IList;
import hasoffer.core.task.worker.IProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Resource
    IDataBaseManager dbm;
    @Resource
    IFetchService fetchService;

    private Logger logger = LoggerFactory.getLogger(TimerController.class);

    // http://web3:8020/timer/statsearchlog3/20160710
    @RequestMapping(value = "/fixImage", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixImage() {
        final String Q_PRODUCT_WEBSITE =
                "SELECT t FROM PtmProduct t WHERE t.sourceSite='FLIPKART'";

        ListAndProcessTask2<PtmProduct> listAndProcessTask2 = new ListAndProcessTask2<PtmProduct>(
                new IList<PtmProduct>() {
                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_PRODUCT_WEBSITE, page, 500);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcess<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        try {
                            // update image for product
                            String sourceUrl = o.getSourceUrl();
                            // visit flipkart page to get image url
                            String oriImageUrl = fetchService.fetchFlipkartImageUrl(sourceUrl);

                            productService.updateProductImage2(o.getId(), oriImageUrl);

                        } catch (Exception e) {
                            logger.debug(e.getMessage() + "\t" + o.getId());
                        }
                    }
                }
        );

        listAndProcessTask2.go();

        return "ok";
    }

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
