package hasoffer.admin.controller;

import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/1/22
 * Function :
 */
@Controller
@RequestMapping(value = "/stat")
public class StatController {

    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ISearchService searchService;
    @Resource
    IProductService productService;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    CmpSkuCacheManager cmpSkuCacheManager;
    @Resource
    ProductCacheManager productCacheManager;
    private Logger logger = LoggerFactory.getLogger(StatController.class);

    @RequestMapping(value = "/statByHour", method = RequestMethod.GET)
    public
    @ResponseBody
    String statSearchCountByHour(@RequestParam(defaultValue = "") String ymd_hour) {

        if (StringUtils.isEmpty(ymd_hour)) {
            ymd_hour = TimeUtils.parse(TimeUtils.add(TimeUtils.nowDate(), TimeUtils.MILLISECONDS_OF_1_HOUR * -1), "yyyyMMdd_HH");
        }

        logger.info("statSearchCountByHour : " + ymd_hour);

        Map<Long, Long> countMap = searchLogCacheManager.getProductCount(ymd_hour);

        if (countMap.size() > 0) {
            logger.info("delete if exists old stats");
            searchService.delSearchCountByHour(ymd_hour);
        }

        logger.info("to save search count");
        for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

            long productId = countKv.getKey();
            long searchCount = countKv.getValue();

            List<PtmCmpSku> cmpSkus = cmpSkuCacheManager.listCmpSkus(productId, SkuStatus.ONSALE);

            int size = cmpSkus.size();

            searchService.saveSearchCountByHour(ymd_hour, productId, searchCount, size);

            productCacheManager.put2UpdateQueue(productId);
//            productService.importProduct2Solr2(productId);
        }

        return "ok";
    }

    /*
    logger.debug(String.format("save search count [%s]", ymd));

    List<SrmProductSearchCount> spscs = new ArrayList<SrmProductSearchCount>();



    int count = 0;
    for (Map.Entry<Long, Long> countKv : countMap.entrySet()) {

        long productId = countKv.getKey();
        long searchCount = countKv.getValue();

        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId, SkuStatus.ONSALE);
        int size = 0;
        if (ArrayUtils.hasObjs(cmpSkus)) {
            size = cmpSkus.size();
        }

        spscs.add(new SrmProductSearchCount(ymd, productId, searchCount, size));

        if (count % 2000 == 0) {
            saveLogCount(spscs);
            count = 0;
            spscs.clear();
        }

        productService.importProduct2Solr2(productId);
    }

    if (ArrayUtils.hasObjs(spscs)) {
        saveLogCount(spscs);
    }*/

    @RequestMapping(value = "/stat", method = RequestMethod.GET)
    public
    @ResponseBody
    String statSearchCount(@RequestParam String ymd) {
        if (StringUtils.isEmpty(ymd)) {
            ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");
        }

        searchService.saveSearchCount_old(ymd);

        productService.expTopSellingsFromSearchCount(ymd);

        searchService.statSearchCount_old(ymd);

        return "ok";
    }

    @RequestMapping(value = "/monitor", method = RequestMethod.GET)
    public ModelAndView skupriceupdateresult() {
        ModelAndView mav = new ModelAndView("system/monitor");

        mav.addObject("results", cmpSkuService.listUpdateResults());

        return mav;
    }


    @RequestMapping(value = "/cmpskupriceupdate/restat", method = RequestMethod.GET)
    public ModelAndView restat(HttpServletRequest request,
                               @RequestParam(defaultValue = "false") boolean all,
                               @RequestParam(defaultValue = "") String day) {

        if (all) {
            String symd = day;
            if (StringUtils.isEmpty(symd)) {
                PtmCmpSkuLog ptmCmpSkuLog = mdm.queryOne(PtmCmpSkuLog.class);
                Date date = ptmCmpSkuLog.getPriceTime();
                symd = TimeUtils.parse(date, TimeUtils.PATTERN_YMD);
            }

            String endYmd = TimeUtils.parse(TimeUtils.today(), TimeUtils.PATTERN_YMD);

            while (symd.compareTo(endYmd) < 0) {
                cmpSkuService.saveOrUpdateSkuPriceUpdateResult(cmpSkuService.countUpdate(symd));
                symd = TimeUtils.parse(TimeUtils.stringToDate(symd, TimeUtils.PATTERN_YMD).getTime() + TimeUtils.MILLISECONDS_OF_1_DAY, "yyyyMMdd");
            }
        } else {
            cmpSkuService.saveOrUpdateSkuPriceUpdateResult(cmpSkuService.countUpdate(day));
        }

        ModelAndView mav = new ModelAndView("system/ok");
        return mav;
    }

}
