package hasoffer.api.controller;

import hasoffer.api.controller.vo.*;
import hasoffer.api.helper.SearchHelper;
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
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmCmpSkuIndex2;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.solr.CategoryIndexServiceImpl;
import hasoffer.core.product.solr.CategoryModel;
import hasoffer.core.product.solr.ProductIndexServiceImpl;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.exception.NonMatchedProductException;
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
@RequestMapping(value = "/cmp3")
public class CompareController {
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

    private Logger logger = LoggerFactory.getLogger(CompareController.class);

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
            if (Website.FLIPKART.equals(sio.getCliSite())
                    || Website.SNAPDEAL.equals(sio.getCliSite())
                    || Website.SHOPCLUES.equals(sio.getCliSite())) {
                // 先去匹配sku
                cmpSkuIndex = cmpSkuCacheManager.getCmpSkuIndex2(deviceId, sio.getCliSite(), sio.getCliSourceId(), sio.getCliQ());
            }
            getSioBySearch(sio);
            cr = getCmpResult(sio, cmpSkuIndex);
        } catch (Exception e) {
            logger.debug(String.format("[NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", q, site, price, page, size));

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

        logger.debug(sio.toString());

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
    private void searchForResult(SearchIO sio) {
        String _q = StringUtils.getSearchKey(sio.getCliQ());

        long cateId = 0L;
        int level = 0, index_for;

        String _q_cate = _q;
        if ((index_for = _q.indexOf(" for ")) > 0) {
            _q_cate = _q.substring(0, index_for);
        }
        // 搜索类目
        List<CategoryModel> cates = categoryIndexService.simpleSearch(_q_cate);
        if (ArrayUtils.hasObjs(cates)) {
            CategoryModel cate = cates.get(0);
            cateId = cate.getId();
            level = cate.getLevel();
        }

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

    private void getSioBySearch(SearchIO sio) {

        String q = sio.getCliQ();
        //todo 临时操作, q 是处理过的
        if (sio.getCliSite().equals(Website.FLIPKART)) {
            int index = q.indexOf("(");
            if (index > 0) {
                q = q.substring(0, q.indexOf("(")).trim();
            }
        }

        String logId = HexDigestUtil.md5(q + "-" + sio.getCliSite().name()); // 这个值作为log表的id
        SrmSearchLog srmSearchLog = searchLogCacheManager.findSrmSearchLog(logId, true);

        if (srmSearchLog == null) {
            sio.setFirstSearch(true);
            searchForResult(sio);
        } else {
            if (srmSearchLog.getPtmProductId() <= 0) {
                logger.debug("Found search log. but product id is 0 .");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), "", 0);
            }
            sio.set(srmSearchLog.getCategory(), srmSearchLog.getPtmProductId(), srmSearchLog.getPtmCmpSkuId());
        }
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
                    continue;
                }

                if (minPrice <= 0 || minPrice > cmpSku.getPrice()) {
                    minPrice = cmpSku.getPrice();
                }
                if (maxPrice <= 0 || maxPrice < cmpSku.getPrice()) {
                    maxPrice = cmpSku.getPrice();
                }

                // 忽略前台返回的价格
                ComparedSkuVo csv = new ComparedSkuVo(cmpSku, new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                csv.setPriceOff(cliPrice - cmpSku.getPrice());

                addVo(comparedSkuVos, csv);
            }

            if (ArrayUtils.isNullOrEmpty(comparedSkuVos)) {
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
            logger.debug("Found skus size is 0 .");
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), sio.getKeyword(), 0.0f);
        }

        sio.setHsSkuId(cmpSkuId);

        String currentDeeplink = "";
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

        String imageUrl = productCacheManager.getProductMasterImageUrl(sio.getHsProId());//productService.getProductMasterImageUrl(sio.getHsProId());

        // todo 临时处理，snapdeal都不返回当前deeplink
//        if (sio.getCliSite() == Website.SNAPDEAL) {
//            currentDeeplink = "";
//        }s

        ProductVo productVo = new ProductVo(sio.getHsProId(), sio.getCliQ(), imageUrl, minPrice, currentDeeplink);

        return new CmpResult(priceOff, productVo, new PageableResult<ComparedSkuVo>(comparedSkuVos, pagedCmpskus.getNumFund(), pagedCmpskus.getCurrentPage(), pagedCmpskus.getPageSize()));
    }

    private void addVo(List<ComparedSkuVo> comparedSkuVos, ComparedSkuVo comparedSkuVo) {
        if (comparedSkuVo == null || comparedSkuVo.getPrice() <= 0) {
            return;
        }
        comparedSkuVos.add(comparedSkuVo);
    }
}