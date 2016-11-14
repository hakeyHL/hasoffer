package hasoffer.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.admin.controller.vo.TitleCountVo;
import hasoffer.admin.worker.*;
import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.exception.ImageDownloadOrUploadException;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.ImagePath;
import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.*;
import hasoffer.core.analysis.ProductAnalysisService;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.HijackLog;
import hasoffer.core.persistence.mongo.MobileCateDescription;
import hasoffer.core.persistence.mongo.PtmCmpSkuDescription;
import hasoffer.core.persistence.mongo.UrmDeviceRequestLog;
import hasoffer.core.persistence.po.ptm.*;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuIndex2Updater;
import hasoffer.core.persistence.po.ptm.updater.PtmCmpSkuUpdater;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.persistence.po.sys.SysAdmin;
import hasoffer.core.product.*;
import hasoffer.core.product.solr.CmpSkuModel;
import hasoffer.core.product.solr.CmpskuIndexServiceImpl;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.task.ListProcessTask;
import hasoffer.core.task.worker.ILister;
import hasoffer.core.task.worker.IProcessor;
import hasoffer.core.task.worker.impl.ListProcessWorkerStatus;
import hasoffer.core.user.IDeviceService;
import hasoffer.core.utils.Httphelper;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import hasoffer.fetch.sites.paytm.PaytmHelper;
import hasoffer.fetch.sites.shopclues.ShopcluesHelper;
import hasoffer.webcommon.context.Context;
import hasoffer.webcommon.context.StaticContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.*;

import static hasoffer.base.utils.HtmlUtils.getSubNodesByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Date : 2016/3/25
 * Function :
 */
@Controller
@RequestMapping(value = "/fixdata")
public class FixController {

    public static final String WEBSITE_91MOBILE_URL_PREFIEX = "http://www.91mobiles.com";

    private static final String Q_SKU_PRODUCTID = "SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ORDER BY t.id";
    private static final String Q_PTMCMPSKU = "SELECT t FROM PtmCmpSku t WHERE t.productId < 100000";
    private static final String Q_INDEX = "SELECT t FROM PtmCmpSkuIndex2 t ORDER BY t.id ASC";

    private final static String Q_TITLE_COUNT = "SELECT t.title,COUNT(t.id) FROM PtmProduct t WHERE t.title is not null GROUP BY t.title HAVING COUNT(t.id) > 1 ORDER BY COUNT(t.id) DESC";
    private final static String Q_PRODUCT_BY_TITLE = "SELECT t FROM PtmProduct t WHERE t.title = ?0 ORDER BY t.id ASC";

    private static Logger logger = LoggerFactory.getLogger(FixController.class);
    @Resource
    IProductService productService;
    @Resource
    ISearchService searchService;
    @Resource
    ICmpSkuService cmpSkuService;
    @Resource
    IFetchService fetchService;
    @Resource
    IDeviceService deviceService;
    @Resource
    IDataFixService dataFixService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    CmpskuIndexServiceImpl cmpskuIndexService;
    @Resource
    ICategoryService categoryservice;
    @Resource
    ICacheService cacheServiceImpl;
    private LinkedBlockingQueue<TitleCountVo> titleCountQueue = new LinkedBlockingQueue<TitleCountVo>();

    private Website[] websites = {
            Website.ASKMEBAZAAR,
            Website.INDIATIMES,
            Website.CROMARETAIL,
            Website.CROMA,
            Website.HOMESHOP18,
            Website.BAGITTODAY,
            Website.THEITDEPOT,
            Website.SAHOLIC,
            Website.FIRSTCRY,
            Website.EDABBA,
            Website.GADGETS360,
            Website.MANIACSTORE,
            Website.SYBERPLACE,
            Website.BABYOYE,
            Website.SHOPMONK,
            Website.PURPLLE,
            Website.NAAPTOL,
            Website.ZOOMIN
    };

    //fixdata/addUrlKeyForPtmCmpSku
    @RequestMapping(value = "/addUrlKeyForPtmCmpSku/{type}/{ptmcmpskuId}", method = RequestMethod.GET)
    @ResponseBody
    public String addUrlKeyForPtmCmpSku(@PathVariable long ptmcmpskuId, @PathVariable int type) throws Exception {

        String queryString = "";

        ExecutorService executorService = Executors.newCachedThreadPool();


        ListProcessWorkerStatus<PtmCmpSku> ws = new ListProcessWorkerStatus<>();

        if (type == 0) {//更新urlKey为null的
            queryString = "SELECT t FROM PtmCmpSku t WHERE t.urlKey is null ORDER BY t.id";
            executorService.execute(new MysqlListWorker2(queryString, ws, dbm));
        } else if (type == 1) {//从指定的最小id开始更新
            queryString = "SELECT t FROM PtmCmpSku t WHERE t.id > ?0 ORDER BY t.id";
            executorService.execute(new MysqlListWorker2(queryString, ws, dbm, ptmcmpskuId));
        } else {
            return "error";
        }

        for (int i = 0; i < 50; i++) {
            executorService.execute(new UrlKeyFixWorker(ws, cmpSkuService));
        }

        return "ok";
    }


    //fixdata/mobile91Fetch
    @RequestMapping(value = "/mobile91Fetch", method = RequestMethod.GET)
    @ResponseBody
    public String mobile91Fetch() throws Exception {

        String prefix = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.5544784158021026&requestType=2&listType=list&selMobSort=views&amount=1000%3B45000&sCatName=phone&price_range_apply=0&tr_fl%5B%5D=mob_market_status_filter.marketstatus_filter%3Aava_stores&search=&hidFrmSubFlag=1&page=";
        String suffix = "&category=mobile&unique_sort=&hdnCategory=mobile&user_search=&url_feat_rule=";
        int num = 2;

        for (int i = 0; i < 119; i++) {
            String url = prefix + num + suffix;
            System.out.println(url + " FETCH START");
            mobile91Fetch(url);
            System.out.println(url + " FETCH END");
            num++;
        }

        return "ok";
    }

    @RequestMapping(value = "/cate91Fetch", method = RequestMethod.GET)
    @ResponseBody
    public String category91Fetch() throws Exception {
        int totalPageSize = 0;
        int limitSize = 0;
        String jsonReqUrlList = "http://api.91mobiles.com:8080/nm-community/api/searchPage/web";

        List<String> apiJsonCates = new ArrayList<>();
        apiJsonCates.add("camera");
        apiJsonCates.add("tablet");
        apiJsonCates.add("tv");


        List<String> htmlReqUrlList = new ArrayList<>();
        htmlReqUrlList.add("http://www.91mobiles.com/mobile-memory-card-finder.php");
        htmlReqUrlList.add("http://www.91mobiles.com/mobile-power-bank-finder.php");
        htmlReqUrlList.add("http://www.91mobiles.com/smartwatchfinder.php");

        for (String cate : apiJsonCates) {
            JSONObject jsonObject = new JSONObject();
            //t 当前时间戳
            jsonObject.put("t", new Date().getTime());
            //q:搜索关键字
            jsonObject.put("q", cate);
            //srtBy:score
            jsonObject.put("srtBy", "score");
            //srtType:desc
            jsonObject.put("srtType", "desc");
            //limit 20
            if (limitSize < 1) {
                limitSize = 20;
            }
            jsonObject.put("limit", limitSize);
            //startRow 0
            //get total page
            try {
                jsonObject.put("startRow", 0);
                String postResultString = Httphelper.doPost(jsonReqUrlList, jsonObject.toJSONString());
                if (!StringUtils.isEmpty(postResultString)) {
                    JSONObject jsonResult = JSONObject.parseObject(postResultString);
                    Integer productCount = jsonResult.getInteger("productCount");
                    if (productCount > 0) {
                        if (productCount % limitSize != 0) {
                            //+1
                            totalPageSize = (productCount / limitSize) + 1;
                        } else {
                            totalPageSize = productCount / limitSize;
                        }
                    }
                }
            } catch (Exception e) {
                logger.info(" calculate totalPage exception {}", e.getMessage());
            }
            int num = 0;
            for (int i = 0; i < totalPageSize; i++) {
                jsonObject.put("startRow", i * jsonObject.getInteger("limit"));
                System.out.println(jsonReqUrlList + " _ " + cate + "  FETCH START");
                Cate91Fetch(jsonReqUrlList, jsonObject);
                System.out.println(jsonReqUrlList + " _ " + cate + " FETCH END");
                num++;
            }
            System.out.println("total num " + num);
        }
        totalPageSize = 14;
        int num = 0;
        for (String htmlUrl : htmlReqUrlList) {
            for (int i = 1; i < totalPageSize + 1; i++) {
                htmlUrl = htmlUrl + "?page=" + i;
                System.out.println(htmlUrl + " html  FETCH START");
                Cate91FetchHtml(htmlUrl);
                System.out.println(htmlUrl + " html  FETCH END");
                num++;
            }
        }
        return "ok";
    }

