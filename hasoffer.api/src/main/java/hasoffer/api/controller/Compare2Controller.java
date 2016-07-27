package hasoffer.api.controller;

import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.SearchHelper;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.exception.ERROR_CODE;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
                    logger.info(ptmProduct.toString());
                }

            }
            logger.error(e.getMessage());
            logger.error(String.format("[NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", q, site, price, page, size));

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

    @RequestMapping(value = "/cmpsku", method = RequestMethod.GET)
    public ModelAndView cmpsku(@RequestParam(defaultValue = "0") final String id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size
    ) {

        ModelAndView mav = new ModelAndView();
        CmpResult cr = null;
        PtmProduct product = productService.getProduct(Long.valueOf(id));
        if (product != null) {
            String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
            DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
            SearchIO sio = new SearchIO(product.getSourceId(), product.getTitle(), "", product.getSourceSite(), product.getPrice() + "", deviceInfo.getMarketChannel(), deviceId, page, size);
            try {
//            getSioBySearch(sio);
                cr = getCmpProducts(sio, product);
            } catch (Exception e) {
                logger.error(String.format("[NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", product.getTitle(), product.getSourceSite(), product.getPrice(), page, size));
                //if exception occured ,get default cmpResult
                mav.addObject("data", cr);
                return mav;
            }
            // 速度优化
            SearchHelper.addToLog(sio);
            logger.debug(sio.toString());
            mav.addObject("data", cr);
            return mav;
        }
        mav.addObject("data", cr);
        return mav;
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
            logger.error("Found skus size is 0 .");
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), sio.getKeyword(), 0.0f);
        }

        sio.setHsSkuId(cmpSkuId);
        String currentDeeplink = "";
        try {
            if (cmpSkuIndex != null && cmpSkuIndex.getId() > 0) {
                PtmCmpSku cmpSku = cmpSkuCacheManager.getCmpSkuById(cmpSkuIndex.getId());
                if (cmpSku.getWebsite().equals(sio.getCliSite())) {
                    currentDeeplink = WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                }
            } else if (clientCmpSku != null) {
                if (!cmpSkuCacheManager.isFlowControlled(sio.getDeviceId(), sio.getCliSite())) {
                    if (StringUtils.isEqual(clientCmpSku.getSkuTitle(), sio.getCliQ()) && clientCmpSku.getPrice() == cliPrice) {
                        currentDeeplink = WebsiteHelper.getDeeplinkWithAff(clientCmpSku.getWebsite(), clientCmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
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
            Long tempTotalComments = Long.valueOf(0);
            int tempRatins = 0;
            int tempCount = 0;
            //初始化price为客户端传输的price
            if (ArrayUtils.hasObjs(cmpSkus)) {
                // 获取vo list
                for (PtmCmpSku cmpSku : cmpSkus) {
                    if (cmpSku.getWebsite() == null
                            || cmpSku.getPrice() <= 0
                            || cmpSku.getStatus() != SkuStatus.ONSALE) { // 临时过滤掉不能更新价格的商品
                        continue;
                    }
                    tempCount += 1;
                    // 忽略前台返回的价格
                    tempTotalComments += cmpSku.getCommentsNumber();
                    tempRatins += cmpSku.getRatings();
                    CmpProductListVo cplv = new CmpProductListVo(cmpSku, WebsiteHelper.getLogoUrl(cmpSku.getWebsite()));
                    cplv.setDeepLinkUrl(WebsiteHelper.getUrlWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name()}));
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
            String imageUrl = productCacheManager.getProductMasterImageUrl(product.getId());
            cmpResult.setImage(imageUrl);
            cmpResult.setName(product.getTitle());
            PageableResult<CmpProductListVo> priceList = new PageableResult<CmpProductListVo>(comparedSkuVos, pagedCmpskus.getNumFund(), pagedCmpskus.getCurrentPage(), pagedCmpskus.getPageSize());
            cmpResult.setBestPrice(priceList.getData().get(0).getPrice());
            cmpResult.setPriceList(priceList.getData());
            cmpResult.setRatingNum(tempRatins / (tempCount == 0 ? 1 : tempCount));
            PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, product.getId());
            String specs = "";
            if (ptmCmpSkuDescription != null) {
                specs = ptmCmpSkuDescription.getJsonDescription();
            }
            cmpResult.setSpecs(specs);
            cmpResult.setTotalRatingsNum(tempTotalComments / Long.valueOf(tempCount == 0 ? 1 : tempCount));
            return cmpResult;
        }
        return cmpResult;
    }

    private void addVo(List<ComparedSkuVo> comparedSkuVos, ComparedSkuVo comparedSkuVo) {
        if (comparedSkuVo == null || comparedSkuVo.getPrice() <= 0) {
            return;
        }
        comparedSkuVos.add(comparedSkuVo);
    }
}