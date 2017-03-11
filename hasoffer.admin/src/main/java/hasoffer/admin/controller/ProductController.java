package hasoffer.admin.controller;

import hasoffer.admin.controller.vo.CategoryVo;
import hasoffer.admin.controller.vo.CmpSkuVo;
import hasoffer.admin.controller.vo.ProductVo;
import hasoffer.base.enums.SearchResultSort;
import hasoffer.base.model.PageModel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.base.utils.IDUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.app.AppCacheService;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.persistence.dbm.osql.datasource.DataSource;
import hasoffer.core.persistence.dbm.osql.datasource.DataSourceType;
import hasoffer.core.persistence.mongo.PtmCmpSkuLog;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.sys.SysAdmin;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.ICmpSkuService;
import hasoffer.core.product.IFetchService;
import hasoffer.core.product.IProductService;
import hasoffer.core.product.exception.ProductNotFoundException;
import hasoffer.core.product.impl.PtmStdPriceServiceImpl;
import hasoffer.core.product.impl.PtmStdSKuServiceImpl;
import hasoffer.core.product.solr.CmpSkuIndexServiceImpl;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.utils.ImageUtil;
import hasoffer.core.utils.api.ApiUtils;
import hasoffer.fetch.model.OriFetchedProduct;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import hasoffer.webcommon.helper.PageHelper;
import jodd.io.FileUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2015/12/22.
 */
@Controller
@RequestMapping(value = "/p")
public class ProductController {

    @Resource
    ISearchService searchService;
    @Resource
    ICategoryService categoryService;
    @Resource
    IProductService productService;
    @Resource
    ProductIndex2ServiceImpl productIndex2Service;
    @Resource
    CmpSkuIndexServiceImpl cmpskuIndexService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IFetchService fetchService;
    @Resource
    ICacheService cacheServiceImpl;
    @Resource
    AppCacheService appCacheService;
    @Resource
    PtmStdPriceServiceImpl ptmStdPriceService;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    PtmStdSKuServiceImpl ptmStdSKuService;