    private void mobile91Fetch(String url) throws Exception {
        String html = HtmlUtils.getUrlHtml(url);

        JSONObject object = JSONObject.parseObject(html);

        html = object.getString("response");

        html = html.substring(html.indexOf('<'));

        TagNode root = new HtmlCleaner().clean(html);

        List<TagNode> productListNode = getSubNodesByXPath(root, "//div[@class='filter filer_finder']");

        for (TagNode productNode : productListNode) {

            TagNode productUrlNode = getSubNodeByXPath(productNode, "//a[@target='_blank']", null);

            String productUrl = productUrlNode.getAttributeByName("href");

            productUrl = WEBSITE_91MOBILE_URL_PREFIEX + productUrl;

            TagNode sourceIdNode = getSubNodeByXPath(productNode, "//span[@title='Add to favourites']", null);
            String sourceId = sourceIdNode.getAttributeByName("data-product-id");

            System.out.println(productUrl + "___" + sourceId);

            PtmProduct ptmProduct = new PtmProduct();

            List<PtmCmpSku> ptmCmpSkuList = new ArrayList<>();

            MobileCateDescription mobileCateDescription = new MobileCateDescription();

            long t1 = System.currentTimeMillis();
            System.out.println("start fetch process product " + System.currentTimeMillis());

            try {
                fetchProductAndSkuList(ptmProduct, ptmCmpSkuList, mobileCateDescription, productUrl, sourceId);
            } catch (Exception e) {
                long t2 = System.currentTimeMillis();
                System.out.println("fetch product error cast" + (t2 - t1) / 1000);
                continue;
            }

            long t3 = System.currentTimeMillis();
            System.out.println("fetch product finish cast " + (t3 - t1) / 1000);

            if (StringUtils.isEmpty(ptmProduct.getTitle())) {
                continue;
            }
            if (ptmCmpSkuList.size() == 0) {
                continue;
            }

            PtmProduct ptmproduct = productService.createPtmproduct(ptmProduct);
            System.out.println("product create success " + ptmproduct.getId());
            System.out.println(ptmproduct);


            for (PtmCmpSku ptmCmpSku : ptmCmpSkuList) {
                ptmCmpSku.setProductId(ptmproduct.getId());
                PtmCmpSku ptmcmpsku = productService.createPtmcmpsku(ptmCmpSku);
                System.out.println("sku create success " + ptmcmpsku.getId());
                System.out.println(ptmcmpsku);
            }

            mobileCateDescription.setId(ptmProduct.getId());
            mdm.save(mobileCateDescription);
            System.out.println("prodcut description create success " + mobileCateDescription.getId());
            System.out.println("prodcut description " + mobileCateDescription);

            productService.importProduct2Solr2(ptmProduct);
            System.out.println("product import success " + ptmproduct.getId());
        }
    }

    private void Cate91Fetch(String url, JSONObject jsonObject) throws Exception {
        String jsonString = Httphelper.doPost(url, jsonObject.toJSONString());
        //get Products
        if (!StringUtils.isEmpty(jsonString)) {
            JSONObject object = JSONObject.parseObject(jsonString);
            if (object != null) {
                JSONArray products = object.getJSONArray("products");
                Iterator<Object> iterator = products.iterator();
                while (iterator.hasNext()) {
                    JSONObject product = (JSONObject) iterator.next();
                    String productUrl = product.getString("productUrl");
                    if (!StringUtils.isEmpty(productUrl)) {
                        productUrl = "http://www.91mobiles.com/" + productUrl;
                        System.out.println(productUrl);
                    }
                }
            }
        } else {
            return;
        }

    }

    private void Cate91FetchHtml(String url) throws Exception {
        String html = HtmlUtils.getUrlHtml(url);
        TagNode root = new HtmlCleaner().clean(html);

        List<TagNode> productListNode = getSubNodesByXPath(root, "//div[@class='filter filer_finder']");

        for (TagNode productNode : productListNode) {

            TagNode productUrlNode = getSubNodeByXPath(productNode, "//a[@target='_blank']", null);

            String productUrl = productUrlNode.getAttributeByName("href");
            if (productUrl != null) {
                productUrl = WEBSITE_91MOBILE_URL_PREFIEX + productUrl;
            }
            System.out.println(productUrl);
        }
    }

