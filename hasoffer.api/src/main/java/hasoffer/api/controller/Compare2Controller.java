package hasoffer.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.api.helper.ClientHelper;
import hasoffer.api.helper.Httphelper;
import hasoffer.api.helper.SearchHelper;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.core.app.impl.AppCmpServiceImpl;
import hasoffer.core.app.vo.*;
import hasoffer.core.cache.CmpSkuCacheManager;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.exception.ERROR_CODE;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.mongo.PtmProductDescription;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.persistence.po.urm.UrmUser;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IPtmStdPriceService;
import hasoffer.core.product.impl.ProductServiceImpl;
import hasoffer.core.product.impl.PtmStdSKuServiceImpl;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdPriceIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdPriceModel;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.exception.NonMatchedProductException;
import hasoffer.core.system.impl.AppServiceImpl;
import hasoffer.core.user.IPriceOffNoticeService;
import hasoffer.core.utils.JsonHelper;
import hasoffer.core.utils.api.ApiUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
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
    @Resource
    IPriceOffNoticeService iPriceOffNoticeService;
    @Resource
    AppCmpServiceImpl appCmpService;
    @Resource
    ApiUtils apiUtils;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IPtmStdPriceService iPtmStdPriceService;
    @Resource
    PtmStdSKuServiceImpl ptmStdSKuService;
    @Resource
    IPtmStdPriceService ptmStdPriceService;
    @Resource
    PtmStdPriceIndexServiceImpl ptmStdPriceIndexService;
    private Logger logger = LoggerFactory.getLogger(Compare2Controller.class);

    public static void main(String[] args) throws Exception {
       /* for (int i = 0; i < 10; i++) {
            String dealUrlWithAff = WebsiteHelper.getDeeplinkWithAff(Website.SNAPDEAL, "https://www.snapdeal.com/product/jbl-sb350-soundbar-with-wirless/1602277955", new String[]{MarketChannel.SHANCHUAN.name(), "dfecc858243a616a"});
            System.out.println(dealUrlWithAff);
        }*/
        float v = StringUtils.wordMatchD(StringUtils.toLowerCase("Professional Kingston MicroSDHC 32GB (32 Gigabyte) Card for Samsung Galaxy S4 Smartphone with custom"), "SAMSUNG GALAXY On7 (Black, 8 GB)");
        System.out.println(v);
    }

    private static void addVo(List<ComparedSkuVo> comparedSkuVos, ComparedSkuVo comparedSkuVo) {
        if (comparedSkuVo == null || comparedSkuVo.getPrice() <= 0) {
            return;
        }
        comparedSkuVos.add(comparedSkuVo);
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
    @DataSource(value = DataSourceType.Slave)
    @RequestMapping("sdk/cmpskus")
    public String cmpSkus(@RequestParam(defaultValue = "") final String q,
                          @RequestParam(defaultValue = "") final String brand,
                          @RequestParam(defaultValue = "") final String sourceId,
                          @RequestParam(defaultValue = "") String site,
                          @RequestParam(defaultValue = "0") String price,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int pageSize,
                          HttpServletResponse response) {
         /*1. 判断和处理客户端传送的价格,使其合法化
        1.1 是否可以使用正则或者别的方法把除数字外的东西去掉.

        2. 初始化匹配条件到sio对象中

        3. 匹配sku
        3.1 查看是否存在以deviceId和cliSite联合组成的key,此数据是在dot和流量拦截时存入的.(AppController)PtmCmpSkuIndex2
        3.2 如果存在则返回null
        3.3 不存在则以cliSite sourceId keyword 为及其他组成的key查询缓存中是否存在
        3.4 存在则转为对象然后返回,流程结束
        3.5 不存在则通过cliSite sourceId keyword 从数据库中查询,然后放入缓存,返回,流程结束

        4. 匹配商品 getSioBySearch
        4.1 获取查询Q , 根据q,cliSite使用HexDigestUtil.md5生成log的key.
        4.2 从searchLog中获取此商品,如果有则set进sio中
        4.3 如果无sio.setFirstSearch(true),然后再使用searchForResult方法获取该商品
        4.4 具体为从solr中按照title搜索该商品,只有匹配度大于0.4的才会被认为是一个商品
        5. 比价列表
        5.1 库中是否有此商品
        5.2 无则直接结束流程
        5.3 有则查询和处理比价列表返回*/

        //初始化sio对象
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);

        if (!StringUtils.isEmpty(price)) {
            //如果price不为空
            price = apiUtils.getStringNum(price);
        }
        SearchIO sio = new SearchIO(sourceId, q, brand, site, price, deviceInfo.getMarketChannel(), deviceId, page, pageSize);
        getPtmStdPriceBySioFromSolr(sio);
        if (sio.getStdSkuId() <= 0) {
            getSioBySearch(sio);
        }
        String jsonResult = appCmpService.sdkCmpSku(sio);
        Httphelper.sendJsonMessage(jsonResult, response);
        return null;
    }

    @DataSource(value = DataSourceType.Slave)
    @RequestMapping(value = "/cmpsku", method = RequestMethod.GET)
    public ModelAndView cmpsku(@RequestParam(defaultValue = "0") String id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int pageSize,
                               HttpServletResponse response,
                               HttpServletRequest request
    ) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorCode", "00000");
        jsonObject.put("msg", "ok");
        if (org.apache.commons.lang3.StringUtils.isEmpty(id)) {
            jsonObject.put("msg", "id required .");
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject), response);
        }
        String userToken = Context.currentContext().getHeader("usertoken");
        PropertyFilter propertyFilter = JsonHelper.filterProperty(new String[]{"skuPrice", "deepLink", "saved", "priceOff", "productVo", "pagedComparedSkuVos", "copywriting", "displayMode", "std", "cashBack"});
        CmpResult cr = new CmpResult();
        SearchIO sio = null;
        PtmProduct product = null;
        String deviceId = (String) Context.currentContext().get(StaticContext.DEVICE_ID);
        DeviceInfoVo deviceInfo = (DeviceInfoVo) Context.currentContext().get(Context.DEVICE_INFO);
        PtmStdSku ptmStdSku = null;
        if ((id).length() >= 10) {
            id = ApiUtils.rmoveBillion(Long.valueOf(id)) + "";
            ptmStdSku = ptmStdSKuService.getStdSkuById(Long.valueOf(id));
        }
        if (ptmStdSku != null) {
            sio = new SearchIO(ptmStdSku.getSourceId(), ptmStdSku.getTitle(), "", "", ptmStdSku.getRefPrice() + "", deviceInfo.getMarketChannel(), deviceId, page, pageSize);
            cr = getCmpPrices(sio, ptmStdSku, userToken);
        } else {
            product = productService.getProduct(Long.valueOf(id));
            if (product != null) {
                sio = new SearchIO(product.getSourceId(), product.getTitle(), "", StringUtils.isEmpty(product.getSourceSite()) == true ? null : product.getSourceSite(), product.getPrice() + "", deviceInfo.getMarketChannel(), deviceId, page, pageSize);
                cr = getCmpProducts(sio, product, userToken);
            }
        }
        try {
            jsonObject.put("page", JSONObject.toJSON(PageHelper.getPageModel(request, cr.getPagedComparedSkuVos())));
        } catch (Exception e) {
            logger.error("exception occur while get cmpSkuList on interface cmpsku , id is : {} and ptmStdSku ,PtmProduct ", ptmStdSku == null ? product.getId() : ptmStdSku.getId(), ptmStdSku, product);
//            logger.error(String.format("[NonMatchedProductException]:query=[%s].site=[%s].price=[%s].page=[%d, %d]", product.getTitle(), product.getSourceSite(), product.getPrice(), page, pageSize));
            jsonObject.put("data", JSONObject.toJSON(cr));
            System.out.println(e.getMessage());
            Httphelper.sendJsonMessage(JSON.toJSONString(jsonObject, propertyFilter), response);
            return null;
        }
        // 速度优化
        SearchHelper.addToLog(sio);
        logger.debug(sio.toString());
        apiUtils.resloveClass(cr);
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
                    logger.info(cmpSku.getId() + ", price=" + cmpSku.getPrice() + ", status=" + cmpSku.getStatus());
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
                logger.info(" getCmpResult(sio, cmpSkuIndex) record deepLink " + csv.getDeeplink());
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
     * 给vc开发用的
     *
     * @param url
     * @param page
     * @param pageSize
     * @return
     */
    @DataSource(value = DataSourceType.Slave)
    @RequestMapping(value = "/newnewconfig", method = RequestMethod.POST)
    @ResponseBody
    public String newnewconfig(@RequestParam String url,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "20") int pageSize) {

        String json = "";

        //先手sourceSid匹配，如果沒有数据，再走原有的接口
        //上传的url不为空
        if (!org.apache.commons.lang3.StringUtils.isBlank(url)) {

            Website website = WebsiteHelper.getWebSite(url);

            String sourceSid = WebsiteHelper.getSkuIdFromUrl(website, url);

            //todo 这个方法没有加缓存
            json = getSkuListBySourceSidAndWebsite(website, sourceSid, page, pageSize);

            //判断如果上面还是空的,走/cmp/sdk/cmpskus
            if (StringUtils.isEmpty(json)) {

//                SearchIO sio = new SearchIO(sourceId, q, brand, site, price, deviceInfo.getMarketChannel(), deviceId, page, pageSize);
                SearchIO sio = new SearchIO(sourceSid, "", "", website.name(), "0", null, "", page, pageSize);
                getSioBySearch(sio);
                json = appCmpService.sdkCmpSku(sio);

            }
        }

        return json;
    }

    /**
     * 这是根据sourceSid+website直接走数据库的匹配逻辑
     *
     * @param website
     * @param sourceSid
     * @return
     */
    private String getSkuListBySourceSidAndWebsite(Website website, String sourceSid, int page, int pageSize) {
        //网站不为空
        if (website != null) {

            if (Website.SNAPDEAL.equals(website) || Website.FLIPKART.equals(website)) {

                List<PtmCmpSku> skuList = cmpSkuService.getPtmCmpSkuListBySourceSidAndWebsite(sourceSid, Website.FLIPKART, page, pageSize);

                //这里返回的是一个sourceSid与请求url相同且网站是FLIPKART的集合
                //可能会根据某个条件筛选来进行优化，
                //此处暂时有数据就返回的策略
                if (skuList != null && skuList.size() != 0) {
                    //外层循环用来控制遍历数据库返回的skuList
                    for (int i = 0; i < skuList.size() - 1; i++) {

                        PtmCmpSku ptmCmpSku = skuList.get(i);

                        List<PtmCmpSku> resultSkuList = cmpSkuService.listCmpSkus(ptmCmpSku.getProductId());

                        //内层循环用来控制遍历按照sku的productId返回的sku列表
                        for (int j = resultSkuList.size() - 1; j >= 0; j--) {
                            ptmCmpSku = resultSkuList.get(j);
                            //价格为0过滤
                            if (ptmCmpSku.getPrice() <= 0) {
                                resultSkuList.remove(j);
                                continue;
                            }
                            //状态不是onsale过滤
                            if (ptmCmpSku.getStatus() != SkuStatus.ONSALE) {
                                resultSkuList.remove(j);
                                continue;
                            }
                        }

                        //循环过滤完毕，判断时候有数据剩余
                        if (skuList.size() != 0) {
                            return JSONUtil.toJSON(resultSkuList);
                        }
                    }
                }
            }
        }
        return "";
    }


    /**
     * get cmp product results
     *
     * @param sio
     * @return
     */
    private CmpResult getCmpProducts(SearchIO sio, PtmProduct product, String userToken) {
        //初始化一个空的用于存放比价商品列表的List
        List<CmpProductListVo> comparedSkuVos = new ArrayList<CmpProductListVo>();
        CmpResult cmpResult = new CmpResult();
        //从ptmCmpSku表获取 productId为指定值、且状态为ONSALE 按照价格升序排列
        PageableResult<PtmCmpSku> pagedCmpskus = productCacheManager.listPagedCmpSkus(product.getId(), sio.getPage(), sio.getSize());
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            System.out.println("get skus size is " + pagedCmpskus.getData().size());
            List<PtmCmpSku> cmpSkus = pagedCmpskus.getData();
            System.out.println(" cmpskus size is " + cmpSkus.size());
            //评论数按照加权平均值展示
            Long tempTotalComments = Long.valueOf(0);
            //统计site
//            Set<Website> websiteSet = new HashSet<Website>();
            //初始化price为客户端传输的price
            if (ArrayUtils.hasObjs(cmpSkus)) {
                // 获取vo list
                for (PtmCmpSku cmpSku : cmpSkus) {
//                    if (cmpSku.getWebsite() == null
//                            || cmpSku.getPrice() <= 0
//                            ) {
//                        continue;
//                    }
//                    if (cmpSku.getWebsite() != null) {
//                        websiteSet.add(cmpSku.getWebsite());
//                    }
                    // 忽略前台返回的价格
//                    System.out.println("sku smallImagePath is " + cmpSku.getSmallImagePath());
                    CmpProductListVo cplv = new CmpProductListVo(cmpSku, WebsiteHelper.getLogoUrl(cmpSku.getWebsite()));
//                    System.out.println("after set , imageUrl is  " + cplv.getImageUrl());
                    cplv.setDeepLinkUrl(WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()}));

                    logger.info(" getCmpProducts record deepLinkUrl :" + cplv.getDeepLinkUrl());
                    cplv.setDeepLink(WebsiteHelper.getDeeplinkWithAff(cmpSku.getWebsite(), cmpSku.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()}));
                    logger.info(" getCmpProducts record deepLink :" + cplv.getDeepLinkUrl());
                    if (!StringUtils.isEmpty(userToken)) {
                        cplv.setIsAlert(apiUtils.isPriceOffAlert(userToken, cplv.getId()));
                    }
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
            //计算评论数*星级的总和
            int sum = 0;