    @RequestMapping(value = "/cmp/del/{id}", method = RequestMethod.GET)
    public ModelAndView delCompare(@PathVariable long id) {

        if (ApiUtils.removeBillion(id) > 0) {
            PtmStdPrice ptmStdPrice = ptmStdPriceService.getPtmStdPriceById(ApiUtils.removeBillion(id));
            if (ptmStdPrice != null) {
                ptmStdPriceService.removePriceById(ptmStdPrice.getId());
                appCacheService.getPtmStdPrice(ptmStdPrice.getId(), 1);
            } else {
                ptmStdPriceService.importPtmStdPrice2Solr(ApiUtils.removeBillion(id));
            }
        } else {
            PtmCmpSku cmpSku = cmpSkuService.getCmpSkuById(id);
            if (cmpSku == null) {
                cmpskuIndexService.remove(String.valueOf(id));
            } else {
                cmpSkuService.deleteCmpSku(id);
                appCacheService.getPtmCmpSku(id, 1);
            }
        }
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    @RequestMapping(value = "/cmp/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Map updateCompare(@PathVariable long id) {

        Map<String, String> map = new HashMap<String, String>();

        PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(id);

        String url = ptmCmpSku.getUrl();

        OriFetchedProduct oriFetchedProduct = null;
        try {
            oriFetchedProduct = fetchService.fetchSummaryProductByUrl(url);
            if (oriFetchedProduct != null) {
                cmpSkuService.updateCmpSkuByOriFetchedProduct(id, oriFetchedProduct);
                map.put("status", "success");
            } else {
                map.put("status", "fail");
            }
        } catch (Exception e) {
            map.put("status", "fail");
        }

        return map;
    }

    @DataSource(value = DataSourceType.Master)
    @RequestMapping(value = "/cmp/save", method = RequestMethod.POST)
    public ModelAndView saveCompare(HttpServletRequest request, MultipartFile skuFile) {
        String productId = request.getParameter("productId");
        ModelAndView mav = new ModelAndView("redirect:/p/cmp/" + productId);
        String id = request.getParameter("id");
        String url = request.getParameter("url");
        String oriImageUrl = request.getParameter("skuImageUrl");
        String title = request.getParameter("title");
        String skuStatus = request.getParameter("skuStatus");
        float price = 0.0f;
        String priceStr = request.getParameter("price");
        if (NumberUtils.isNumber(priceStr)) {
            price = Float.valueOf(priceStr);
        }
        String skuImageUrl = "";
        //修改了图片
        if (StringUtils.isEmpty(oriImageUrl)) {
            if (skuFile != null && !skuFile.isEmpty()) {
                try {
                    File imageFile = FileUtil.createTempFile(IDUtil.uuid(), ".jpg", null);
                    FileUtil.writeBytes(imageFile, skuFile.getBytes());
//                    skuImageUrl = ImageUtil.uploadImage(imageFile);
                    skuImageUrl = ImageUtil.uploadImage(imageFile, 240, 240);
                } catch (Exception e) {
                    return mav;
                }
            }
        }

        String color = request.getParameter("color");
        String size = request.getParameter("size");
        if (!StringUtils.isEmpty(id)) {
            // 有商品id就是更新
            if (ApiUtils.removeBillion(Long.parseLong(id)) < 0) {
                cmpSkuService.updateCmpSku(Long.valueOf(id), url, color, size, price, skuStatus, title, skuImageUrl);
                appCacheService.getPtmCmpSku(Long.parseLong(id), 1);
            } else {
                //大id
                PtmStdPrice ptmStdPrice = appCacheService.getPtmStdPrice(ApiUtils.removeBillion(Long.parseLong(id)));
                if (ptmStdPrice != null) {
                    ptmStdPrice.setPrice(price);
                    ptmStdPrice.setUrl(org.apache.commons.lang3.StringUtils.isEmpty(url) ? ptmStdPrice.getUrl() : url);
                    ptmStdPrice.setSkuStatus(SkuStatus.valueOf(skuStatus));
                    ptmStdPriceService.update(ptmStdPrice);
                    appCacheService.getPtmStdPrice(ptmStdPrice.getId(), 1);
                }
            }
        } else {
            // 无商品id就是创建
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(productId) && Long.parseLong(productId) > 0) {
                if (ApiUtils.removeBillion(Long.parseLong(productId)) > 0) {
                    Long aLong = ptmStdPriceService.create(new PtmStdPrice(Long.parseLong(ApiUtils.removeBillion(Long.parseLong(productId)) + ""), price, SkuStatus.valueOf(skuStatus), url));
                    if (aLong > 0) {
                        appCacheService.getPtmStdPrice(aLong, 1);
                    }
                } else {
                    PtmCmpSku cmpSku = cmpSkuService.createCmpSku(Long.valueOf(productId), url, color, size, price, skuStatus, title, skuImageUrl);
                    if (cmpSku != null) {
                        appCacheService.getPtmCmpSku(cmpSku.getId(), 1);
                    }
                }
            }

        }
        return mav;
    }

