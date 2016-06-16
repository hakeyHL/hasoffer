package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.XPathUtils;
import hasoffer.data.redis.IRedisService;
import hasoffer.dubbo.api.fetch.common.StringConstant;
import hasoffer.dubbo.api.fetch.po.FetchResult;
import hasoffer.dubbo.api.fetch.service.IFetchService;
import hasoffer.dubbo.api.fetch.service.IKeywordService;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.helper.WebsiteProcessorFactory;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodesByXPath;

public class FlipkartFetchServiceImpl extends BaseFetchServiceImpl implements IFetchService{
    private Logger logger = LoggerFactory.getLogger(FlipkartFetchServiceImpl.class);

    @Resource
    private IRedisService<ListProduct> redisService;

    @Resource
    private IKeywordService keywordService;

    private static final String XPATH_TITLE = "//h1[@itemprop='name']";
    private static final String XPATH_SUBTITLE = "//span[@class='subtitle']";
    private static final String XPATH_PRICE1 = "//span[@class='selling-price omniture-field']";
    private static final String XPATH_IMAGE = "//div[@class='imgWrapper']/img[1]";
    private static final String XPATH_STATUS = "//div[@class='out-of-stock-status']";
    private static final String XPATH_STATUS_COMING = "//div[@class='coming-soon-status']";
    private static final String XPATH_SOURCEID = "//div[@id='reco-module-wrapper']";

    @Override
    public FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex) {
        FetchResult fetchResultList = getFetchResultList(webSite, keyword);
        if (fetchResultList == null) {
            FetchResult fetchResult = new FetchResult(webSite, keyword);
            fetchResult.setTaskStatus(TaskStatus.START);
            addNewFetchResult(fetchResult);
            return fetchResult;
        } else {
            return fetchResultList;
        }
    }

    private void addNewFetchResult(FetchResult fetchResult) {
        String key = getFetchResultKey(fetchResult.getWebsite(), fetchResult.getKeyword());
        keywordService.saveKeyword(key);
        String json = JSONUtil.toJSON(fetchResult);
        redisService.add(key, json, 1000);
    }

    private FetchResult getFetchResultList(Website webSite, String keyWord) {
        String json = redisService.get(getFetchResultKey(webSite, keyWord), 1000);
        if (json == null || "".equals(json)) {
            return null;
        }
        try {
            return JSONUtil.toObject(json, FetchResult.class);
        } catch (IOException e) {
            logger.debug("getProduct error.");
            return null;
        }
    }

    @Override
    public FetchResult getProductsByUrl(String webSite, String url) throws HttpFetchException, ContentParseException {
        FetchResult result = new FetchResult();
        ListProduct fetchedProduct = new ListProduct();

        if (url != null && url.contains("dl.flipkart.com/dl/")) {
            url = FlipkartHelper.getUrlByDeeplink(url);
        }

        String pageHtml = HtmlUtils.getUrlHtml(url);

        TagNode root = new HtmlCleaner().clean(pageHtml);


        String sourceId = FlipkartHelper.getProductIdByUrl(url);
        if (sourceId == null) {
            TagNode sourceIdNode = XPathUtils.getSubNodeByXPath(root, XPATH_SOURCEID, new ContentParseException("sourceId not found"));
            sourceId = sourceIdNode.getAttributeByName("data-pid");
            if (!url.contains("?pid=")) {
                url += "?pid=" + sourceId;
            }
        }

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();

        String subTitle = "";
        TagNode subTitleNode = getSubNodeByXPath(root, XPATH_SUBTITLE, null);
        if (subTitleNode != null) {
            subTitle = subTitleNode.getText().toString();
        }

        TagNode priceNode = null;
        float price = 0.0f;

        try {
            priceNode = getSubNodeByXPath(root, XPATH_PRICE1, new ContentParseException("price not found by XPATH_PRICE1"));
        } catch (ContentParseException e1) {
            List<TagNode> priceNodes = getSubNodesByXPath(root, XPATH_PRICE1, new ContentParseException("price not found by XPATH_PRICE1"));
            if (priceNodes.size() != 0) {
                priceNode = priceNodes.get(priceNodes.size() - 1);
            }
        }
        if (priceNode != null) {
            String priceString = StringUtils.filterAndTrim(priceNode.getText().toString(), Arrays.asList("Rs.", " ", ","));
            price = Float.parseFloat(priceString);
        } else {
            price = 0.0f;
        }


        TagNode imageNode = getSubNodeByXPath(root, XPATH_IMAGE, new ContentParseException("image not found"));
        String imageUrl = imageNode.getAttributeByName("data-src");

        TagNode comingStatusNode = getSubNodeByXPath(root, XPATH_STATUS_COMING, null);
        if (comingStatusNode != null) {
            fetchedProduct.setStatus(ProductStatus.OUTSTOCK);
            fetchedProduct.setWebsite(Website.FLIPKART);
            fetchedProduct.setTitle(title);
            fetchedProduct.setImageUrl(imageUrl);
            fetchedProduct.setUrl(url);
            fetchedProduct.setPrice(price);
            fetchedProduct.setSourceId(sourceId);
            result.addProduct(fetchedProduct);
        }

        TagNode statusNode = getSubNodeByXPath(root, XPATH_STATUS, null);
        if (statusNode != null) {
            fetchedProduct.setStatus(ProductStatus.OUTSTOCK);
        } else {
            fetchedProduct.setStatus(ProductStatus.ONSALE);
        }

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setPrice(price);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(FlipkartHelper.getCleanUrl(url));
        fetchedProduct.setWebsite(Website.FLIPKART);
        fetchedProduct.setSourceId(sourceId);
        //fetchedProduct.setSourcePid(FlipkartHelper.getProductIdByUrl(url));
        fetchedProduct.setSubTitle(subTitle);
//        summaryProduct.setPageHtml(pageHtml);

        return result;
    }

    @Override
    public FetchResult fetch(String queryStr) {
        if (queryStr == null) {
            return null;
        }
        if (!queryStr.contains(StringConstant.FETCH_RESULT_KEY_LIST)) {
            return null;
        }
        queryStr = queryStr.replace(StringConstant.FETCH_RESULT_KEY_LIST, "");
        String[] strings = queryStr.split("_");
        if (strings.length != 2) {
            return null;
        }
        String webSiteStr = strings[0];
        String keywordStr = strings[1];

        Website webSite = Website.valueOf(webSiteStr);

        IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(webSite);
        FetchResult fetchResult = new FetchResult(webSite, keywordStr);
        try {
            List<ListProduct> listProducts = new ArrayList<ListProduct>();
            if (listProcessor != null) {
                listProducts = listProcessor.getProductSetByKeyword(keywordStr, 10);
            }
            logger.debug(String.format("found [%d] products. search[%s] from [%s].", listProducts.size(), keywordStr, Website.FLIPKART.name()));
            fetchResult.setTaskStatus(TaskStatus.FINSH);
            fetchResult.setListProducts(listProducts);
            return fetchResult;
        } catch (Exception e) {
            logger.error("error : search {} from {}.Info : {}", keywordStr, Website.FLIPKART, e.getMessage());
            fetchResult.setTaskStatus(TaskStatus.EXCEPTION);
            fetchResult.setListProducts(null);
            return fetchResult;
        }
    }

    @Override
    public void cache(FetchResult pop) {
        redisService.add(getFetchResultKey(pop.getWebsite(), pop.getKeyword()), JSONUtil.toJSON(pop), 1000);
    }

    private String getFetchResultKey(Website webSite, String keyWord) {
        return StringConstant.FETCH_RESULT_KEY_LIST + webSite + "_" + keyWord;
    }

}