//            System.out.println("iterator  comparedSkuVos , and  it is size is " + comparedSkuVos.size());
            for (CmpProductListVo cmpProductListVo : comparedSkuVos) {
//                if (websiteSet.size() <= 0) {
//                    break;
//                }
//                if (websiteSet.contains(cmpProductListVo.getWebsite())) {
//                    websiteSet.remove(cmpProductListVo.getWebsite());
                //去除列表中除此之外的其他此site的数据
                if (!cmpProductListVo.getWebsite().equals(Website.EBAY)) {
//                    System.out.println("not ebay ");
                    //评论数*星级 累加 除以评论数和
                    sum += cmpProductListVo.getTotalRatingsNum() * cmpProductListVo.getRatingNum();
                    tempTotalComments += cmpProductListVo.getTotalRatingsNum();
                }
                //获取offers
//                System.out.println(" get offers from mongoDb ");
//                System.out.println(" cmpProductListVo " + cmpProductListVo.getId() + "  : price : " + cmpProductListVo.getPrice());
                PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, cmpProductListVo.getId());
                List<String> offer = new ArrayList<>();
                if (ptmCmpSkuDescription != null) {
                    String offers = ptmCmpSkuDescription.getOffers();
//                    System.out.println(" got it ,and offers is " + offers);
                    if (!StringUtils.isEmpty(offers)) {
                        String[] temps = offers.split(",");
                        for (String str : temps) {
                            offer.add(str);
                        }
                        cmpProductListVo.setOffers(offer);
                    }
                }
                //将hasoffer coin拼接返回
                if (cmpProductListVo.getWebsite().name().equals("FLIPKART")) {
                    //如果是flipkart,则添加hasoffer coin
                    offer.add("Extra " + cmpProductListVo.getCoins() + " Hasoffer Coins");

                }
                tempCmpProductListVos.add(cmpProductListVo);