    private void fetchProductAndSkuList(PtmProduct ptmProduct, List<PtmCmpSku> ptmCmpSkuList, MobileCateDescription mobileCateDescription, String productUrl, String sourceId) throws Exception {

        TagNode productPageRootTagNode = HtmlUtils.getUrlRootTagNode(productUrl);

        //查询sourceId是否有重复，发现就continue
        PtmProduct product = dbm.querySingle("SELECT t FROM PtmProduct t WHERE t.sourceSite = 'MOBILE91' AND t.sourceId = ?0 ", Arrays.asList(sourceId));
        if (product != null) {
            System.out.println("ptmproduct has existed " + product.getId() + " sourceId = " + sourceId);
            System.out.println("new url " + productUrl);
            return;
        }

        TagNode productTitleNode = getSubNodeByXPath(productPageRootTagNode, "//h1", null);

        //主商品title
        String productTitle = StringUtils.filterAndTrim(productTitleNode.getText().toString(), null);
        //主商品图片
        TagNode productImageNode = getSubNodeByXPath(productPageRootTagNode, "//img[@id='mainImage']", null);

        String imageUrl = "";
        if (productImageNode != null) {
            imageUrl = productImageNode.getAttributeByName("data-zoom-image");
        } else {
            productImageNode = getSubNodeByXPath(productPageRootTagNode, "//div[@class='zoomWindowContainer']/div", null);
            if (productImageNode != null) {
                imageUrl = productImageNode.getAttributeByName("style");
                String[] subStr = imageUrl.split("background-image");
                imageUrl = subStr[1].substring(subStr[1].indexOf('(', subStr[1].indexOf(')'))).replace("\"", "");
            }
        }


        List<TagNode> skuNodeList = getSubNodesByXPath(productPageRootTagNode, "//ul[@id='found_store_list']/li[@data-stores='yes']");
        if (skuNodeList == null || skuNodeList.size() == 0) {
            skuNodeList = getSubNodesByXPath(productPageRootTagNode, "//div[@id='onlineStoresListRowDivId']/div");
        }

        Map<String, String> specMap = getSpecMap();

        List<TagNode> specSectionNodeList = getSubNodesByXPath(productPageRootTagNode, "//div[@class='specs_table_wrap']/div/table");

        if (specSectionNodeList == null || specSectionNodeList.size() == 0) {
            specSectionNodeList = getSubNodesByXPath(productPageRootTagNode, "//div[@class='spec_box']/table[@class='spec_table']");
        }

        for (TagNode specSctionNode : specSectionNodeList) {

            List<TagNode> specInfoNode = getSubNodesByXPath(specSctionNode, "/tbody/tr");

            if (specInfoNode != null && getSubNodesByXPath(specInfoNode.get(0), "//table[@class='border specs_table_sub']").size() > 0) {//判断是否还有分块节点

                for (TagNode subSpecNode : specInfoNode) {

                    List<TagNode> subSpecInfoNodeList = getSubNodesByXPath(subSpecNode, "//table[@class='border specs_table_sub']/tbody/tr");

                    for (TagNode subSpecInfoNode : subSpecInfoNodeList) {

                        TagNode subTitleNode = getSubNodeByXPath(subSpecNode, "//div[@class='sub_head']/p/span", null);
                        String subTitle = subTitleNode.getText().toString();

                        fetchSpecInfo(subSpecInfoNode, specMap, subTitle.toUpperCase() + " ");//转换成大写
                    }
                }
            } else {
                String subTitle = "";
                for (TagNode specNode : specInfoNode) {
                    TagNode subTitleNode = getSubNodeByXPath(specNode, "//span[@class='specs_head']", null);
                    if (subTitleNode != null) {
                        subTitle = subTitleNode.getText().toString();
                    }
                    fetchSpecInfo(specNode, specMap, subTitle);
                }
            }
        }


        float minPrice = 0.0f;
        for (TagNode skuNode : skuNodeList) {

            String websiteString = skuNode.getAttributeByName("data-relevance");
            if (StringUtils.isEmpty(websiteString)) {
                websiteString = skuNode.getAttributeByName("data-store");
            }

            String[] subStr = websiteString.split("\\.");

            if (subStr != null && subStr.length == 2) {
                websiteString = subStr[0].toUpperCase();
            }

            try {

                //sku网站名称
                Website website = Website.valueOf(websiteString);

                //sku标题
                TagNode skuTitleNode = getSubNodeByXPath(skuNode, "//p[@class='heading instock div_delivery']", null);
                if (skuTitleNode == null) {
                    skuTitleNode = getSubNodeByXPath(skuNode, "//div[@class='prclst_strD']/span[@class='p_ttle']", null);
                }
                String skuTitle = skuTitleNode.getText().toString();
                if (StringUtils.isEmpty(skuTitle)) {
                    skuTitle = productTitle;
                }

                //sku价格
                float price = 0.0f;
                String priceString = "";
                TagNode skuPriceNode = getSubNodeByXPath(skuNode, "//span[@class='price price_price_color']", null);
                if (skuPriceNode == null) {
                    priceString = skuNode.getAttributeByName("data-price");
                } else {
                    priceString = StringUtils.filterAndTrim(skuPriceNode.getText().toString(), Arrays.asList("Rs.", ","));
                }
                if (NumberUtils.isNumber(priceString)) {
                    price = Float.parseFloat(priceString);
                }

                //sku评分
                int rating = 0;
                TagNode skuStarNode = getSubNodeByXPath(skuNode, "//div[@class='rating prclst']", null);
                if (skuStarNode == null) {
                    skuStarNode = getSubNodeByXPath(skuNode, "/div[@class='prclst_strN']/div[@class='prclst-stars']/div", null);
                }
                String skuStarString = skuStarNode.getAttributeByName("style");
                skuStarString = skuStarString.substring(skuStarString.indexOf(':') + 1, skuStarString.indexOf('%'));
                if (NumberUtils.isNumber(skuStarString)) {
                    rating = Integer.parseInt(skuStarString) / 20;
                }

                //获取sku详细的url
                String oriUrl = "";
                String url = "";
                TagNode skuUrlNode = getSubNodeByXPath(skuNode, "/div[@class='merchant_list']/span", null);
                if (skuUrlNode == null) {
                    skuUrlNode = getSubNodeByXPath(skuNode, "/div[@class='prslstStrgoto']/span", null);
                }
                String beforeRedirectUrl = skuUrlNode.getAttributeByName("data-href-url");
                TagNode redirectRootNode = HtmlUtils.getUrlRootTagNode(beforeRedirectUrl);
                skuUrlNode = getSubNodeByXPath(redirectRootNode, "//meta[@http-equiv='refresh']", null);
                String urlInfo = skuUrlNode.getAttributeByName("content");

                /*
http://affiliateshopclues.com/?a=33&c=69&p=r&s1=&ckmrdr=http://www.shopclues.com/redmi-note-3-32gb-3gb-ram-2.html?utm_source=91mobiles&utm_medium=CPS&s2=||1477299181|27065|553|detail-vary2|
http://www.amazon.in/Xiaomi-Redmi-Note-Gold-32GB/dp/B01C2T6IDY/?tag=exclusivemob-21&ascsubtag=||1477299234|27065|553|detail-vary2|
https://www.snapdeal.com/product/redmi-note3-32gb/654134757432?utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_id=1306&aff_sub=!!1477299306!27065!553!detail-vary2!&affRedirect=true&viewinapp=true&msite-fallback=true
http://www.infibeam.com/Mobiles/xiaomi-redmi-note-3/P-mobi-6249051535-cat-z.html?trackId=buy_street&subTrackId=detail-vary2#variantId=P-mobi-50513042892
https://dl.flipkart.com/dl/moto-e3-power-black-16-gb/p/itmekgt2fbywqgcv?pid=MOBEKGT2HGDGADFW&affid=sales91mob&affExtParam1=DP&affExtParam2=!!1477299379!28983!553!detail!
http://www.s2d6.com/x/?x=c&z=s&v=5953892&k=||1477299419|28983|553|detail|&t=http://rover.ebay.com/rover/1/4686-127726-2357-6/2?&site=Partnership_CONMT&mpre=http%3A%2F%2Fwww.ebay.in%2Fitm%2FMOTO-E3-POWER-16GB-2GB-RAM-8MP-2MP-4G-LTE-3500-MAH-PHONE-BLACK-%2F252594992971%3Fhash=item3acfd5b74b%3Ag%3AQ4oAAOSwal5YCJBG%26aff_source=dgm
                 */
                String[] subStr1 = urlInfo.split("URL=");
                if (subStr1 != null && subStr1.length == 2) {
                    oriUrl = subStr1[1];
                    if (Website.SHOPCLUES.equals(website)) {
                        String[] subStr3 = oriUrl.split("ckmrdr=");
                        if (subStr3 != null && subStr3.length == 2) {
                            url = subStr3[1];
                            subStr3 = url.split("html\\?");
                            if (subStr3 != null && subStr3.length == 2) {
                                url = subStr3[0] + "html";
                            }
                        }
                    } else if (Website.AMAZON.equals(website)) {
                        String[] subStr3 = oriUrl.split("\\?");
                        if (subStr3 != null && subStr3.length == 2) {
                            url = subStr3[0];
                        }
                    } else if (Website.SNAPDEAL.equals(website) || Website.INFIBEAM.equals(website)) {
                        String[] subStr3 = oriUrl.split("\\?");
                        if (subStr3 != null && subStr3.length >= 2) {
                            url = subStr3[0];
                        }
                    } else if (Website.FLIPKART.equals(website)) {
                        String[] subStr3 = oriUrl.split("&affid");
                        if (subStr3 != null && subStr3.length >= 2) {
                            url = subStr3[0];
                            url = url.replace("dl.flipkart.com/dl", "www.flipkart.com");
                        }
                    } else if (Website.EBAY.equals(website)) {
                        String[] subStr3 = oriUrl.split("mpre=");
                        if (subStr3 != null && subStr3.length >= 2) {
                            url = subStr3[1];
                            url = URLDecoder.decode(url);
                            url = url.substring(0, url.indexOf('&'));
                        }
                    }
                }

                //设置ptmcmpsku信息
                PtmCmpSku ptmCmpSku = new PtmCmpSku();
                if (StringUtils.isEmpty(url) || StringUtils.isEmpty(oriUrl)) {
                    System.out.println("url or oriUrl is empty");
                    continue;
                }

                ptmCmpSku.setWebsite(website);
                ptmCmpSku.setTitle(skuTitle);
                ptmCmpSku.setPrice(price);
                if (minPrice == 0.0 || minPrice > price) {
                    minPrice = price;
                }
                ptmCmpSku.setRatings(rating);
                ptmCmpSku.setCategoryId(5L);
                ptmCmpSku.setOriImageUrl(imageUrl);
                ptmCmpSku.setCreateTime(TimeUtils.nowDate());
                ptmCmpSku.setOriUrl(oriUrl);
                ptmCmpSku.setUrl(url);

                ptmCmpSkuList.add(ptmCmpSku);

            } catch (Exception e) {

                if (e instanceof IllegalArgumentException) {
                } else {
                    e.printStackTrace();
                }
                continue;
            }

        }

        //设置主商品信息
        ptmProduct.setTitle(productTitle);
        ptmProduct.setSourceUrl(productUrl);
        ptmProduct.setSourceSite(Website.MOBILE91.name());
        ptmProduct.setSourceId(sourceId);
        ptmProduct.setCategoryId(5);
        ptmProduct.setPrice(minPrice);

        //商品描述信息
        Map<String, String> newSpecMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : specMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            key = key.replaceAll(" ", "_").replace("(s)", "").toLowerCase();

            newSpecMap.put(key, value);
        }