    @RequestMapping(value = "/cmp/{id}", method = RequestMethod.GET)
    @DataSource(value = DataSourceType.Slave)
    public ModelAndView listCompares(@PathVariable long id) throws ProductNotFoundException {
        ModelAndView mav = new ModelAndView("product/cmp");
        mav.addObject("pId", id);
        PtmStdSku ptmStdSku = null;
        PtmProduct product = null;
        if (ApiUtils.removeBillion(id) > 0) {
            ptmStdSku = appCacheService.getPtmStdSku(ApiUtils.removeBillion(id));
        } else {
            product = productService.getProduct(id);
        }

        //skuImageUrl
        PageableResult<PtmStdPrice> pagedCmpskus;
        PageableResult<PtmCmpSku> pagedCmpSkus;
        List<CmpSkuVo> cmpSkuVos = new ArrayList<>();
        if (ptmStdSku != null) {
            pagedCmpskus = ptmStdPriceService.getPagedPtmStdPriceList(ptmStdSku.getId(), SkuStatus.ONSALE, 1, Integer.MAX_VALUE);
            if (pagedCmpskus != null) {
                List<PtmStdPrice> data = pagedCmpskus.getData();
                for (PtmStdPrice ptmStdPrice : data) {
                    CmpSkuVo cmpSkuVo = new CmpSkuVo(ptmStdPrice);
                    cmpSkuVo.setImageUrl(productCacheManager.getPtmStdSkuImageUrl(ptmStdPrice.getStdSkuId()));
                    cmpSkuVos.add(cmpSkuVo);
                }
            }
        } else if (product != null) {
            pagedCmpSkus = productService.listPagedCmpSkus(id, 1, Integer.MAX_VALUE);
            List<PtmCmpSku> cmpSkus = pagedCmpSkus.getData();
//            Set<String> colors = new HashSet<String>();
//            Set<String> sizes = new HashSet<String>();
            if (ArrayUtils.hasObjs(cmpSkus)) {
                for (PtmCmpSku cmpSku : cmpSkus) {
                    cmpSkuVos.add(new CmpSkuVo(cmpSku));

//                    if (!StringUtils.isEmpty(cmpSku.getColor())) {
//                        colors.add(cmpSku.getColor());
//                    }
//                    if (!StringUtils.isEmpty(cmpSku.getSize())) {
//                        sizes.add(cmpSku.getSize());
//                    }
//                    List<PtmCmpSkuLog> logs = null;//cmpSkuService.listByPcsId(cmpSku.getId());
//                    if (ArrayUtils.hasObjs(logs)) {
//                        priceLogMap.put(cmpSku, logs);
//                    }
                }
            }
        }
        mav.addObject("cmpSkus", cmpSkuVos);
        if (ptmStdSku == null) {
            mav.addObject("product", getProductVo(product));
        } else {
            ProductVo productVo = new ProductVo(ptmStdSku);
            productVo.setMasterImageUrl(productCacheManager.getPtmStdSkuImageUrl(ptmStdSku.getId()));
            productVo.setCategories(getCategories(ptmStdSku.getCategoryId()));
            mav.addObject("product", productVo);
        }
//        mav.addObject("skuColors", JSONUtil.toJSON(colors));
//        mav.addObject("skuSizes", JSONUtil.toJSON(sizes));
        //获取sku状态列表
        mav.addObject("skuStatus", SkuStatus.values());
//        List<String> days = new ArrayList<String>();
//        Map<Website, List<Float>> priceMap = new HashMap<Website, List<Float>>();
//        getPriceLogs(priceLogMap, days, priceMap);
//        mav.addObject("priceMap", JSONUtil.toJSON(ChartHelper.getChartData(priceMap)));
//        mav.addObject("priceDays", JSONUtil.toJSON(days));
//        mav.addObject("showCharts", ArrayUtils.hasObjs(days));

        return mav;
    }

    private void getPriceLogs(Map<PtmCmpSku, List<PtmCmpSkuLog>> priceLogMap, List<String> days, Map<Website, List<Float>> priceMap) {

        if (priceLogMap == null || priceLogMap.size() <= 0) {
            return;
        }

        // 先分析价格的日期区间
        final String DATE_PATTERN = "yyyyMMdd";
        String startDay = "30000000", endDay = "00000000";

        Map<Website, Map<String, Float>> priceLogMap2 = new HashMap<Website, Map<String, Float>>();

        for (Map.Entry<PtmCmpSku, List<PtmCmpSkuLog>> kv : priceLogMap.entrySet()) {
            PtmCmpSku cmpSku = kv.getKey();
            List<PtmCmpSkuLog> logs = kv.getValue();

            Map<String, Float> subMap = new HashMap<String, Float>();

            for (PtmCmpSkuLog log : logs) {
                String ymd = TimeUtils.parse(log.getPriceTime(), DATE_PATTERN);
                subMap.put(ymd, log.getPrice());

                if (startDay.compareTo(ymd) > 0) {
                    startDay = ymd;
                }
                if (endDay.compareTo(ymd) < 0) {
                    endDay = ymd;
                }
            }

            String ymd = TimeUtils.parse(cmpSku.getUpdateTime(), DATE_PATTERN);
            if (!subMap.containsKey(ymd)) {
                subMap.put(ymd, cmpSku.getPrice());

                if (endDay.compareTo(ymd) < 0) {
                    endDay = ymd;
                }
            }

            priceLogMap2.put(cmpSku.getWebsite(), subMap);
        }

        TimeUtils.fillDays(days, startDay, endDay, DATE_PATTERN);
        fillPriceMap(priceLogMap2, priceMap, days);
    }

