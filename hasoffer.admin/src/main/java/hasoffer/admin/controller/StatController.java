package hasoffer.admin.controller;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.product.SkuUpdateResult;
import hasoffer.core.bo.product.SkuUpdateResult2;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.persistence.po.stat.StatCmpCategory;
import hasoffer.core.persistence.po.stat.StatSkuUpdateResult;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import hasoffer.manager.SkuUpdateStatManager;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
    @Resource
    SkuUpdateStatManager skuUpdateStatManager;
    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(StatController.class);

    @RequestMapping(value = "/stat_cmp_category", method = RequestMethod.GET)
    public void stat_cmp_category(final String ymd) {

        final String Q_SEARCHCOUNT = "SELECT t FROM SrmProductSearchCount t WHERE t.ymd=?0";

        final Map<Long, AtomicLong> cateCount = new HashMap<>();

        ListProcessTask<SrmProductSearchCount> statTask = new ListProcessTask<>(
                new ILister() {
                    @Override
                    @DataSource(DataSourceType.Slave)
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_SEARCHCOUNT, page, 1000, Arrays.asList(ymd));
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcessor<SrmProductSearchCount>() {
                    @Override
                    public void process(SrmProductSearchCount o) {
                        PtmProduct product = productService.getProduct(o.getProductId());

                        if (product == null) {
                            return;
                        }

                        long categoryid = product.getCategoryId();

                        if (categoryid <= 0) {
//                            categoryid = 0;
                            return;
                        }

                        AtomicLong count = cateCount.get(categoryid);
                        if (count == null) {
                            count = new AtomicLong(o.getCount());
                            cateCount.put(categoryid, count);
                        } else {
                            count.addAndGet(o.getCount());
                        }
                    }
                }
        );

        statTask.go();

        while (!statTask.isAllFinished()) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long totalCount = 0;
        for (Map.Entry<Long, AtomicLong> kv : cateCount.entrySet()) {
            totalCount += kv.getValue().get();
        }

        for (Map.Entry<Long, AtomicLong> kv : cateCount.entrySet()) {
            long cateid = kv.getKey();
            long count = kv.getValue().get();

            float per = (float) count / totalCount;

            if (per < 0.01) {
                continue;
            }

            PtmCategory category = categoryService.getCategory(cateid);

            // ymd, cateid, catename, count, totalcount
            searchService.saveStatCmpCategory(new StatCmpCategory(ymd, cateid, category.getName(), count, totalCount));
        }
    }

    @RequestMapping(value = "/show_updates", method = RequestMethod.GET)
    public ModelAndView show_updates(@RequestParam(defaultValue = "") String ymd) {
        if (StringUtils.isEmpty(ymd)) {
            ymd = TimeUtils.parse(TimeUtils.today(), "yyyyMMdd");
        }

        SkuUpdateResult2 skuUpdateResult2 = searchLogCacheManager.getStatResult(ymd);

        ModelAndView mav = new ModelAndView("showstat/sku_update_status");
        mav.addObject("updateRst", skuUpdateResult2);

        long wait4UpdateProduct = productCacheManager.getWait4UpdateProductCount(ymd);
        long updateProcessd = productCacheManager.getUpdateProcessdProductCount(ymd);

        mav.addObject("wait4Update", wait4UpdateProduct);
        mav.addObject("updateProcessd", updateProcessd);

        List<StatSkuUpdateResult> skuUpdateResults = cmpSkuService.getSkuUpdateResults();
        mav.addObject("updateRst2", skuUpdateResults);

        return mav;
    }

    @RequestMapping(value = "/sku_update_status_today", method = RequestMethod.GET)
    public ModelAndView sku_update_status_today() {
        skuUpdateStatManager.statUpdateResultToday();

        ModelAndView mav = new ModelAndView("redirect:/stat/show_updates");
        return mav;
    }

    @RequestMapping(value = "/show_failed_update_skus", method = RequestMethod.GET)
    public ModelAndView show_failed_update_skus(@RequestParam(defaultValue = "1") int start,
                                                @RequestParam(defaultValue = "100") int count) {
        List<String> ids = cmpSkuCacheManager.getFailedUpdate(start, count);

        List<PtmCmpSku> cmpSkus = new ArrayList<>();

        for (String id : ids) {
            PtmCmpSku cmpSku = cmpSkuService.getCmpSkuById(Long.valueOf(id));
            cmpSkus.add(cmpSku);
        }

        ModelAndView mav = new ModelAndView("showstat/show_fail");
        mav.addObject("skus", cmpSkus);
        return mav;
    }

    @RequestMapping(value = "/sku_update_result", method = RequestMethod.GET)
    public
    @ResponseBody
    String sku_update_result(@RequestParam(defaultValue = "") String ymd) {
        if (StringUtils.isEmpty(ymd)) {
            ymd = TimeUtils.parse(TimeUtils.yesterday(), "yyyyMMdd");
        }

        SkuUpdateResult skuUpdateResult = skuUpdateStatManager.statUpdateResult(ymd);

        cmpSkuService.saveSkuUpdateResult(skuUpdateResult);

        return "ok";
    }

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
