package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.ExceptionHelper;
import hasoffer.api.helper.Httphelper;
import hasoffer.api.helper.SearchHelper;
import hasoffer.base.model.AppDisplayMode;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.exception.ERROR_CODE;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.PtmProductDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.iml.ProductServiceImpl;
import hasoffer.core.product.solr.CategoryIndexServiceImpl;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.exception.NonMatchedProductException;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.JsonHelper;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import hasoffer.webcommon.helper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created on 2015/12/21.
 */
@Controller
@RequestMapping(value = "/cmp")
public class Compare2Controller {
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;
    @Resource
    ProductIndexServiceImpl productIndexService;
    @Resource
    CategoryIndexServiceImpl categoryIndexService;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    CmpSkuCacheManager cmpSkuCacheManager;
    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    ISearchService searchService;
    @Resource
    ProductServiceImpl productService;
    @Resource
    IMongoDbManager mongoDbManager;
    @Resource
    AppServiceImpl appService;
    private Logger logger = LoggerFactory.getLogger(Compare2Controller.class);

    public static void main(String[] args) {
//        String dealUrlWithAff = WebsiteHelper.getDealUrlWithAff(Website.SHOPCLUES, "http://www.shopclues.com/reach-allure-speed.html", new String[]{MarketChannel.GOOGLEPLAY.name(), "asd123gfd654"});
//        String urlWithAff = WebsiteHelper.getUrlWithAff(Website.SHOPCLUES, "http://www.shopclues.com/reach-allure-speed.html", new String[]{MarketChannel.GOOGLEPLAY.name(), "asd123gfd654"});
//        System.out.println(urlWithAff);
//        String flipkart = WebsiteHelper.getDealUrlWithAff(Website.FLIPKART, "https://www.flipkart.com/apple-iphone-6s-silver-16-gb/p/itmebysgupjepunx", new String[]{MarketChannel.GOOGLEPLAY.name(), "asd123gfd654"});
//        System.out.println(flipkart);
//        String deeplinkWithAff = WebsiteHelper.getDeeplinkWithAff(Website.SHOPCLUES, "http://www.shopclues.com/reach-allure-speed.html", new String[]{MarketChannel.GOOGLEPLAY.name(), "asd123gfd654"});
//        System.out.println(deeplinkWithAff);
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        map.put(1l, 4);
        map.put(3l, 100);
        map.put(2l, 3);
        Set<Long> longs = map.keySet();
        int t = 0;
        int t1 = 0;
        Iterator<Long> iterator = longs.iterator();
        while (iterator.hasNext()) {
            Long next = iterator.next();
            t += map.get(next);
            t1 += map.get(next) * next;
        }
        System.out.println(t1);
        System.out.println(t);
        System.out.println(BigDecimal.valueOf(t).divide(BigDecimal.valueOf(10), 1, BigDecimal.ROUND_HALF_UP));
        BigDecimal s = BigDecimal.ZERO;
        Set<Long> long2 = map.keySet();
        Iterator<Long> iterator1 = long2.iterator();
        while (iterator1.hasNext()) {
            Long next = iterator1.next();
            BigDecimal ss = BigDecimal.valueOf(map.get(next)).divide(BigDecimal.valueOf(t), 1, BigDecimal.ROUND_HALF_UP);
            s = s.add(ss.multiply(BigDecimal.valueOf(next)));
        }
        System.out.println(s.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP));
    }

    // @Cacheable(value = "compare", key = "'getcmpskus_'+#q+'_'+#site+'_'+#price+'_'+#page+'_'+#size")
    // Model And View 不是可序列化的 会抛出  java.io.NotSerializableException 异常
    @RequestMapping(value = "/getcmpskus", method = RequestMethod.GET)
    public ModelAndView getcmpskus(HttpServletRequest request,
                                   @RequestParam(defaultValue = "") final String q,
                                   @RequestParam(defaultValue = "") final String brand,
                                   @RequestParam(defaultValue = "") final String sourceId,
                                   @RequestParam(defaultValue = "") String site,
                                   @RequestParam(defaultValue = "0") String price,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);

        SearchIO sio = new SearchIO(sourceId, q, brand, site, price, deviceInfo.getMarketChannel(), deviceId, page, size);
        CmpResult cr = null;

        PtmCmpSkuIndex2 cmpSkuIndex = null;

        try {
            // 先去匹配sku
            cmpSkuIndex = cmpSkuCacheManager.getCmpSkuIndex2(sio.getDeviceId(), sio.getCliSite(), sio.getCliSourceId(), sio.getCliQ());
            getSioBySearch(sio);
            cr = getCmpResult(sio, cmpSkuIndex);
        } catch (Exception e) {
            if (sio.getHsProId() > 0) {
                PtmProduct ptmProduct = productService.getProduct(sio.getHsProId());
                if (ptmProduct == null) {
                    productService.deleteProduct(sio.getHsProId());
                } else {
                    //logger.info(ptmProduct.toString());
                }

            }
            //logger.error(e.getMessage());
            //  logger.error(String.format("[NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", q, site, price, page, size));

            cr = getDefaultCmpResult(sio, cmpSkuIndex);
        }
        // 速度优化
        SearchHelper.addToLog(sio);

        ModelAndView mav = new ModelAndView();

        mav.addObject("priceOff", cr.getPriceOff());
        mav.addObject("product", cr.getProductVo());
        mav.addObject("skus", cr.getPagedComparedSkuVos().getData());
        mav.addObject("page", PageHelper.getPageModel(request, cr.getPagedComparedSkuVos()));
        mav.addObject("newLayout", false);