    private void fillPriceMap(Map<Website, Map<String, Float>> priceLogMap, Map<Website, List<Float>> priceMap, List<String> days) {
        for (Map.Entry<Website, Map<String, Float>> kv : priceLogMap.entrySet()) {
            Website website = kv.getKey();

            if (website == null) {
                continue;
            }

            Map<String, Float> logMap = kv.getValue();
            List<Float> prices = new ArrayList<Float>();

            for (String ymd : days) {
                Float price = logMap.get(ymd);
                if (price == null) {
                    price = 0.0F;
                }
                prices.add(price);
            }
            priceMap.put(website, prices);
        }
    }


    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public ModelAndView listProducts(@PathVariable long id) throws ProductNotFoundException {

        ModelAndView mav = new ModelAndView("product/detail");

        PtmProduct product = productService.getProduct(id);
        if (product == null) {
            throw new ProductNotFoundException();
        }

        mav.addObject("product", getProductVo(product));
        mav.addObject("imageUrls", productService.getProductImageUrls(id));
        mav.addObject("features", new ArrayList<>());
        mav.addObject("basicAttributes", new ArrayList<>());

        return mav;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView listProducts(HttpServletRequest request,
                                     @RequestParam(required = false) String title,
                                     @RequestParam(defaultValue = "0") int category1,
                                     @RequestParam(defaultValue = "0") int category2,
                                     @RequestParam(defaultValue = "0") int category3,
                                     @RequestParam(defaultValue = "RELEVANCE") SearchResultSort sort,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {

        ModelAndView mav = new ModelAndView("product/list");

        PageableResult<ProductModel2> pagedResults = null;
        List<PtmProduct> products = null;
        PageModel pageModel = null;

        SearchCriteria sc = new SearchCriteria();
        if (category3 > 0) {
            sc.setCategoryId(String.valueOf(category3));
            sc.setLevel(3);
        } else if (category2 > 0) {
            sc.setCategoryId(String.valueOf(category2));
            sc.setLevel(2);
        } else if (category1 > 0) {
            sc.setCategoryId(String.valueOf(category1));
            sc.setLevel(1);
        }
        sc.setKeyword(title);
        sc.setPageSize(size);
        sc.setPage(page);
        sc.setSort(sort);

        pagedResults = productIndex2Service.searchProducts(sc);

//        products = productService.getProducts(pagedResults.getData());

        pageModel = PageHelper.getPageModel(request, pagedResults);

        mav.addObject("products", getProductVos2(pagedResults.getData()));
        mav.addObject("page", pageModel);

        return mav;
    }

    private List<ProductVo> getProductVos2(List<ProductModel2> products) {
        List<ProductVo> productVos = new ArrayList<ProductVo>();
        if (ArrayUtils.isNullOrEmpty(products)) {
            return productVos;
        }

        for (ProductModel2 product : products) {
            productVos.add(getProductVo2(product));
        }
        return productVos;
    }

    private ProductVo getProductVo2(ProductModel2 p) {
        ProductVo vo = new ProductVo();

        vo.setId(p.getId());

        vo.setTitle(p.getTitle());
        vo.setTag(p.getTag());

        vo.setPrice(p.getMinPrice());
        vo.setMinPrice(p.getMinPrice());

        vo.setBrand(p.getBrand());

        vo.setMasterImageUrl(productService.getProductMasterImageUrl(p.getId()));

        return vo;
    }

    @RequestMapping(value = "/updateTag", method = RequestMethod.POST)
    public ModelAndView updateTag(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("product/list");

        String proId = request.getParameter("id");
        String tag = request.getParameter("tag");

        productService.updateProductTag(proId, tag);

        mav.addObject("result", "ok");

        return mav;
    }

    @RequestMapping(value = "/updateBrand", method = RequestMethod.POST)
    public ModelAndView updateBrand(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("product/list");

        String proId = request.getParameter("id");
        String brand = request.getParameter("brand");

        productService.updateProductBrand(Long.valueOf(proId), brand);

        mav.addObject("result", "ok");

        return mav;
    }

    private List<ProductVo> getProductVos(List<PtmProduct> products) {
        List<ProductVo> productVos = new ArrayList<ProductVo>();
        if (ArrayUtils.isNullOrEmpty(products)) {
            return productVos;
        }

        for (PtmProduct product : products) {
            productVos.add(getProductVo(product));
        }
        return productVos;
    }

    private ProductVo getProductVo(PtmProduct p) {
        ProductVo vo = new ProductVo();

        vo.setId(p.getId());
        vo.setCreateTime(p.getCreateTime());

        vo.setCategories(getCategories(p.getCategoryId()));

        vo.setTitle(p.getTitle());
        vo.setTag(p.getTag());
        vo.setPrice(p.getPrice());

        vo.setMasterImageUrl(productService.getProductMasterImageUrl(p.getId()));
        vo.setDescription(p.getDescription());

        vo.setColor(p.getColor());
        vo.setSize(p.getSize());
        vo.setRating(p.getRating());

        vo.setSourceSite(p.getSourceSite());
        vo.setSourceId(p.getSourceId());

        return vo;
    }

    private List<CategoryVo> getCategories(long categoryId) {
        List<CategoryVo> categoryVos = new ArrayList<CategoryVo>();
        List<PtmCategory> ptmCategories = categoryService.getRouterCategoryList(categoryId);

        if (ArrayUtils.hasObjs(ptmCategories)) {
            for (PtmCategory category : ptmCategories) {
                categoryVos.add(new CategoryVo(category));
            }
        }

        return categoryVos;
    }

    /**
     * 新增PtmProduct
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public PtmProduct createProduct(HttpServletRequest request, @RequestParam(defaultValue = "0") int category3) throws UnsupportedEncodingException {
        //创建一个sku TODO 胡礼

        //1. 在库中创建一个sku,如果此sku没有对应商品则创建一个商品,sku放入缓存
        //2. 更新商品价格
        //3. 重新导入solr


        long catagoryId = category3;
        String data = request.getParameter("data");
        //表单数据是经过serialize的，所以传上来的数据需要经过decode使用，待测
        data = URLDecoder.decode(data);
        String[] subStrs1 = data.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String str : subStrs1) {
            String[] subStrs2 = str.split("=");
            if (subStrs2.length == 1) {
                map.put(subStrs2[0], "");
            } else {
                map.put(subStrs2[0], subStrs2[1]);
            }
        }
        String url = map.get("url");
        String title = map.get("title");
        String priceString = map.get("price");
        float price = 0.0f;
        if (NumberUtils.isNumber(priceString)) {
            price = Float.parseFloat(priceString);
        }
        String description = map.get("description");
        String colors = map.get("skus");
        String sizes = map.get("sANs");
        String website = map.get("website");
        String sourceId = map.get("sourceId");
        String color = "";
        String size = "";

        //创建PtmProduct
        PtmProduct product = productService.createProduct(catagoryId, title, price, description, colors, sizes, 0, website, sourceId);

        //newProoduct的日志
        SysAdmin admin = (SysAdmin) Context.currentContext().get(StaticContext.USER);

        //添加cmpSku
        PtmCmpSku cmpSku = cmpSkuService.createCmpSku(product.getId(), url, color, size, price);

        return product;
    }

    @RequestMapping(value = "/batchDelete", method = RequestMethod.GET)
    @ResponseBody
    public boolean batchDelete(@RequestParam(value = "ids[]") Long[] ids) {
        for (long id : ids) {
            if (ApiUtils.removeBillion(id) > 0) {
                PtmStdPrice ptmStdPrice = ptmStdPriceService.getPtmStdPriceById(ApiUtils.removeBillion(id));
                if (ptmStdPrice != null) {
                    //如果存在
                    //1. 从数据库删除
                    ptmStdPriceService.removePriceById(ApiUtils.removeBillion(id));
                    //2. 从缓存中删除
                    appCacheService.getPtmStdPrice(ApiUtils.removeBillion(id), 1);
                } else {
                    ptmStdPriceService.importPtmStdPrice2Solr(ApiUtils.removeBillion(id));
                }
            } else {
                PtmCmpSku cmpSku = cmpSkuService.getCmpSkuById(id);
                if (cmpSku != null) {
                    cmpSkuService.deleteCmpSku(cmpSku.getId());
                    appCacheService.getPtmCmpSku(cmpSku.getId(), 1);
                } else {
                    cmpskuIndexService.remove(id + "");
                }
            }
        }
        return true;
    }

    @RequestMapping(value = "/removeCache/{productId}", method = RequestMethod.POST)
    @ResponseBody
    public Map removeCache(@PathVariable Long productId) {
        Map<String, String> statusMap = new HashMap<>();
        try {
            if (ApiUtils.removeBillion(productId) > 0) {
                PtmStdSku ptmStdSku = ptmStdSKuService.getStdSkuById(ApiUtils.removeBillion(productId));
                if (ptmStdSku != null) {
                    //1. 更改商品价格,可能变更了最低价格
                    ptmStdSKuService.updatePtmStdSkuPrice(ApiUtils.removeBillion(productId));
                    //2. 重新导入solr
                    ptmStdSKuService.importPtmStdSku2Solr(ptmStdSku);
                    //3. 清除改stdSku的缓存
                    appCacheService.getPtmStdSku(ApiUtils.removeBillion(productId), 2);
                }
            } else {
                //1. 更新product价格,重新导入prodcut
                productService.updatePtmProductPrice(productId);
                //2. 清除商品缓存
                appCacheService.getPtmProduct(productId, 2);
            }
            statusMap.put("status", "success");
        } catch (Exception e) {
            statusMap.put("status", "fail");
        }
        return statusMap;
    }
}