//                }
            }
            //移除之前加进列表的所有的sku列表
            comparedSkuVos = null;
            comparedSkuVos = new ArrayList<>();
            //将新的加入的放入到列表中
            comparedSkuVos.addAll(tempCmpProductListVos);
            String imageUrl = productCacheManager.getProductMasterImageUrl(product.getId());
            cmpResult.setImage(imageUrl);
            cmpResult.setName(product.getTitle());
            PageableResult<CmpProductListVo> priceList = new PageableResult<>(comparedSkuVos, pagedCmpskus.getNumFund(), pagedCmpskus.getCurrentPage(), pagedCmpskus.getPageSize());
            cmpResult.setBestPrice(priceList.getData().get(0).getPrice());
            cmpResult.setPriceList(priceList.getData());
            int rating = ClientHelper.returnNumberBetween0And5(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(tempTotalComments == 0 ? 1 : tempTotalComments), 0, BigDecimal.ROUND_HALF_UP).longValue());
            cmpResult.setRatingNum(rating);
            PtmProductDescription ptmProductDescription = mongoDbManager.queryOne(PtmProductDescription.class, product.getId());
            String specs = "";
            if (ptmProductDescription != null) {
                specs = ptmProductDescription.getJsonDescription();
            }
            cmpResult.setPagedComparedSkuVos(priceList);
            cmpResult.setSpecs(specs);
            //cmpResult.setTotalRatingsNum(WeightedAverage.divide(BigDecimal.ONE, 0, BigDecimal.ROUND_HALF_UP).longValue());
            cmpResult.setTotalRatingsNum(tempTotalComments);
            return cmpResult;
        }
        return cmpResult;
    }

    public void getSioBySearch(SearchIO sio) {

        String q = sio.getCliQ();

        String logId = HexDigestUtil.md5(q + "-" + sio.getCliSite().name()); // 这个值作为log表的id

        SrmSearchLog srmSearchLog = searchLogCacheManager.findSrmSearchLog(logId, true);

        if (srmSearchLog != null
                && (srmSearchLog.getPrecise() == SearchPrecise.TIMERSET2
                || srmSearchLog.getPrecise() == SearchPrecise.MANUALSET)) {

            if (srmSearchLog.getPtmProductId() <= 0) {
//                System.out.println("Found search log. but product id is 0 .");
                throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, sio.getCliQ(), "", 0);
            }

            sio.set(srmSearchLog.getCategory(), srmSearchLog.getPtmProductId(), srmSearchLog.getPtmCmpSkuId());
//            System.out.println("getHsProId  :" + sio.getHsProId());
        } else {
            if (srmSearchLog == null) {
//                System.out.println("srmSearchLog is null");
                sio.setFirstSearch(true);
//                System.out.println("setFirstSearch is true");
            }

            try {
//                System.out.println("searchForResult  ");
                searchForResult(sio);
                System.out.println(" searchForResult result  " + sio.getHsProId());
            } catch (NonMatchedProductException e) {
//                System.out.println("searchForResult_old  ");
                System.out.println(" searchForResult_old result  " + sio.getHsProId());
            }
        }
    }

    public void getPtmStdPriceBySioFromSolr(SearchIO sio) {
        PageableResult<PtmStdPriceModel> pricesList = ptmStdPriceIndexService.searchPrices(sio, 1, 5);
        if (pricesList != null && pricesList.getData() != null && pricesList.getData().size() > 0) {
            for (PtmStdPriceModel ptmStdPriceModel : pricesList.getData()) {
                float mc = StringUtils.wordMatchD(StringUtils.toLowerCase(ptmStdPriceModel.getTitle()), sio.getCliQ());
                if (mc >= 0.5) {
                    sio.set(ptmStdPriceModel);
                    break;
                }
            }
        }
    }

    /**
     * 从solr中搜索
     *
     * @param sio
     */
    public void searchForResult(SearchIO sio) throws NonMatchedProductException {
        String _q = StringUtils.getSearchKey(sio.getCliQ());

        // 搜索SKU
        PageableResult<CmpSkuModel> pagedCmpskuModels = cmpskuIndexService.searchSku(_q, 1, 5);
        List<CmpSkuModel> skuModels = pagedCmpskuModels.getData();
//        System.out.println("skuModels   " + skuModels.size());

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
        System.out.println(" mc " + mc);
        // 匹配度如果小于40%, 则认为不匹配
        if (mc <= 0.4) {
            throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, _q, title, mc);
        }

        long cateId = 0L;
        sio.set(cateId, skuModel.getProductId(), skuModel.getId());
    }

    private CmpResult getCmpPrices(SearchIO sio, PtmStdSku ptmStdSku, String userToken) {
        //初始化一个空的用于存放比价商品列表的List
        List<CmpProductListVo> comparedSkuVos = new ArrayList<>();
        CmpResult cmpResult = new CmpResult();
        PageableResult<PtmStdPrice> pagedCmpskus = ptmStdPriceService.getPagedPtmStdPriceList(ptmStdSku.getId(), SkuStatus.ONSALE, sio.getPage(), sio.getSize());
        if (pagedCmpskus != null && pagedCmpskus.getData() != null && pagedCmpskus.getData().size() > 0) {
            List<PtmStdPrice> cmpPriceList = pagedCmpskus.getData();
            //评论数按照加权平均值展示
            Long tempTotalComments = Long.valueOf(0);
            if (ArrayUtils.hasObjs(cmpPriceList)) {
                // 获取vo list
                for (PtmStdPrice ptmStdPrice : cmpPriceList) {
                    CmpProductListVo cplv = new CmpProductListVo(ptmStdPrice, productCacheManager.getPtmStdSkuImageUrl(ptmStdSku.getId()), WebsiteHelper.getLogoUrl(ptmStdPrice.getWebsite()));
                    cplv.setDeepLinkUrl(WebsiteHelper.getDeeplinkWithAff(ptmStdPrice.getWebsite(), ptmStdPrice.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()}));
                    cplv.setDeepLink(WebsiteHelper.getDeeplinkWithAff(ptmStdPrice.getWebsite(), ptmStdPrice.getUrl(), new String[]{sio.getMarketChannel().name(), sio.getDeviceId()}));
                    if (!StringUtils.isEmpty(userToken)) {
                        cplv.setIsAlert(apiUtils.isPriceOffAlert(userToken, cplv.getId()));
                    }
                    comparedSkuVos.add(cplv);
                }
                if (ArrayUtils.isNullOrEmpty(comparedSkuVos)) {
                    throw new NonMatchedProductException(ERROR_CODE.UNKNOWN, "", ptmStdSku.getTitle(), ptmStdSku.getRefPrice());
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
            List<CmpProductListVo> tempCmpProductListVos = new ArrayList<>();
            int sum = 0;
            for (CmpProductListVo cmpProductListVo : comparedSkuVos) {
                if (!cmpProductListVo.getWebsite().equals(Website.EBAY)) {
                    sum += cmpProductListVo.getTotalRatingsNum() * cmpProductListVo.getRatingNum();
                    tempTotalComments += cmpProductListVo.getTotalRatingsNum();
                }
                PtmCmpSkuDescription ptmCmpSkuDescription = mongoDbManager.queryOne(PtmCmpSkuDescription.class, cmpProductListVo.getId());
                List<String> offer = new ArrayList<>();
                if (ptmCmpSkuDescription != null) {
                    String offers = ptmCmpSkuDescription.getOffers();
                    if (!StringUtils.isEmpty(offers)) {
                        String[] temps = offers.split(",");
                        for (String str : temps) {
                            offer.add(str);
                        }
                        cmpProductListVo.setOffers(offer);
                    }
                }
                if (cmpProductListVo.getWebsite().name().equals("FLIPKART")) {
                    offer.add("Extra " + cmpProductListVo.getCoins() + " Hasoffer Coins");

                }
                tempCmpProductListVos.add(cmpProductListVo);
            }
            //移除之前加进列表的所有的sku列表
            comparedSkuVos = null;
            comparedSkuVos = new ArrayList<>();
            //将新的加入的放入到列表中
            comparedSkuVos.addAll(tempCmpProductListVos);
            String imageUrl = productCacheManager.getPtmStdSkuImageUrl(ptmStdSku.getId());
            cmpResult.setImage(imageUrl);
            cmpResult.setName(ptmStdSku.getTitle());
            PageableResult<CmpProductListVo> priceList = new PageableResult<>(comparedSkuVos, pagedCmpskus.getNumFund(), pagedCmpskus.getCurrentPage(), pagedCmpskus.getPageSize());
            cmpResult.setBestPrice(priceList.getData().get(0).getPrice());
            cmpResult.setPriceList(priceList.getData());
            int rating = ClientHelper.returnNumberBetween0And5(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(tempTotalComments == 0 ? 1 : tempTotalComments), 0, BigDecimal.ROUND_HALF_UP).longValue());
            cmpResult.setRatingNum(rating);
            cmpResult.setPagedComparedSkuVos(priceList);
            cmpResult.setTotalRatingsNum(tempTotalComments);
            PtmStdSkuDetail ptmStdSkuDetail = mongoDbManager.queryOne(PtmStdSkuDetail.class, ptmStdSku.getId());
            Map<String, String> specsMap = new HashMap();
            ApiUtils.setParameters(specsMap, ptmStdSkuDetail.getParamGroups());
            cmpResult.setSpecs(JSON.toJSONString(specsMap));
            return cmpResult;
        }
        return cmpResult;
    }

}