//        logger.info(sio.toString());

        return mav;
    }

    /**
     * 根据商品获取比价的sku列表
     *
     * @return
     */
    @RequestMapping("sdk/cmpskus")
    public String cmpSkus(@RequestParam(defaultValue = "") final String q,
                          @RequestParam(defaultValue = "") final String brand,
                          @RequestParam(defaultValue = "") final String sourceId,
                          @RequestParam(defaultValue = "") String site,
                          @RequestParam(defaultValue = "0") String price,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int pageSize,
                          HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"ratingNum", "bestPrice", "priceOff", "backRate", "support", "price", "returnGuarantee", "freight"});
        //初始化sio对象
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        CmpResult cr = null;
        PtmProduct ptmProduct = null;
        SearchIO sio = new SearchIO(sourceId, q, brand, site, price, deviceInfo.getMarketChannel(), deviceId, page, pageSize);
        try {
            //根据title匹配到商品
            getSioBySearch(sio);
            logger.info("get product from solr or searchLog ");
            if (sio.getHsProId() > 0) {
                ptmProduct = productService.getProduct(sio.getHsProId());
                //若此时匹配到的商品实际库中不存在则删除此匹配记录,下次重新匹配
                if (ptmProduct == null) {
                    logger.info("product id" + sio.getHsProId() + " is not exist ");
                    productService.deleteProduct(sio.getHsProId());
                    //未匹配,结束操作
                } else {
                    cr = getCmpProducts(sio);
                    cr.setProductId(sio.getHsProId());
                    cr.setCopywriting(ptmProduct != null && ptmProduct.isStd() ? "Searched across Flipkart,Snapdeal,Paytm & 6 other apps to get the best deals for you." : "Looked around Myntre,Jabong & 5 other apps,thought you might like these items as well..");
                    cr.setDisplayMode(ptmProduct != null && ptmProduct.isStd() ? AppDisplayMode.NONE : AppDisplayMode.WATERFALL);
                    cr.setStd(ptmProduct.isStd());
                }
            } else {
                //小于等于0,直接返回
                logger.info("productid is " + sio.getHsProId() + " ls than zero");
                jsonObject.put("data", JSONObject.toJSON(cr));
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
                return null;
            }
        } catch (Exception e) {
            logger.error(ExceptionHelper.getExceptionMessage(e));
            logger.error(String.format("sdk_cmp_  [NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", q, site, price, page, pageSize));
            jsonObject.put("data", JSONObject.toJSON(cr));
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
            return null;
        }
        if (cr != null) {
            jsonObject.put("data", JSONObject.toJSON(cr));
        } else {
            jsonObject.put("data", "{\n" +
                    "        \"copywriting\": \"\",\n" +
                    "        \"show\": \"WATERFALL\",\n" +
                    "        \"skus\": [\n" +
                    "            {\n" +
                    "                \"status\": \"onsale\",\n" +
                    "                \"title\": \"小王子（法国“圣埃克苏佩里基金会”官方认可简体中文译本）\",\n" +
                    "                \"imageUrl\": \"http://img13.360buyimg.com/n1/jfs/t2200/173/590579185/269686/4c299e77/56174e3eN362982a4.jpg\",\n" +
                    "                \"cashBack\": \"10\",\n" +
                    "                \"deepLink\": \"http://item.jd.com/11143993.html\",\n" +
                    "                \"saved\": 100,\n" +
                    "                \"id\": \"11143993\",\n" +
                    "                \"skuPrice\": \"1,000\",\n" +
                    "                \"website\": \"FLIPKART\"\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"status\": \"sold out\",\n" +
                    "                \"title\": \"摩斯维 手机套/金属边框/防摔保护壳外壳 适用于华为荣耀畅玩4X/全网通/电信/移动版 拉丝尊享款-香槟金-送钢化膜\",\n" +
                    "                \"imageUrl\": \"http://img11.360buyimg.com/n1/jfs/t2698/221/1187894551/168647/33c6c8e1/5736a5f7Nfa29f761.jpg\",\n" +
                    "                \"cashBack\": \"20\",\n" +
                    "                \"deepLink\": \"http://item.jd.com/1381873091.html\",\n" +
                    "                \"saved\": -100,\n" +
                    "                \"id\": \"1381873091\",\n" +
                    "                \"skuPrice\": \"1,000\",\n" +
                    "                \"website\": \"FLIPKART\"\n" +
                    "            }\n" +
                    "        ]\n" +
                    "    }");
        }
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
        return null;
    }

    @RequestMapping(value = "/cmpsku", method = RequestMethod.GET)
    public ModelAndView cmpsku(@RequestParam(defaultValue = "0") final String id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int size,
                               HttpServletResponse response
    ) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"imageUrl", "skuPrice", "deepLink", "title", "saved", "id", "status", "priceOff", "productVo", "pagedComparedSkuVos", "copywriting", "displayMode", "std", "cashBack"});
        CmpResult cr = null;
        PtmProduct product = productService.getProduct(Long.valueOf(id));
        if (product != null) {
            String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
            DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
            SearchIO sio = new SearchIO(product.getSourceId(), product.getTitle(), "", product.getSourceSite(), product.getPrice() + "", deviceInfo.getMarketChannel(), deviceId, page, size);
            try {
                cr = getCmpProducts(sio, product);
            } catch (Exception e) {
                logger.error(String.format("[NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", product.getTitle(), product.getSourceSite(), product.getPrice(), page, size));
                //if exception occured ,get default cmpResult
                jsonObject.put("data", JSONObject.toJSON(cr));
                Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
                return null;
            }
            // 速度优化
            SearchHelper.addToLog(sio);
            logger.debug(sio.toString());
            jsonObject.put("data", JSONObject.toJSON(cr));
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
            return null;
        }
        jsonObject.put("data", JSONObject.toJSON(cr));
        Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
        return null;
    }

    private CmpResult getDefaultCmpResult(SearchIO sio, PtmCmpSkuIndex2 cmpSkuIndex) {
        String currentDeeplink = "";
        if (cmpSkuIndex != null && cmpSkuIndex.getId() != null && cmpSkuIndex.getId() > 0) {
            PtmCmpSku cmpSku = cmpSkuCacheManager.getCmpSkuById(cmpSkuIndex.getId());

            if (cmpSku != null && cmpSku.getWebsite().equals(sio.getCliSite())) {
                currentDeeplink = WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
            }
        }

        List<ComparedSkuVo> comparedSkuVos = new ArrayList<ComparedSkuVo>();
        comparedSkuVos.add(new ComparedSkuVo(sio.getCliSite(), sio.getCliQ(), sio.getCliPrice()));

        return new CmpResult(0,
                new ProductVo(0L, sio.getCliQ(), "", sio.getCliPrice(), currentDeeplink),
                new PageableResult<ComparedSkuVo>(comparedSkuVos, 0, 1, 10)
        );
    }

    /**
     * 从solr中搜索
     *
     * @param sio
     */
    private void searchForResult_old(SearchIO sio) {
        String _q = StringUtils.getSearchKey(sio.getCliQ());

        long cateId = 0L;
        int level = 0, index_for;

        // 搜索商品
        PageableResult<Long> pagedProIds = productIndexService.searchPro(cateId, level, StringUtils.toLowerCase(_q), 1, 5);

        List<Long> proIds = pagedProIds.getData();

        if (ArrayUtils.isNullOrEmpty(proIds)) {
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, "", 0);
        }

        long proId = proIds.get(0);
        PtmProduct product = productCacheManager.getProduct(proId); //productService.getProduct(proId);

        float mc = StringUtils.wordMatchD(StringUtils.toLowerCase(product.getTitle()), _q);
        if (!StringUtils.isEmpty(product.getTag())) {
            mc = (mc + StringUtils.wordMatchD(StringUtils.toLowerCase(product.getTag()), _q) * 2) / 2;
        }

        // 匹配度如果小于40%, 则认为不匹配
        if (mc <= 0.4) {
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, product.getTitle(), mc);
        }

        sio.set(cateId, proId, 0L);
    }

    /**
     * 从solr中搜索
     *
     * @param sio
     */
    private void searchForResult(SearchIO sio) throws NonMatchedProductException {
        String _q = StringUtils.getSearchKey(sio.getCliQ());

        // 搜索SKU
        PageableResult<CmpSkuModel> pagedCmpskuModels = cmpskuIndexService.searchSku(_q, 1, 5);

        List<CmpSkuModel> skuModels = pagedCmpskuModels.getData();

        if (ArrayUtils.isNullOrEmpty(skuModels)) {
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, "", 0);
        }

        CmpSkuModel skuModel = null;
        for (CmpSkuModel cmpSkuModel : skuModels) {
            if (cmpSkuModel.getProductId() == 0) {
                continue;
            }
            skuModel = cmpSkuModel;
            break;
        }

        if (skuModel == null) {
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, "", 0);
        }

        String title = skuModel.getTitle();

        float mc = StringUtils.wordMatchD(StringUtils.toLowerCase(title), _q);
        // 匹配度如果小于40%, 则认为不匹配
        if (mc <= 0.4) {
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, title, mc);
        }

        long cateId = 0L;
        sio.set(cateId, skuModel.getProductId(), skuModel.getId());
    }

    private void getSioBySearch(SearchIO sio) {

        String q = sio.getCliQ();

        String logId = HexDigestUtil.md5(q + "-" + sio.getCliSite().name()); // 这个值作为log表的id

        SrmSearchLog srmSearchLog = searchLogCacheManager.findSrmSearchLog(logId, true);

        if (srmSearchLog != null
                && (srmSearchLog.getPrecise() == SearchPrecise.TIMERSET2
                || srmSearchLog.getPrecise() == SearchPrecise.MANUALSET)) {

            if (srmSearchLog.getPtmProductId() <= 0) {
                logger.error("Found search log. but product id is 0 .");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), "", 0);
            }

            sio.set(srmSearchLog.getCategory(), srmSearchLog.getPtmProductId(), srmSearchLog.getPtmCmpSkuId());
        } else {
            if (srmSearchLog == null) {
                sio.setFirstSearch(true);
            }

            try {
                searchForResult(sio);
            } catch (NonMatchedProductException e) {
                searchForResult_old(sio);
            }
        }

        /*if (srmSearchLog == null) {
            sio.setFirstSearch(true);

            searchForResult(sio);
        } else {
            if (srmSearchLog.getPrecise() == SearchPrecise.TIMERSET2
                    || srmSearchLog.getPrecise() == SearchPrecise.MANUALSET) {

                if (srmSearchLog.getPtmProductId() <= 0) {
                    logger.debug("Found search log. but product id is 0 .");
                    throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), "", 0);
                }

                sio.set(srmSearchLog.getCategory(), srmSearchLog.getPtmProductId(), srmSearchLog.getPtmCmpSkuId());
            } else {
                searchForResult(sio);
            }
        }*/
    }

    private CmpResult getCmpResult(SearchIO sio, PtmCmpSkuIndex2 cmpSkuIndex) {

        List<ComparedSkuVo> comparedSkuVos = new ArrayList<ComparedSkuVo>();

        /**
         * 如果匹配到商品，proId是匹配到的商品ID，cmpSkuId是比价列表中对应该网站的skuId
         * 如果没有匹配到，或比价列表不含该网站，则相应变量值为0
         */
        long cmpSkuId = 0L;
        float minPrice = sio.getCliPrice(), maxPrice = sio.getCliPrice();

        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listPagedCmpSkus(sio.getHsProId(), sio.getPage(), sio.getSize());
        List<PtmCmpSku> cmpSkus = pagedCmpskus.getData();
        PtmCmpSku clientCmpSku = null;

        float cliPrice = sio.getCliPrice(), priceOff = 0.0f;
        if (ArrayUtils.hasObjs(cmpSkus)) {

            for (PtmCmpSku cmpSku : cmpSkus) {
                if (sio.getCliSite().equals(cmpSku.getWebsite())) {
                    clientCmpSku = cmpSku;
                    break;
                }
            }

            if (clientCmpSku != null) {
                cmpSkuId = clientCmpSku.getId();
                if (cliPrice <= 0) {
                    cliPrice = clientCmpSku.getPrice();
                    minPrice = cliPrice;
                    maxPrice = cliPrice;
                } else {
                    clientCmpSku.setPrice(cliPrice);
                }
            } else {
                // 如果比价列表中没有找到该网站的 sku， 则把客户端传上来的商品返回
                addVo(comparedSkuVos, new ComparedSkuVo(sio.getCliSite(), sio.getCliQ(), sio.getCliPrice()));
            }

            // 获取vo list
            for (PtmCmpSku cmpSku : cmpSkus) {

                if (cmpSku.getWebsite() == null
                        || cmpSku.getPrice() <= 0
                        || cmpSku.getStatus() != SkuStatus.ONSALE) { // 临时过滤掉不能更新价格的商品
                    logger.error(cmpSku.getId() + ", price=" + cmpSku.getPrice() + ", status=" + cmpSku.getStatus());
                    continue;
                }
                if (minPrice <= 0 || minPrice > cmpSku.getPrice()) {
                    minPrice = cmpSku.getPrice();
                }
                if (maxPrice <= 0 || maxPrice < cmpSku.getPrice()) {
                    maxPrice = cmpSku.getPrice();
                }

                // 忽略前台返回的价格
                List<String> affs = getAffs(sio);
                ComparedSkuVo csv = new ComparedSkuVo(cmpSku, affs.toArray(new String[0]));
                csv.setPriceOff(cliPrice - cmpSku.getPrice());

                addVo(comparedSkuVos, csv);
            }

            if (ArrayUtils.isNullOrEmpty(comparedSkuVos)) {
                logger.error("Compared SKU VO IS EMPTY");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, "", sio.getCliQ(), sio.getCliPrice());
            }

            float standPrice = maxPrice;
            if (cliPrice <= 0) {
                // 取一个标准价格，如果client sku 为null，则取maxPrice
                if (clientCmpSku != null) {
                    standPrice = clientCmpSku.getPrice();
                }
                for (ComparedSkuVo skuVo : comparedSkuVos) {
                    skuVo.setPriceOff(standPrice - skuVo.getPrice());
                }
            } else {
                standPrice = cliPrice;
            }
            // you can save ...
            priceOff = standPrice - minPrice;

            //根据价格排序
            Collections.sort(comparedSkuVos, new Comparator<ComparedSkuVo>() {
                @Override
                public int compare(ComparedSkuVo o1, ComparedSkuVo o2) {
                    if (o1.getPrice() > o2.getPrice()) {
                        return 1;
                    } else if (o1.getPrice() < o2.getPrice()) {
                        return -1;
                    }
                    return 0;
                }
            });

        } else {
            // logger.error("Found skus size is 0 .");
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), sio.getKeyword(), 0.0f);
        }

        sio.setHsSkuId(cmpSkuId);
        String currentDeeplink = "";
        try {
            if (cmpSkuIndex != null && cmpSkuIndex.getId() > 0) {
                if (cmpSkuIndex.getWebsite().equals(sio.getCliSite())) {
                    currentDeeplink = WebsiteHelper.getDeeplinkWithAff(cmpSkuIndex.getWebsite(), cmpSkuIndex.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                }
                /*PtmCmpSku cmpSku = cmpSkuCacheManager.getCmpSkuById(cmpSkuIndex.getId());
                if (cmpSku.getWebsite().equals(sio.getCliSite())) {
                    currentDeeplink = WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                }*/
            } else if (clientCmpSku != null) {
                if (!cmpSkuCacheManager.isFlowControlled(sio.getDeviceId(), sio.getCliSite())) {
                    if (StringUtils.isEqual(clientCmpSku.getSkuTitle(), sio.getCliQ()) && clientCmpSku.getPrice() == cliPrice) {
                        currentDeeplink = WebsiteHelper.getDeeplinkWithAff(clientCmpSku.getWebsite(), clientCmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                    }
                }
            }
        } catch (Exception e) {
            // logger.error(e.getMessage());
        }

        String imageUrl = productCacheManager.getProductMasterImageUrl(sio.getHsProId());//productService.getProductMasterImageUrl(sio.getHsProId());
        ProductVo productVo = new ProductVo(sio.getHsProId(), sio.getCliQ(), imageUrl, minPrice, currentDeeplink);

        return new CmpResult(priceOff, productVo, new PageableResult<ComparedSkuVo>(comparedSkuVos, pagedCmpskus.getNumFund(), pagedCmpskus.getCurrentPage(), pagedCmpskus.getPageSize()));
    }

    private List<String> getAffs(SearchIO sio) {
        List<String> affs = new ArrayList<String>();
        affs.add(sio.getMarketChannel().name());
        affs.add(sio.getDeviceId());
        UrmUser urmUser = appService.getUserByUserToken((String) Context.currentContext().get(StaticContext.USER_TOKEN));
        if (urmUser != null) {
            affs.add(urmUser.getId().toString());
        }
        return affs;
    }

    /**
     * get cmp product results
     *
     * @param sio
     * @return
     */
    private CmpResult getCmpProducts(SearchIO sio, PtmProduct product) {
        //初始化一个空的用于存放比价商品列表的List
        List<CmpProductListVo> comparedSkuVos = new ArrayList<CmpProductListVo>();
        CmpResult cmpResult = new CmpResult();
        //从ptmCmpSku表获取 productId为指定值、且状态为ONSALE 按照价格升序排列
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listPagedCmpSkus(product.getId(), sio.getPage(), sio.getSize());
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            List<PtmCmpSku> cmpSkus = pagedCmpskus.getData();
            //评论数按照加权平均值展示
            Long tempTotalComments = Long.valueOf(0);
            //评论星级按照平均值展示
            int tempRatins = 0;
            int tempCount = 0;
            //统计site
            Set<Website> websiteSet = new HashSet<Website>();
            //初始化price为客户端传输的price
            if (ArrayUtils.hasObjs(cmpSkus)) {
                // 获取vo list
                for (PtmCmpSku cmpSku : cmpSkus) {
                    if (cmpSku.getWebsite() == null
                            || cmpSku.getPrice() <= 0
                            || cmpSku.getStatus() != SkuStatus.ONSALE) { // 临时过滤掉不能更新价格的商品
                        continue;
                    }
                    if (cmpSku.getWebsite() != null) {
                        websiteSet.add(cmpSku.getWebsite());
                    }
                    // 忽略前台返回的价格
                    CmpProductListVo cplv = new CmpProductListVo(cmpSku, WebsiteHelper.getLogoUrl(cmpSku.getWebsite()));
                    cplv.setDeepLinkUrl(WebsiteHelper.getDealUrlWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name()}));
                    cplv.setDeepLink(WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name()}));
                    comparedSkuVos.add(cplv);
                }
                if (ArrayUtils.isNullOrEmpty(comparedSkuVos)) {
                    throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, "", product.getTitle(), product.getPrice());
                }
                //根据价格排序
                Collections.sort(comparedSkuVos, new Comparator<CmpProductListVo>() {
                    @Override
                    public int compare(CmpProductListVo o1, CmpProductListVo o2) {
                        if (o1.getPrice() > o2.getPrice()) {
                            return 1;
                        } else if (o1.getPrice() < o2.getPrice()) {
                            return -1;
                        }
                        return 0;
                    }
                });

            } else {
                logger.debug("Found skus size is 0 .");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), sio.getKeyword(), 0.0f);
            }
            List<CmpProductListVo> tempCmpProductListVos = new ArrayList<CmpProductListVo>();
            Map<Long, Integer> tempComment = new HashMap<Long, Integer>();
            //每个site只保留一个且为最低价
            for (CmpProductListVo cmpProductListVo : comparedSkuVos) {
                if (websiteSet.size() <= 0) {
                    break;
                }
                if (websiteSet.contains(cmpProductListVo.getWebsite())) {
                    websiteSet.remove(cmpProductListVo.getWebsite());
                    //去除列表中除此之外的其他此site的数据
                    if (tempComment.containsKey(cmpProductListVo.getTotalRatingsNum())) {
                        tempComment.put(cmpProductListVo.getTotalRatingsNum(), tempComment.get(cmpProductListVo.getTotalRatingsNum()) + 1);
                    } else {
                        tempComment.put(cmpProductListVo.getTotalRatingsNum(), 1);
                    }
                    tempRatins += cmpProductListVo.getRatingNum();
                    tempCmpProductListVos.add(cmpProductListVo);
                }
            }
            //移除之前加进列表的所有的sku列表
            comparedSkuVos = null;
            comparedSkuVos = new ArrayList<>();
            //将新的加入的放入到列表中
            comparedSkuVos.addAll(tempCmpProductListVos);
            String imageUrl = productCacheManager.getProductMasterImageUrl(product.getId());
            cmpResult.setImage(imageUrl);
            cmpResult.setName(product.getTitle());
            PageableResult<CmpProductListVo> priceList = new PageableResult<CmpProductListVo>(comparedSkuVos, pagedCmpskus.getNumFund(), pagedCmpskus.getCurrentPage(), pagedCmpskus.getPageSize());
            cmpResult.setBestPrice(priceList.getData().get(0).getPrice());
            cmpResult.setPriceList(priceList.getData());
            //评论星级为加权平均值
            Set<Map.Entry<Long, Integer>> entries = tempComment.entrySet();
            //算得每一个的权值
            int totalWeigth = 0;
            for (Map.Entry<Long, Integer> map : entries) {
                //算总值
                totalWeigth += map.getValue();
            }
            BigDecimal WeightedAverage = BigDecimal.ZERO;
            for (Map.Entry<Long, Integer> map : entries) {
                //算得加权平均值
                Long key = map.getKey();
                Integer value = map.getValue();

                BigDecimal Weight = BigDecimal.valueOf(value).divide(BigDecimal.valueOf(totalWeigth), 1, BigDecimal.ROUND_HALF_UP);
                WeightedAverage = WeightedAverage.add(Weight.multiply(BigDecimal.valueOf(key)));
            }
            cmpResult.setRatingNum(tempRatins / tempCmpProductListVos.size());
            PtmProductDescription ptmProductDescription = mongoDbManager.queryOne(PtmProductDescription.class, product.getId());
            String specs = "";
            if (ptmProductDescription != null) {
                specs = ptmProductDescription.getJsonDescription();
            }
            cmpResult.setSpecs(specs);
            cmpResult.setTotalRatingsNum(WeightedAverage.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP).longValue());
            return cmpResult;
        }
        return cmpResult;
    }

    private CmpResult getCmpProducts(SearchIO sio) {
        //初始化一个空的用于存放比价商品列表的List
        List<CmpProductListVo> comparedSkuVos = new ArrayList<CmpProductListVo>();
        CmpResult cmpResult = new CmpResult();
        // 1. 查询此商品对应的sku列表 状态为ONSALE/OUTSTOCK
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listCmpSkus(sio.getHsProId(), sio.getPage(), sio.getSize());
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            List<PtmCmpSku> cmpSkus = pagedCmpskus.getData();
            //统计site
            Set<Website> websiteSet = new HashSet<Website>();
            if (ArrayUtils.hasObjs(cmpSkus)) {
                // 获取vo list
                for (PtmCmpSku cmpSku : cmpSkus) {
                    if (cmpSku.getWebsite() == null
                            || cmpSku.getPrice() <= 0) { // 临时过滤掉不能更新价格的商品
                        continue;
                    }
                    if (cmpSku.getWebsite() != null) {
                        websiteSet.add(cmpSku.getWebsite());
                    }
                    if (cmpSku.getWebsite().equals(sio.getCliSite())) {
                        //取与客户端所传商品同一个site的sku作为sku匹配sku
                        cmpResult.setProductVo(new ProductVo(sio.getHsProId(), sio.getCliQ(), cmpSku.getSmallImagePath() == null ? "" : ImageUtil.getImageUrl(cmpSku.getSmallImagePath()), 0.0f, WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()})));
                    }
                    CmpProductListVo cplv = new CmpProductListVo(cmpSku, sio.getCliPrice());
                    comparedSkuVos.add(cplv);
                }
                if (ArrayUtils.isNullOrEmpty(comparedSkuVos)) {
                    throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), "productid_" + sio.getHsProId(), sio.getCliPrice());
                }
                //根据价格排序
                Collections.sort(comparedSkuVos, new Comparator<CmpProductListVo>() {
                    @Override
                    public int compare(CmpProductListVo o1, CmpProductListVo o2) {
                        if (o1.getPrice() > o2.getPrice()) {
                            return 1;
                        } else if (o1.getPrice() < o2.getPrice()) {
                            return -1;
                        }
                        return 0;
                    }
                });

            } else {
                logger.debug("Found skus size is 0 .");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), sio.getKeyword(), 0.0f);
            }
            List<CmpProductListVo> tempCmpProductListVos = new ArrayList<CmpProductListVo>();
            //每个site只保留一个且为最低价
            long startTime = System.nanoTime();   //获取开始时间
            for (CmpProductListVo cmpProductListVo : comparedSkuVos) {
                if (websiteSet.size() <= 0) {
                    break;
                }
                if (websiteSet.contains(cmpProductListVo.getWebsite())) {
                    websiteSet.remove(cmpProductListVo.getWebsite());
                    //去除列表中除此之外的其他此site的数据
                    tempCmpProductListVos.add(cmpProductListVo);
                }
            }
            //移除之前加进列表的所有的sku列表
            comparedSkuVos = null;
            comparedSkuVos = new ArrayList<>();
            //将新的加入的放入到列表中
            comparedSkuVos.addAll(tempCmpProductListVos);
            long endTime = System.nanoTime(); //获取结束时间
            System.out.println("total time is " + (endTime - startTime) / 1000000 + "");
        }
        cmpResult.setPriceList(comparedSkuVos);
        cmpResult.setCopywriting("Searched across Flipkart,Snapdeal,Paytm & 6 other apps to get the best deals for you.");
        cmpResult.setDisplayMode(AppDisplayMode.WATERFALL);
        return cmpResult;
    }

    private void addVo(List<ComparedSkuVo> comparedSkuVos, ComparedSkuVo comparedSkuVo) {
        if (comparedSkuVo == null || comparedSkuVo.getPrice() <= 0) {
            return;
        }
        comparedSkuVos.add(comparedSkuVo);
    }
}