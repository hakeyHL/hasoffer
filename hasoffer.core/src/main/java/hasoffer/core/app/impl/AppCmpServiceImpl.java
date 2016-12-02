package hasoffer.core.app.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.base.model.AppDisplayMode;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.app.AppCmpService;
import hasoffer.core.app.vo.CmpProductListVo;
import hasoffer.core.app.vo.CmpResult;
import hasoffer.core.app.vo.ProductVo;
import hasoffer.core.app.vo.SearchIO;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.exception.ERROR_CODE;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.IPtmStdSkuService;
import hasoffer.core.product.impl.ProductServiceImpl;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.exception.NonMatchedProductException;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.core.utils.JsonHelper;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.fetch.helper.WebsiteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by hs on 2016年10月19日.
 * Time 15:11
 */
@Service
public class AppCmpServiceImpl implements AppCmpService {
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;
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
    @Resource
    IPriceOffNoticeService iPriceOffNoticeService;
    @Resource
    PtmStdSkuIndexServiceImpl ptmStdSkuIndexService;
    @Resource
    IPtmStdSkuService skuService;
    @Resource
    IPtmStdPriceService stdPriceService;
    @Resource
    ProductCacheManager productCacheManager;
    private Logger logger = LoggerFactory.getLogger(AppCmpServiceImpl.class);