        BeanUtils.populate(mobileCateDescription, newSpecMap);
    }

    //此处要求，不是区域内部继续分块，subTitle传空值
    private void fetchSpecInfo(TagNode specNode, Map<String, String> specMap, String subTitle) throws ContentParseException {

        TagNode specKeyNode = getSubNodeByXPath(specNode, "//th[@class='scnd']", null);
        if (specKeyNode == null) {
            specKeyNode = getSubNodeByXPath(specNode, "//td[@class='spec_ttle']", null);
        }
        TagNode specValueNode = getSubNodeByXPath(specNode, "//td[@class='frth']", null);
        if (specKeyNode == null) {
            specKeyNode = getSubNodeByXPath(specNode, "//td[@class='spec_des']", null);
        }

        String key = specKeyNode.getText().toString();
        String value = StringUtils.filterAndTrim(specValueNode.getText().toString(), null);

        //判断value的值有无对勾
        TagNode specValueChildNode = getSubNodeByXPath(specValueNode, "/span", null);
        if (specValueChildNode != null) {
            String classString = specValueChildNode.getAttributeByName("class");
            if (StringUtils.isEqual(classString, "Stylus_check")) {
                if (StringUtils.isEmpty(value)) {
                    value = "yes";
                } else {
                    value = "yes," + value;
                }
            }
//            else if(StringUtils.isEqual(classString, "no")){//no还是注释掉，页面上一般no符号后面还是no
//                value = "no,"+value;
//            }
        }

//        List<TagNode> valueChildNode = specValueNode.getChildTagList();
//        if (valueChildNode.size() > 0) {
//            value = "";//暂时没有想到解决办法，所以先置为空串
//        }

        if (specMap.containsKey(subTitle + key)) {
            if (StringUtils.isEmpty(subTitle)) {
                specMap.put(key, value);
            } else {
                specMap.put(subTitle + key, value);
            }
        }
    }


    public Map<String, String> getSpecMap() {

        Map<String, String> map = new LinkedHashMap<>();


        //General
        map.put("Launch Date", "");
        map.put("Brand", "");
        map.put("Model", "");
        map.put("Operating System", "");
        map.put("Custom UI", "");
        map.put("SIM Slot(s)", "");

        //Design
        map.put("Dimensions", "");
        map.put("Weight", "");
        map.put("Build Material", "");

        //Display
        map.put("Screen Size", "");
        map.put("Screen Resolution", "");
        map.put("Pixel Density", "");

        //Performance
        map.put("Chipset", "");
        map.put("Processor", "");
        map.put("Architecture", "");
        map.put("Graphics", "");
        map.put("RAM", "");

        //Storage
        map.put("Internal Memory", "");
        map.put("Expandable Memory", "");
        map.put("USB OTG Support", "");

        //Camera
        map.put("MAIN CAMERA Resolution", "");
        map.put("MAIN CAMERA Sensor", "");
        map.put("MAIN CAMERA Autofocus", "");
        map.put("MAIN CAMERA Aperture", "");
        map.put("MAIN CAMERA Optical Image Stabilisation", "");
        map.put("MAIN CAMERA Flash", "");
        map.put("MAIN CAMERA Image Resolution", "");
        map.put("MAIN CAMERA Camera Features", "");
        map.put("MAIN CAMERA Video Recording", "");
        map.put("FRONT CAMERA Resolution", "");
        map.put("FRONT CAMERA Sensor", "");
        map.put("FRONT CAMERA Autofocus", "");

        //Battery
        map.put("Capacity", "");
        map.put("Type", "");
        map.put("User Replaceable", "");
        map.put("Quick Charging", "");

        //Network&Connectivity
        map.put("SIM Size", "");
        map.put("Network Support", "");
        map.put("VoLTE", "");
        map.put("SIM 1", "");
        map.put("SIM 2", "");
        map.put("Bluetooth", "");
        map.put("GPS", "");
        map.put("NFC", "");
        map.put("USB Connectivity", "");

        //Multimedia
        map.put("FM Radio", "");
        map.put("Loudspeaker", "");
        map.put("Audio Jack", "");

        //Special Features
        map.put("Fingerprint Sensor", "");
        map.put("Fingerprint Sensor Position", "");
        map.put("Other Sensors", "");

        return map;
    }

    @RequestMapping(value = "/deletesmallsitesku", method = RequestMethod.GET)
    @ResponseBody
    public String deletesmallsitesku() {

        final String Q_SKU = "select t from PtmCmpSku t where t.website=?0";
        final Set<Long> proIdSet = new HashSet<>();

        for (Website website : websites) {
            System.out.println(String.format("Delete [%s] skus....", website.name()));
            List<PtmCmpSku> cmpSkus = dbm.query(Q_SKU, Arrays.asList(website));
            int count = 0, size = cmpSkus.size();
            for (PtmCmpSku cmpSku : cmpSkus) {
                proIdSet.add(cmpSku.getProductId());
                // delete cmpsku
                cmpSkuService.deleteCmpSku(cmpSku.getId());
                count++;
                if (count % 10 == 0) {
                    System.out.println(String.format("Delete [%s] skus....[%d/%d]", website.name(), count, size));
                }
            }
        }

        for (Long proId : proIdSet) {
            PtmProduct pro = dbm.get(PtmProduct.class, proId);
            if (pro != null) {
                productService.importProduct2Solr2(pro);
            } else {
                productService.deleteProduct(proId);
            }
        }

        return "ok";
    }

    /**
     * 该方法用于将现有sku中（Date：2016-08-08）,flipkart的被访问的sku，找到其对应的类目
     */
    //flipkart/getflipkartskucate2
    @RequestMapping(value = "/getFlipkartSkuCate2", method = RequestMethod.GET)
    @ResponseBody
    public String getFlipkartSkuCate2() {

        //俩种添加策略
        //1.按照访问向队列添加
        //2.按照id升序向队列添加
        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.categoryid = 0";
//        String queryString = ;

        ListProcessWorkerStatus ws = new ListProcessWorkerStatus();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new FlipkartSkuCategory2GetListWorker(queryString, ws, dbm));

        for (int i = 0; i < 10; i++) {
            es.execute(new FlipkartSkuCategory2GetSaveWorker(dbm, ws));
        }

        return "ok";
    }

    @RequestMapping(value = "/fixproductcmps/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public String fixproductcmps1(@PathVariable long productId) {
        fixProductCmps(productId);
        return "ok";
    }

    @RequestMapping(value = "/fixmultiskus", method = RequestMethod.GET)
    @ResponseBody
    public String fixmultiskus(@RequestParam String filename) {
        File file = new File("/home/hasoffer/tmp/" + filename);
        List<String> lines;
        try {
            lines = FileUtils.readLines(file);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
        int count = 0;
        for (String line : lines) {

            String[] vals = line.split("\t");
            long skuCount = Long.valueOf(vals[0].trim());
            long productId = Long.valueOf(vals[1].trim());

            System.out.println(productId + "\t" + skuCount);

            fixProductCmps(productId);

            count++;
            if (count % 100 == 0) {
                System.out.println(count + "..products processed.");
//                break;
            }
        }

        return "ok";
    }

    private void fixProductCmps(long productId) {
        PtmProduct product = productService.getProduct(productId);
        if (product != null) {
            System.out.println("---------------- " + productId + " ----------------");
            System.out.println(product.getTitle());
            Set<String> skuUrlSet = new HashSet<>();

            List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(productId);
            for (PtmCmpSku cmpSku : cmpSkus) {
                if (!StringUtils.isEmpty(product.getTitle())) {
                    System.out.println(cmpSku.getTitle());
                    float score = ProductAnalysisService.stringMatch(product.getTitle(), cmpSku.getTitle());
                    if (score < 0.4) {
                        logger.debug(String.format("[Delete_%d]Score is [%f].", cmpSku.getId(), score));
                        cmpSkuService.deleteCmpSku(cmpSku.getId());
                        continue;
                    }
                }

                boolean exists = skuUrlSet.contains(cmpSku.getUrl());

                if (exists) {
                    logger.debug(String.format("[Delete_%d] Exist.", cmpSku.getId()));
                    cmpSkuService.deleteCmpSku(cmpSku.getId());
                } else {
                    skuUrlSet.add(cmpSku.getUrl());
                }

            }

            System.out.println("---------------------end-----------------------");
        }
    }

    //fixdata/updateptmproduct/{id}
    @RequestMapping(value = "/updateptmproduct/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String updateptmproduct(@PathVariable long id) {

        //更新商品价格
        productService.updatePtmProductPrice(id);
        //清除product缓存
        cacheServiceImpl.del("PRODUCT_" + id);
        //清除sku缓存        PRODUCT__listPagedCmpSkus_3198_1_10
        Set<String> keys = cacheServiceImpl.keys("PRODUCT__listPagedCmpSkus_" + id + "_*");

        for (String key : keys) {
            cacheServiceImpl.del(key);
        }

        return "ok";
    }

    @RequestMapping(value = "/setprostdbyml", method = RequestMethod.GET)
    public
    @ResponseBody
    String setprostdbyml() {


        System.out.println("all finished.");

        return "ok";
    }

    @RequestMapping(value = "/initproductifstd", method = RequestMethod.GET)
    public
    @ResponseBody
    String initproductifstd() {

        List<PtmCategory> stdCates = getStdCategories();

        int page = 1, size = 2000;

        int len = stdCates.size();
        for (int i = 0; i < len; i++) {

            PtmCategory cate = stdCates.get(i);

            System.out.println(String.format("set cate[%d] to std product", cate.getId()));

            List<PtmProduct> products = productService.listProducts(cate.getId(), 1, Integer.MAX_VALUE);

            for (PtmProduct o : products) {
                productService.updateProductStd(o.getId(), true);
            }
        }

        System.out.println("all finished.");

        return "ok";
    }

    private List<PtmCategory> getStdCategories() {

        Long[] cateIds = new Long[]{1L, 257L, 4662L, 1504L, 2334L};

        List<PtmCategory> cates = new ArrayList<>();

        for (Long cateId : cateIds) {
            PtmCategory category = categoryservice.getCategory(cateId);
            getStdCategories(cates, category);
        }

        return cates;
    }

    private void getStdCategories(List<PtmCategory> cates, PtmCategory category) {
        if (category == null) {
            return;
        }
        cates.add(category);
        if (category.getLevel() < 3) {
            List<PtmCategory> cates2 = categoryservice.listSubCategories(category.getId());
            for (PtmCategory cate : cates2) {
                getStdCategories(cates, cate);
            }
        }
    }

    //fixdata/deleteproduct/
    @RequestMapping(value = "/deleteproduct/{proId}", method = RequestMethod.GET)
    public
    @ResponseBody
    String deleteproduct(@PathVariable Long proId) {
        if (proId > 0) {
            PtmProduct product = dbm.get(PtmProduct.class, proId);
            if (product == null) {
                System.out.println("product is null");
                productService.deleteProduct(proId);
            } else {
                System.out.println("product is not null");
                logger.info(product.toString());
            }
        }
        return "ok";
    }

    //fixdata/deleteproductanyway/
    @RequestMapping(value = "/deleteproductanyway/{proId}", method = RequestMethod.GET)
    public ModelAndView
    deleteproduct2(@PathVariable Long proId) {
        ModelAndView mav = new ModelAndView();

        SysAdmin admin = (SysAdmin) Context.currentContext().get(StaticContext.USER);

        if (admin == null || !admin.getUname().equals("chevy")) {
            mav.addObject("result", "error!");
            return mav;
        }

        if (proId > 0) {
            productService.deleteProduct(proId);
        }

        mav.addObject("result", "ok");
        return mav;
    }

    @RequestMapping(value = "/cleansearchlogs", method = RequestMethod.GET)
    public
    @ResponseBody
    String cleansearchlogs() {

        ListProcessTask<SrmSearchLog> listAndProcessTask2 = new ListProcessTask<SrmSearchLog>(
                new ILister<SrmSearchLog>() {
                    @Override
                    public PageableResult<SrmSearchLog> getData(int page) {
                        return searchService.listSearchLogs(page, 1000);
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcessor<SrmSearchLog>() {
                    @Override
                    public void process(SrmSearchLog o) {
                        long proId = o.getPtmProductId();
                        if (proId > 0) {
                            PtmProduct product = dbm.get(PtmProduct.class, proId);
                            if (product == null) {
                                productService.deleteProduct(proId);
                            }
                        }
                    }
                }
        );

        listAndProcessTask2.go();

        return "ok";
    }

    /**
     * find title Count queue
     *
     * @return
     */
    @RequestMapping(value = "/setsearchcounts", method = RequestMethod.GET)
    public
    @ResponseBody
    String setsearchcounts() {
        // 根据 srmproductsearchcount 表的数据更新 solr
        String sql = "SELECT DISTINCT(t.productId) FROM SrmProductSearchCount t";

        List<Long> ids = dbm.query(sql);

        for (Long id : ids) {
            PtmProduct product = productService.getProduct(id);

            if (product != null) {
                productService.importProduct2Solr2(product);
            }
        }
        return "ok";
    }

    /**
     * find title Count queue
     *
     * @return
     */
    @RequestMapping(value = "/findsametitleproducts", method = RequestMethod.GET)
    public
    @ResponseBody
    synchronized String findsametitleproducts() {

        if (titleCountQueue.size() > 0) {
            return "queue size : " + titleCountQueue.size();
        }

        List<Object[]> titleCountMaps = dbm.query(Q_TITLE_COUNT);

        for (Object[] m : titleCountMaps) {
            String title = (String) m[0];
            System.out.println(m[1] + "\t:\t" + title);

            titleCountQueue.add(new TitleCountVo(title, Integer.parseInt(m[1].toString())));
        }

        return "queue size : " + titleCountQueue.size();
    }

    /**
     * 修复title相同的product
     * /fixtask/mergesametitleproduct
     *
     * @return
     */
    @RequestMapping(value = "/mergesametitleproduct", method = RequestMethod.GET)
    public
    @ResponseBody
    String mergesametitleproduct(@RequestParam(defaultValue = "1") String counts) {

        int count = 1;

        TitleCountVo tcv = titleCountQueue.poll();

        while (tcv != null) {
            System.out.println(tcv.toString());

            mergeProducts(tcv.getTitle());

            if (NumberUtils.isNumber(counts)) {
                int countsInt = Integer.parseInt(counts);
                if (count >= countsInt) {
                    break;
                }
            }

            count++;
            tcv = titleCountQueue.poll();
        }

        System.out.println("finished.");

        return "ok";
    }

    private void mergeProducts(String title) {

        List<PtmProduct> products = dbm.query(Q_PRODUCT_BY_TITLE, Arrays.asList(title));

        if (!ArrayUtils.hasObjs(products) || products.size() <= 1) {
            return;
        }

        PtmProduct finalProduct = products.get(0);

        List<PtmCmpSku> cmpSkus = cmpSkuService.listCmpSkus(finalProduct.getId());

        Map<String, PtmCmpSku> cmpSkuMap = new HashMap<String, PtmCmpSku>();
        for (PtmCmpSku cmpSku : cmpSkus) {
            if (cmpSku.getWebsite() == null) {
                // todo 处理
                continue;
            }
            cmpSkuMap.put(cmpSku.getUrl(), cmpSku);
        }

        // 处理其他 products
        // cmpsku 合并
        int size = products.size();
        System.out.println(String.format("[%s] products would be merged into product[%d].", size, finalProduct.getId()));
        for (int i = 1; i < size; i++) {
            searchService.mergeProducts(finalProduct, cmpSkuMap, products.get(i));
        }
    }

    //fixdata/fixProductSkusRepeatUrl
    @RequestMapping(value = "/fixProductSkusRepeatUrl/{productId}", method = RequestMethod.GET)
    @ResponseBody
    public String fixProductSkusRepeatUrl(@PathVariable long productId) {

        System.out.println("productid = " + productId);
        cleanProductSkusByUrl(productId);

        return "ok";
    }

    private void cleanProductSkusByUrl(long productId) {

        Set<String> urlSet = new HashSet<>();

        List<PtmCmpSku> skuList = dbm.query(Q_SKU_PRODUCTID, Arrays.asList(productId));
        if (skuList == null) {
            System.out.println("skuList is null");
        } else {
            System.out.println("get skuList size =  " + skuList.size());
        }

        boolean flag = false;

        for (int i = 0; i < skuList.size(); i++) {

            PtmCmpSku ptmCmpSku = skuList.get(i);

            String url = ptmCmpSku.getUrl();
            System.out.println("sku [" + i + "] url is " + url);
            if (urlSet.contains(url)) {
                System.out.println("sku [" + i + "] url is already saved remove");
                cmpSkuService.deleteCmpSku(ptmCmpSku.getId());
                System.out.println("remove succes");
                flag = true;
            } else {
                urlSet.add(url);
                System.out.println("sku [" + i + "] url not in set add success");
            }
        }

        if (flag) {
            System.out.println("reimport product " + productId + " to solr start");
            productService.importProduct2Solr2(productId);
            System.out.println("reimport product " + productId + " to solr end");
        }

    }

    /**
     * 修复sorceSite为空的商品的图片
     *
     * @return
     */
    //fixdata/fixSourceUrlNullProductImage
    @RequestMapping(value = "/fixSourceUrlNullProductImage", method = RequestMethod.GET)
    @ResponseBody
    public String fixSourceUrlNullProductImage() {

        List<PtmProduct> productList = dbm.query("SELECT t FROM PtmProduct t WHERE t.sourceSite = ''");

        for (PtmProduct product : productList) {

            System.out.println("fix product " + product.getId());

            List<PtmImage> productImageList = dbm.query("SELECT t FROM PtmImage t WHERE t.productId = ?0 ", Arrays.asList(product.getId()));

            if (productImageList != null && productImageList.size() != 0) {
                System.out.println("product " + product.getId() + " already fix " + productImageList.size());
                continue;
            }

            List<PtmCmpSku> skuList = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ORDER BY t.id", Arrays.asList(product.getId()));
            if (skuList == null || skuList.size() == 0) {
                System.out.println("reday to fix " + product.getId() + " has " + skuList.size() + " sku contiue");
                continue;
            }

            System.out.println("reday to fix " + product.getId() + " has " + skuList.size() + " sku");
            for (PtmCmpSku sku : skuList) {

                Website website = sku.getWebsite();
                String url = sku.getUrl();
                System.out.println("website = " + website.name());
                String imageUrl = "";
                try {
                    imageUrl = fetchService.fetchWebsiteImageUrl(website, url);

                    if (StringUtils.isEmpty(imageUrl)) {
                        continue;
                    }

                    productService.updateProductImage2(product.getId(), imageUrl);
                    System.out.println("update success for " + product.getId());
                    break;
                } catch (Exception e) {
                    System.out.println("update fail for " + product.getId() + " use " + sku.getId());
                    continue;
                }
            }
        }
        return "ok";
    }

    @RequestMapping(value = "/fiximages/{site}", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixImage(@PathVariable final String site) {
        final String Q_PRODUCT_WEBSITE =
                "SELECT t FROM PtmProduct t WHERE t.sourceSite=?0";

        ListProcessTask<PtmProduct> listAndProcessTask2 = new ListProcessTask<PtmProduct>(
                new ILister<PtmProduct>() {
                    @Override
                    public PageableResult getData(int page) {
                        return dbm.queryPage(Q_PRODUCT_WEBSITE, page, 500, Arrays.asList(site));
                    }

                    @Override
                    public boolean isRunForever() {
                        return false;
                    }

                    @Override
                    public void setRunForever(boolean runForever) {

                    }
                },
                new IProcessor<PtmProduct>() {
                    @Override
                    public void process(PtmProduct o) {
                        try {
                            System.out.println(o.getId() + "\t [re load image] " + TimeUtils.now());
                            // update image for product
                            String sourceUrl = o.getSourceUrl();
                            // visit flipkart page to get image url
                            String oriImageUrl = "";

                            oriImageUrl = fetchService.fetchWebsiteImageUrl(Website.valueOf(site), sourceUrl);

//                            if (Website.FLIPKART.name().equals(site)) {
//                                oriImageUrl = fetchService.fetchFlipkartImageUrl(sourceUrl);
//                            } else if (Website.SNAPDEAL.name().equals(site)) {
//                                oriImageUrl = fetchService.fetchSnapdealImageUrl(sourceUrl);
//                            } else if (Website.EBAY.name().equals(site)) {
//                                oriImageUrl = fetchService.fetchEbayImageUrl(sourceUrl);
//                            }

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

    @RequestMapping(value = "/createskuindex", method = RequestMethod.GET)
    public
    @ResponseBody
    String createskuindex() {
        ListProcessTask<PtmCmpSku> listAndProcessTask2 = new ListProcessTask<PtmCmpSku>(new ILister() {
            @Override
            public PageableResult getData(int page) {
                return dbm.queryPage(Q_PTMCMPSKU, page, 2000);
            }

            @Override
            public boolean isRunForever() {
                return false;
            }

            @Override
            public void setRunForever(boolean runForever) {

            }
        }, new IProcessor<PtmCmpSku>() {
            @Override
            public void process(PtmCmpSku cmpSku) {
                try {
                    cmpskuIndexService.createOrUpdate(new CmpSkuModel(cmpSku));
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
            }
        });

        listAndProcessTask2.go();

        return "ok";
    }

    //fixdata/products
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    public
    @ResponseBody
    String fixdataproducts() {

        Date date = TimeUtils.stringToDate("2016-07-01 21:40:35", "yyyy-MM-dd HH:mm:ss");

        final int page = 1, size = 200;

        PageableResult<PtmProduct> pagedProducts = productService.listProductsByCreateTime(date, page, size);

        final long totalPage = pagedProducts.getTotalPage();

        List<PtmProduct> products = null;

        for (int i = 1; i <= totalPage; i++) {

            if (i > 1) {
                pagedProducts = productService.listProductsByCreateTime(date, page, size);
            }

            products = pagedProducts.getData();

            if (ArrayUtils.hasObjs(products)) {
                for (PtmProduct product : products) {
                    long count = searchService.statLogsCountByProduct(product.getId());

                    if (count == 0) {
                        productService.deleteProduct(product.getId());
                        System.out.println("delete product : " + product.getId() + " - " + TimeUtils.parse(date, "yyyy-MM-dd HH:mm:ss"));
                    }

                    date = product.getCreateTime();
                }
            }
        }

        return "ok";
    }

    @RequestMapping(value = "/hijacklogfix", method = RequestMethod.GET)
    public String hijacklogfix() {

        long minTime = 1462363145553L;
        boolean flag = true;


        Date startDate = TimeUtils.stringToDate("2016-04-29 00:00:00", "yyyy-MM-dd hh:mm:ss");

        while (flag) {

            Date endDate = TimeUtils.add(startDate, TimeUtils.MILLISECONDS_OF_1_MINUTE * 30);

            Query query = new Query();
            query.addCriteria(Criteria.where("createTime").gte(startDate).lt(endDate));

            List<UrmDeviceRequestLog> requestLogList = mdm.query(UrmDeviceRequestLog.class, query);

            if (requestLogList == null || requestLogList.size() == 0) {
                startDate = endDate;
                logger.debug("get 0 log and new startTime:" + startDate.toString());
                continue;
            }

            for (UrmDeviceRequestLog requestLog : requestLogList) {

                if (requestLog.getCreateTime().getTime() > minTime) {
                    logger.debug(requestLog.getId() + " stop");
                    flag = false;
                    break;
                }

                String query1 = requestLog.getQuery();
                if (!StringUtils.isEmpty(query1) && query1.contains("rediToAffiliateUrl")) {

                    Website website = requestLog.getCurShopApp();

                    HijackLog hijackLog = new HijackLog();
                    hijackLog.setId(requestLog.getId());
                    hijackLog.setCreateTime(requestLog.getCreateTime());
                    hijackLog.setWebsite(website);

                    mdm.save(hijackLog);
                    logger.debug(requestLog.getId() + " parse success");
                }
            }

            startDate = endDate;
            logger.debug("new startTime:" + startDate.toString());
        }

        return "ok";
    }

    @RequestMapping(value = "/fixpaytmoriurl", method = RequestMethod.GET)
    public String fixpaytmoriurl() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.oriUrl LIKE '%//catalog.paytm.com/v1/%'";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            String oriUrl = ptmCmpSku.getOriUrl();

            oriUrl = oriUrl.replace("//catalog.paytm.com/v1/", "//paytm.com/shop/");

            updater.getPo().setOriUrl(oriUrl);

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " oriUrl fix success");
        }

        return "ok";
    }


    @RequestMapping(value = "/fixpaytmurlnull", method = RequestMethod.GET)
    public String fixpaytmurlnull() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'PAYTM' AND t.url IS NULL ";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            updater.getPo().setUrl(PaytmHelper.getCleanUrl(ptmCmpSku.getOriUrl()));

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " update success");
        }

        return "ok";
    }


    @RequestMapping(value = "/fixpaytmurl", method = RequestMethod.GET)
    public String fixpaytmurl() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.url LIKE '%//catalog.paytm.com/v1/%'";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            String url = ptmCmpSku.getUrl();

            url = url.replace("//catalog.paytm.com/v1/", "//paytm.com/shop/");

            updater.getPo().setUrl(url);

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " url fix success");
        }

        return "ok";
    }

    //
    @RequestMapping(value = "/fixshopcluesurlnull", method = RequestMethod.GET)
    public String fixshopcluesurlnull() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'SHOPCLUES' AND t.url IS NULL AND t.oriUrl IS NOT NULL";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            updater.getPo().setUrl(ShopcluesHelper.getCleanUrl(ptmCmpSku.getOriUrl()));

            dbm.update(updater);

            logger.debug(ptmCmpSku.getId() + " update success");
        }

        return "ok";
    }

    //fixdata/fixfilpkarturlcontainnull
    @RequestMapping(value = "/fixfilpkarturlcontainnull", method = RequestMethod.GET)
    public String fixfilpkarturlcontainnull() {

        String queryString = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url like '%?pid=null%' ";

        String startUrl = "http://www.flipkart.com";

        List<PtmCmpSku> skus = dbm.query(queryString);

        for (PtmCmpSku ptmCmpSku : skus) {

            String url = ptmCmpSku.getUrl();

            try {

                HttpResponseModel response = HtmlUtils.getResponse(ptmCmpSku.getUrl(), 3);

                String redirect = response.getRedirect();

                url = startUrl + redirect;

            } catch (Exception e) {
                logger.debug("parse error for [" + url + "]" + e.toString());
            }

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(ptmCmpSku.getId());

            updater.getPo().setUrl(url);
            String sourceSid = FlipkartHelper.getSkuIdByUrl(url);
            updater.getPo().setSourceSid(sourceSid);

            dbm.update(updater);
            logger.debug(ptmCmpSku.getId() + " update success");
        }

        return "ok";
    }

    //fixdata/fixflipkarturlnull
    @RequestMapping(value = "/fixflipkarturlnull", method = RequestMethod.GET)
    @ResponseBody
    public String fixflipkarturlnull() {

        final String Q_FLIPKART_NULLURL = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.url IS NULL AND t.oriUrl IS NOT NULL";

        List<PtmCmpSku> skus = dbm.query(Q_FLIPKART_NULLURL);

        for (PtmCmpSku sku : skus) {

            String oriUrl = sku.getOriUrl();

            String url = FlipkartHelper.getCleanUrl(oriUrl);

            url = FlipkartHelper.getUrlByDeeplink(url);

            PtmCmpSkuUpdater updater = new PtmCmpSkuUpdater(sku.getId());

            updater.getPo().setUrl(url);

            dbm.update(updater);

            logger.debug("update url success for [" + sku.getId() + "] to [" + url + "]");
        }

        return "ok";
    }

    //fixdata/fixerrorskuinprice
    @RequestMapping(value = "/fixerrorskuinprice", method = RequestMethod.GET)
    @ResponseBody
    public String fixerrorskuinprice() {

        final ConcurrentLinkedQueue<Long> idQueue = new ConcurrentLinkedQueue<Long>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {
            @Override
            public void run() {

                long i = 1;

                while (true) {

                    if (idQueue.size() > 2000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        idQueue.add(i);
                        i++;
                    }
                }
            }
        });

        for (int i = 0; i < 10; i++) {
            es.execute(new FixSkuErrorInPriceWorker(idQueue, dbm));
        }

        return "ok";
    }

    //fixdata/fixSourceSidIndex
    @RequestMapping(value = "/fixSourceSidIndex", method = RequestMethod.GET)
    @ResponseBody
    public String fixSourceSidIndex() {

        int curPage = 1;
        int pageSize = 1000;

        PageableResult<PtmCmpSkuIndex2> pageableResult = dbm.queryPage(Q_INDEX, curPage, pageSize);

        long totalPage = pageableResult.getTotalPage();
        List<PtmCmpSkuIndex2> indexList = pageableResult.getData();

        while (curPage <= totalPage) {

            if (curPage > 1) {
                pageableResult = dbm.queryPage(Q_INDEX, curPage, pageSize);
                indexList = pageableResult.getData();
            }

            for (PtmCmpSkuIndex2 index : indexList) {

                PtmCmpSkuIndex2Updater updater = new PtmCmpSkuIndex2Updater(index.getId());

                Website website = index.getWebsite();
                String sourceSid = index.getSourceSid();

                String skuTitle = index.getSkuTitle();
                String newSiteSkutitleIndex = HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(skuTitle));
                String oriSiteSkutitleIndex = index.getSiteSkuTitleIndex();

                if (!StringUtils.isEqual(newSiteSkutitleIndex, oriSiteSkutitleIndex)) {
                    updater.getPo().setSiteSkuTitleIndex(newSiteSkutitleIndex);
                }

                updater.getPo().setSiteSourceSidIndex(HexDigestUtil.md5(website.name() + sourceSid));


                dbm.update(updater);

                logger.debug("update success for [" + index.getId() + "]");
            }

            logger.debug(curPage + " page success ; left " + (totalPage - curPage) + "page");
            curPage++;
        }

        return "ok";
    }

    //fixdata/fixproductprice
    @RequestMapping(value = "/fixproductprice")
    @ResponseBody
    public String fixproductprice() {

        final ConcurrentLinkedQueue<PtmProduct> productQueue = new ConcurrentLinkedQueue<PtmProduct>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {

            @Override
            public void run() {

                int curPage = 1;

                int pageSize = 1000;


                while (true) {

                    try {
                        PageableResult<PtmProduct> pageableResult = dbm.queryPage("SELECT t FROM PtmProduct t Where t.id > ?0 ORDER BY t.id ASC ", curPage, pageSize, Arrays.asList(1639554L));


                        if (curPage > 1) {
                            pageableResult = dbm.queryPage("SELECT t FROM PtmProduct t Where t.id > ?0 ORDER BY t.id ASC ", curPage, pageSize, Arrays.asList(1639554L));
                        }

                        if (productQueue.size() > 5000) {
                            try {
                                TimeUnit.SECONDS.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        List<PtmProduct> productList = pageableResult.getData();

                        for (PtmProduct ptmProduct : productList) {

                            long skuNumber = dbm.querySingle("SELECT COUNT(*) FROM PtmCmpSku t WHERE t.productId = ?0 AND t.status = 'ONSALE' ", Arrays.asList(ptmProduct.getId()));

                            if (skuNumber > 0) {
                                productQueue.add(ptmProduct);
                            }

                        }

                    } catch (Exception e) {
                        continue;
                    }

                    curPage++;
                }
            }
        });


        for (int i = 0; i < 5; i++) {

            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        PtmProduct ptmProduct = productQueue.poll();

                        if (ptmProduct == null) {
                            continue;
                        }

                        try {
                            productService.updatePtmProductPrice(ptmProduct.getId());
                        } catch (Exception e) {
                            productQueue.add(ptmProduct);
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                }
            });

        }

        return "ok";
    }

    //fixdata/fixsmallimagepathnull
    @RequestMapping("/fixsmallimagepathnull")
    @ResponseBody
    public String fixsmallimagepathnull() {


        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        int processCount = 20;

        es.execute(new Runnable() {

            String Q_SKU_IMAGE = "SELECT t FROM PtmCmpSku t WHERE t.smallImagePath is null and t.status <> 'OFFSALE' and t.price > 0 and t.oriImageUrl IS NOT NULL and t.oriImageUrl <> '' ORDER BY t.id";

            int page = 1, PAGE_SIZE = 1000;

            @Override
            public void run() {

                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);

                long totalPage = pageableResult.getTotalPage();
                System.out.println("totalpage =" + totalPage);

                while (page < totalPage) {

                    if (cmpSkuQueue.size() > 10000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {

                        }
                    }

                    if (page > 1) {
                        pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);
                    }

                    List<PtmCmpSku> ptmCmpSkuList = pageableResult.getData();

                    for (PtmCmpSku ptmCmpSku : ptmCmpSkuList) {

                        if (WebsiteHelper.DEFAULT_WEBSITES.contains(ptmCmpSku.getWebsite())) {

                            cmpSkuQueue.add(ptmCmpSku);
                        }
                    }

                    System.out.println("queue size =" + cmpSkuQueue.size());
                    System.out.println("currentPage =" + page);
//                    break;//for test
                    page++;
                }
            }
        });

        for (int i = 0; i < processCount; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        PtmCmpSku t = cmpSkuQueue.poll();

                        if (t == null) {
//                            System.out.println("poll get null sleep 15 seconds");
                            try {
                                TimeUnit.SECONDS.sleep(15);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        String oriImageUrl = t.getOriImageUrl();
                        System.out.println("ready to download " + t.getId());

                        try {
                            ImagePath imagePath = hasoffer.core.utils.ImageUtil.downloadAndUpload2(oriImageUrl);

                            cmpSkuService.fixSmallImagePath(t.getId(), imagePath.getSmallPath());

                            System.out.println("fix success for " + t.getId());
                        } catch (ImageDownloadOrUploadException e) {
                            System.out.println("down image error for " + t.getId());
                        }


                    }
                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                break;
            }
        }

        return "ok";
    }

    //fixdata/fixflipkartcategoryidnull
    @RequestMapping(value = "/fixflipkartcategoryidnull")
    @ResponseBody
    public String fixflipkartcategoryidnull() {

        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {

            String Q_SKU_IMAGE = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' and t.categoryId IS NULL ORDER BY t.id";

            int page = 1, PAGE_SIZE = 1000;

            @Override
            public void run() {

                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);

                long totalPage = pageableResult.getTotalPage();
                System.out.println("totalpage =" + totalPage);

                while (page < totalPage) {

                    if (cmpSkuQueue.size() > 10000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {

                        }
                    }

                    if (page > 1) {
                        pageableResult = dbm.queryPage(Q_SKU_IMAGE, page, PAGE_SIZE);
                    }

                    List<PtmCmpSku> ptmCmpSkuList = pageableResult.getData();

                    cmpSkuQueue.addAll(ptmCmpSkuList);

                    System.out.println("queue size =" + cmpSkuQueue.size());
                    System.out.println("currentPage =" + page);
//                    break;//for test
                    page++;
                }
            }
        });

        for (int i = 0; i < 10; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {

                    while (true) {
                        PtmCmpSku ptmcmpsku = cmpSkuQueue.poll();

                        if (ptmcmpsku == null) {
//                            System.out.println("poll get null sleep 15 seconds");
                            try {
                                TimeUnit.SECONDS.sleep(15);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            continue;
                        }

                        System.out.println("ready to parse " + ptmcmpsku.getId());

                        String skuid = FlipkartHelper.getSkuIdByUrl(ptmcmpsku.getUrl());

                        String url = "https://www.flipkart.com/api/3/page/dynamic/product";

                        String json = "{\"requestContext\":{\"productId\":\"" + skuid + "\"}}";

                        Map<String, String> header = new HashMap<>();

                        header.put("x-user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 FKUA/website/41/website/Desktop");

                        try {

                            String response = Httphelper.doPostJsonWithHeader(url, json, header);

                            JSONObject responseJson = JSON.parseObject(response);

                            JSONArray jsonArray = responseJson.getJSONObject("response").getJSONObject("product_breadcrumb").getJSONObject("data").getJSONObject("0").getJSONObject("value").getJSONArray("productBreadcrumbs");

                            String catepath = "";

                            for (int i = 0; i < jsonArray.size(); i++) {
                                if (i > 2) {
                                    break;
                                }

                                catepath = jsonArray.getJSONObject(i).getString("title");
                            }

                            if (StringUtils.isEmpty(catepath)) {

                                PtmCategory3 category3 = dbm.querySingle("SELECT t FROM PtmCategory3 t WHERE t.name = ?0", Arrays.asList(catepath));


                            }

                        } catch (Exception e) {
                            System.out.println("parse exception for " + ptmcmpsku.getId());
                        }
                    }
                }
            });
        }

        while (true) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                break;
            }
        }

        return "ok";
    }

    //fixdata/fixSkuSmallImagePathSizeZero
    @RequestMapping(value = "/fixSkuSmallImagePathSizeZero")
    @ResponseBody
    public String fixSkuSmallImagePathSizeZero() {

        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(new Runnable() {

            @Override
            public void run() {

                String[] strArray = {"0802"};

                for (int i = 0; i < strArray.length; i++) {

                    String str = strArray[i];
                    System.out.println("cur str" + str);

                    int curPage = 1;
                    int pageSize = 1000;

                    PageableResult<PtmCmpSku> pageableResult = dbm.queryPage("SELECT t FROM PtmCmpSku t WHERE t.smallImagePath like '/2016/" + str + "/%' ", curPage, pageSize);

                    long totalPage = pageableResult.getTotalPage();

                    while (curPage <= totalPage) {

                        if (cmpSkuQueue.size() > 10000) {
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            System.out.println("queue size = " + cmpSkuQueue.size());
                            continue;
                        }

                        if (curPage > 1) {
                            pageableResult = dbm.queryPage("SELECT t FROM PtmCmpSku t WHERE t.smallImagePath like '/2016/" + str + "/%' ", curPage, pageSize);
                        }

                        List<PtmCmpSku> cmpSkuList = pageableResult.getData();

                        cmpSkuQueue.addAll(cmpSkuList);

                        curPage++;
                    }
                }
            }
        });

        for (int i = 0; i < 10; i++) {

            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        PtmCmpSku sku = cmpSkuQueue.poll();

                        try {

                            if (sku == null) {
                                try {
                                    TimeUnit.SECONDS.sleep(3);
                                } catch (InterruptedException e) {

                                }
                                continue;
                            }

                            cmpSkuService.downloadImage2(sku);
                        } catch (Exception e) {
                            System.out.println("error download for " + sku.getId());
                        }
                    }
                }
            });
        }

        return "ok";
    }

    //fixdata/fixSkuSmallImagePathSizeZeroTest
    @RequestMapping(value = "/fixSkuSmallImagePathSizeZeroTest")
    @ResponseBody
    public String fixSkuSmallImagePathSizeZeroTest() {

        PtmCmpSku sku = dbm.querySingle("SELECT t FROM PtmCmpSku t WHERE t.id = ?0 ", Arrays.asList(6428134L));

        cmpSkuService.downloadImage2(sku);

        return "ok";
    }


    //fixdata/fixFlipkartSkutitleNull
    @RequestMapping(value = "/fixFlipkartSkutitleNull")
    @ResponseBody
    public String fixFlipkartSkutitleNull() {

        ExecutorService es = Executors.newCachedThreadPool();

        final ConcurrentLinkedQueue<PtmCmpSku> cmpSkuQueue = new ConcurrentLinkedQueue<PtmCmpSku>();

        es.execute(new Runnable() {
            @Override
            public void run() {

                int curPage = 1;
                int pageSize = 1000;
                PageableResult<PtmCmpSku> pageableResult = dbm.queryPage("SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.skuTitle like '%null%'", curPage, pageSize);

                long totalPage = pageableResult.getTotalPage();
                System.out.println("total page " + totalPage);

                while (curPage <= totalPage) {

                    if (cmpSkuQueue.size() > 50000) {
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {

                        }
                        continue;
                    }

                    if (curPage > 1) {
                        pageableResult = dbm.queryPage("SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' AND t.skuTitle like '%null%'", curPage, pageSize);
                    }

                    List<PtmCmpSku> ptmCmpSkuList = pageableResult.getData();

                    for (PtmCmpSku ptmCmpSku : ptmCmpSkuList) {
                        cmpSkuQueue.add(ptmCmpSku);
                        System.out.println("add success " + ptmCmpSku.getId() + "\n skutitle = " + ptmCmpSku.getSkuTitle() + "\n url =" + ptmCmpSku.getUrl());
                    }

                    System.out.println("curPage = " + curPage);
                    curPage++;
                }
            }
        });

        for (int i = 0; i < 10; i++) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {

                        PtmCmpSku ptmcmpsku = cmpSkuQueue.poll();

                        if (ptmcmpsku == null) {
                            System.out.println("pull get null wait 5 seconds");
                            try {
                                TimeUnit.SECONDS.sleep(5);
                            } catch (InterruptedException e) {

                            }
                            continue;
                        }

                        String oldSkuTitle = ptmcmpsku.getSkuTitle();
                        String newSkuTitle = StringUtils.filterAndTrim(oldSkuTitle, Arrays.asList("null"));

                        cmpSkuService.fixFlipkartSkuTitleNull(ptmcmpsku.getId(), newSkuTitle);

                        System.out.println("fix success for " + ptmcmpsku.getId() + "\n" + oldSkuTitle + "---" + newSkuTitle);

                    }
                }
            });
        }

        return "ok";
    }

    //fixdata/fixProductSourceSiteNull
    @RequestMapping(value = "/fixProductSourceSiteNull")
    @ResponseBody
    public String fixProductSourceSiteNull() {


        List<PtmProduct> productList = dbm.query("SELECT t FROM PtmProduct t WHERE t.sourceSite = ''");

        for (PtmProduct ptmProduct : productList) {

            System.out.println("ready to fix product " + ptmProduct.getId());
            List<PtmCmpSku> ptmcmpskuList = dbm.query("SELECT t FROM PtmCmpSku t WHERE t.productId = ?0 ", Arrays.asList(ptmProduct.getId()));

            for (PtmCmpSku ptmCmpSku : ptmcmpskuList) {
                if (StringUtils.isEqual(ptmProduct.getTitle(), ptmCmpSku.getTitle())) {
                    productService.updatePtmProdcutWebsite(ptmProduct.getId(), ptmCmpSku.getWebsite());
                    System.out.println("fix success for " + ptmProduct.getId() + " to " + ptmCmpSku.getWebsite().name());
                }
            }
        }

        return "ok";
    }

    /**
     * @param id
     * @param response 查看一个sku的offer及其相关信息
     * @return
     */
    @RequestMapping("offerTest")
    public String getOffers(@RequestParam(defaultValue = "0") Long id, HttpServletResponse response) {
        System.out.println(" get get get get  offers offers offers ");
        PtmCmpSkuDescription ptmCmpSkuDescription = mdm.queryOne(PtmCmpSkuDescription.class, id);
        if (ptmCmpSkuDescription != null) {
            String offers = ptmCmpSkuDescription.getOffers();
            System.out.println(" got it ,and offers is " + offers);
            PtmCmpSku ptmCmpSku = cmpSkuService.getCmpSkuById(id);
            if (ptmCmpSku != null) {
                System.out.println("sku id is :" + id + " and productId is " + ptmCmpSku.getProductId());
                PtmProduct product = productService.getProduct(ptmCmpSku.getProductId());
                if (product != null) {
                    System.out.println(" product is exist  and title is  " + product.getTitle());
                    System.out.println(" price is :" + product.getPrice());
                }
            }
        }
        return null;
    }

    //fixdata/91mobile
    @RequestMapping(value = "/91mobile", method = RequestMethod.GET)
    public String nineOnemobileFetch() throws HttpFetchException {

        String url = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.5544784158021026&requestType=2&listType=list&selMobSort=views&amount=1000%3B45000&sCatName=phone&price_range_apply=0&tr_fl%5B%5D=mob_market_status_filter.marketstatus_filter%3Aava_stores&search=&hidFrmSubFlag=1&page=2&category=mobile&unique_sort=&hdnCategory=mobile&user_search=&url_feat_rule=";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);


        return null;
    }
}