    @Override
    public String sdkCmpSku(SearchIO sio) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"ratingNum", "bestPrice", "backRate", "support", "price", "returnGuarantee", "freight"});
        CmpResult cr = null;
        String currentDeeplink = "";
        //优先从91mobile solr中搜索匹配
        //deal还要走原来的推荐
        if (sio.getStdSkuId() > 0) {
            //如果有,则返回price列表
            PtmStdSku stdSku = skuService.getStdSkuById(sio.getStdSkuId());
            if (stdSku != null) {
                cr = new CmpResult();
                currentDeeplink = WebsiteHelper.getDeeplinkWithAff(Website.valueOf(sio.getStdPriceWebSite()), sio.getStdPriceUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                cr.setProductVo(new ProductVo(sio.getHsProId(), sio.getCliQ(), productCacheManager.getPtmStdSkuImageUrl(sio.getStdSkuId()), 0.0f, currentDeeplink));
                cr.setDisplayMode(AppDisplayMode.NONE);
                cr.setStd(true);
                cr.setProductId(ApiUtils.addBillion(stdSku.getId()));
                cr.setCopywriting("Searched across Flipkart,Snapdeal,Paytm & 6 other apps to get the best deals for you.");
                PageableResult<PtmStdPrice> pagedPtmStdPriceList = stdPriceService.getPagedPtmStdPriceList(stdSku.getId(), SkuStatus.ONSALE, sio.getPage(), sio.getSize());
                if (pagedPtmStdPriceList != null && pagedPtmStdPriceList.getData() != null && pagedPtmStdPriceList.getData().size() > 0) {
                    List<CmpProductListVo> cmpProductListVoList = new ArrayList<>();
                    if (sio.getCliPrice() <= 0) {
                        List<PtmStdPrice> data = pagedPtmStdPriceList.getData();
                        sortByPriceAsc(data);
                        cr.setBestPrice(data.get(0).getPrice());
                        cr.getProductVo().setCurrentLowestPrice(cr.getBestPrice());
                        sio.setCliPrice(data.get(data.size() - 1).getPrice());
                    }
                    //计算列表中最大价格
                    for (PtmStdPrice ptmStdPrice : pagedPtmStdPriceList.getData()) {
                        CmpProductListVo productListVo = new CmpProductListVo(WebsiteHelper.getLogoUrl(Website.valueOf(sio.getStdPriceWebSite())),
                                productCacheManager.getPtmStdSkuImageUrl(ptmStdPrice.getStdSkuId()),
                                ptmStdPrice, sio.getCliPrice());
                        productListVo.setDeepLink(WebsiteHelper.getDeeplinkWithAff(ptmStdPrice.getWebsite(), ptmStdPrice.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()}));
                        cmpProductListVoList.add(productListVo);
                    }
                    cr.setPriceList(cmpProductListVoList);
                } else {
                    cr.setPriceList(new ArrayList<CmpProductListVo>());
                }
                jsonObject.put("data", JSONObject.toJSON(cr));
                return jsonObject.toJSONString();
            }
        } else {
            PtmCmpSkuIndex2 cmpSkuIndex = cmpSkuCacheManager.getCmpSkuIndex2(sio.getDeviceId(), sio.getCliSite(), sio.getCliSourceId(), sio.getCliQ());
            //根据title匹配到商品
            PtmProduct ptmProduct;
            try {
                if (sio.getHsProId() > 0) {
                    ptmProduct = productService.getProduct(sio.getHsProId());
                    //若此时匹配到的商品实际库中不存在则删除此匹配记录,下次重新匹配
                    if (ptmProduct == null) {
                        logger.info("product id" + sio.getHsProId() + " is not exist ");
                        productService.deleteProduct(sio.getHsProId());
                        if (cmpSkuIndex != null && cmpSkuIndex.getId() > 0) {
                            if (cmpSkuIndex.getWebsite().equals(sio.getCliSite())) {
                                currentDeeplink = WebsiteHelper.getDeeplinkWithAff(cmpSkuIndex.getWebsite(), cmpSkuIndex.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                            }
                            cr = new CmpResult();
                            cr.setProductVo(new ProductVo(sio.getHsProId(), sio.getCliQ(), productCacheManager.getProductMasterImageUrl(sio.getHsProId()), 0.0f, currentDeeplink));
                            cr.setDisplayMode(AppDisplayMode.NONE);
                            cr.setStd(true);
                            cr.setPriceList(new ArrayList<CmpProductListVo>());
                            jsonObject.put("data", JSONObject.toJSON(cr));
                        }
                        cr = fillCmpResult(cr);
                        jsonObject.put("data", JSONObject.toJSON(cr));
                        return jsonObject.toJSONString();
                    } else {
                        cr = getCmpProducts(cmpSkuIndex, sio);
                        if (cr != null && cr.getPriceList().size() > 0) {
                            cr.setPriceOff(cr.getPriceList().get(0).getSaved());
                        }
                        cr = fillCmpResult(cr);
                        cr.setProductId(sio.getHsProId());
                        //cr.setCopywriting(ptmProduct != null && ptmProduct.isStd() ? "Searched across Flipkart,Snapdeal,Paytm & 6 other apps to get the best deals for you." : "Looked around Myntre,Jabong & 5 other apps,thought you might like these items as well..");
                        cr.setCopywriting("Searched across Flipkart,Snapdeal,Paytm & 6 other apps to get the best deals for you.");
                        //暂时屏蔽标品非标品
                        // cr.setDisplayMode(ptmProduct != null && ptmProduct.isStd() ? AppDisplayMode.NONE : AppDisplayMode.WATERFALL);
                        // cr.setStd(ptmProduct.isStd());
                        cr.setDisplayMode(AppDisplayMode.NONE);
                        cr.setStd(true);
                        jsonObject.put("data", JSONObject.toJSON(cr));
                        return JSON.toJSONString(jsonObject, propertyFilter);
                    }
                } else {
                    //小于等于0,直接返回
                    logger.info("productid is " + sio.getHsProId() + " ls than zero");
                    cr = fillCmpResult(cr);
                    jsonObject.put("data", JSONObject.toJSON(cr));
                    return JSON.toJSONString(jsonObject, propertyFilter);
                }
            } catch (Exception e) {
                logger.error(String.format("sdk_cmp_  [NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", sio.getCliQ(), sio.getCliSite(), sio.getCliPrice(), sio.getPage(), sio.getSize()));
                cr = fillCmpResult(cr);
                jsonObject.put("data", JSONObject.toJSON(cr));
                return JSON.toJSONString(jsonObject, propertyFilter);
            }
        }
        jsonObject.put("data", JSONObject.toJSON(cr));
        return JSON.toJSONString(jsonObject, propertyFilter);
    }

    private void sortByPriceAsc(List<PtmStdPrice> data) {
        Collections.sort(data, new Comparator<PtmStdPrice>() {
            @Override
            public int compare(PtmStdPrice o1, PtmStdPrice o2) {
                if (o1.getPrice() < o2.getPrice()) {
                    return -1;
                }
                if (o1.getPrice() > o2.getPrice()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private CmpResult fillCmpResult(CmpResult cr) {
        if (cr == null) {
            cr = new CmpResult();
            cr.setPriceList(new ArrayList<CmpProductListVo>());
        } else {
            if (cr.getPriceList() == null) {
                cr.setPriceList(new ArrayList<CmpProductListVo>());
            }
        }
        return cr;
    }

    private CmpResult getCmpProducts(PtmCmpSkuIndex2 ptmCmpSkuIndex2, SearchIO sio) {
        long cmpSkuId = 0L;
        float cmpedPrice = 0f;
        cmpedPrice = sio.getCliPrice();
        //初始化一个空的用于存放比价商品列表的List
        List<CmpProductListVo> comparedSkuVos = new ArrayList<CmpProductListVo>();
        CmpResult cmpResult = new CmpResult();
        // 1. 查询此商品对应的sku列表 状态为ONSALE/OUTSTOCK
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listCmpSkus(sio.getHsProId(), sio.getPage(), sio.getSize());
        PtmCmpSku clientCmpSku = null;
        float cliPrice = sio.getCliPrice();
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            List<PtmCmpSku> cmpSkus = pagedCmpskus.getData();
            //统计site
            Set<Website> websiteSet = new HashSet<Website>();
            ApiUtils.filterProducts(cmpSkus, sio.getCliQ());
            if (ArrayUtils.hasObjs(cmpSkus)) {
                if (sio.getCliPrice() <= 0) {
                    //未获取到价格,用最高价格作为客户端传来的商品价格
                    cmpedPrice = Collections.max(cmpSkus, new Comparator<PtmCmpSku>() {
                        @Override
                        public int compare(PtmCmpSku o1, PtmCmpSku o2) {
                            if (o1.getPrice() < o2.getPrice()) {
                                return -1;
                            }
                            if (o1.getPrice() > o2.getPrice()) {
                                return 1;
                            }
                            return 0;
                        }
                    }).getPrice();
                }

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
                    } else {
                        clientCmpSku.setPrice(cliPrice);
                    }
                } else {
                    // 如果比价列表中没有找到该网站的 sku， 则把客户端传上来的商品返回
                    CmpProductListVo cplv = new CmpProductListVo();
                    cplv.setTitle(sio.getCliQ());
                    cplv.setPrice(Math.round(sio.getCliPrice()));
                    cplv.setWebsite(sio.getCliSite());
                    comparedSkuVos.add(cplv);
                }
                // 获取vo list
                for (PtmCmpSku cmpSku : cmpSkus) {
                    if (cmpSku.getWebsite() == null
                            || cmpSku.getPrice() <= 0) { // 临时过滤掉不能更新价格的商品
                        continue;
                    }
                    if (cmpSku.getWebsite() != null) {
                        websiteSet.add(cmpSku.getWebsite());
                    }
                    CmpProductListVo cplv = new CmpProductListVo(cmpSku, cmpedPrice);
                    cplv.setDeepLink(WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()}));
                    logger.info(" getCmpProducts(ptmCmpSkuIndex2, sio) record deepLink :" + cplv.getDeepLink());
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
                //如果客户端传价格无法解析则重新计算save
                if (sio.getCliPrice() <= 0) {
                    int maxPrice = comparedSkuVos.get(comparedSkuVos.size() - 1).getPrice();
                    System.out.println(" can not analysis client's price ,use maxPrice instead of it " + maxPrice);
                    Iterator<CmpProductListVo> iterator = comparedSkuVos.iterator();
                    while (iterator.hasNext()) {
                        CmpProductListVo next = iterator.next();
                        next.setSaved(Math.round(maxPrice - next.getPrice()));
                    }
                }
            } else {
                logger.debug("Found skus size is 0 .");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), sio.getKeyword(), 0.0f);
            }
            sio.setHsSkuId(cmpSkuId);
            List<CmpProductListVo> tempCmpProductListVos = new ArrayList<CmpProductListVo>();
            //每个site只保留一个且为最低价
//            System.out.println("websiteSet :" + websiteSet.size());
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
//            System.out.println("tempCmpProductListVos" + tempCmpProductListVos.size());
            comparedSkuVos.addAll(tempCmpProductListVos);
            long endTime = System.nanoTime(); //获取结束时间
            System.out.println("total time is " + (endTime - startTime) / 1000000 + "");
        }
        String currentDeeplink = "";
        try {
            if (ptmCmpSkuIndex2 != null && ptmCmpSkuIndex2.getId() > 0) {
                if (ptmCmpSkuIndex2.getWebsite().equals(sio.getCliSite())) {
//                    System.out.println(" enter ptmCmpSkuIndex2 get deepLink ");
                    currentDeeplink = WebsiteHelper.getDeeplinkWithAff(ptmCmpSkuIndex2.getWebsite(), ptmCmpSkuIndex2.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
//                    System.out.println("currentDeeplink1  " + currentDeeplink);
                }
            } else if (clientCmpSku != null) {
                if (!cmpSkuCacheManager.isFlowControlled(sio.getDeviceId(), sio.getCliSite())) {
                    if (clientCmpSku.getSkuTitle().equalsIgnoreCase(sio.getCliQ()) && clientCmpSku.getPrice() == cliPrice) {
                        currentDeeplink = WebsiteHelper.getDeeplinkWithAff(clientCmpSku.getWebsite(), clientCmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()});
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("get deepLink failed ");
        }
        //取与客户端所传商品同一个site的sku作为sku匹配sku
        cmpResult.setProductVo(new ProductVo(sio.getHsProId(), sio.getCliQ(), productCacheManager.getProductMasterImageUrl(sio.getHsProId()), 0.0f, currentDeeplink));
//        System.out.println("comparedSkuVos" + comparedSkuVos.size());
        cmpResult.setPriceList(comparedSkuVos);
        cmpResult.setCopywriting("Searched across Flipkart,Snapdeal,Paytm & 6 other apps to get the best deals for you.");
        return cmpResult;
    }

    PtmStdSku searchStdPriceFromSolr(SearchIO searchIO) {
        return null;
    }
}
