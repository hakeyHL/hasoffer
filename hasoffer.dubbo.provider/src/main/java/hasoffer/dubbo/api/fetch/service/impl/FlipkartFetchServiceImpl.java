package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.XPathUtils;
import hasoffer.dubbo.api.fetch.po.FetchResult;
import hasoffer.dubbo.api.fetch.service.IFetchService;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.helper.WebsiteProcessorFactory;
import hasoffer.fetch.model.ListProduct;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.sites.flipkart.FlipkartHelper;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodesByXPath;

public class FlipkartFetchServiceImpl extends BaseFetchServiceImpl implements IFetchService{
    private Logger logger = LoggerFactory.getLogger(FlipkartFetchServiceImpl.class);

    private static final String XPATH_TITLE = "//h1[@itemprop='name']";
    private static final String XPATH_SUBTITLE = "//span[@class='subtitle']";
    private static final String XPATH_PRICE1 = "//span[@class='selling-price omniture-field']";
    private static final String XPATH_IMAGE = "//div[@class='imgWrapper']/img[1]";
    private static final String XPATH_STATUS = "//div[@class='out-of-stock-status']";
    private static final String XPATH_STATUS_COMING = "//div[@class='coming-soon-status']";
    private static final String XPATH_SOURCEID = "//div[@id='reco-module-wrapper']";

    @Override
    public FetchResult getProductsKeyWord(Website webSite, String keyword, int startIndex, int endIndex) {
        IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(Website.FLIPKART);
        try {
            List<ListProduct> listProducts = new ArrayList<ListProduct>();
            if (listProcessor != null) {
                listProducts = listProcessor.getProductSetByKeyword(keyword, 10);
            }
            logger.debug(String.format("found [%d] products. search[%s] from [%s].", listProducts.size(), keyword, Website.FLIPKART.name()));
            FetchResult fetchResult = new FetchResult();
            fetchResult.setTaskStatus(TaskStatus.RUNNING);
            fetchResult.setListProducts(listProducts);
            return fetchResult;
        } catch (Exception e) {
            logger.error("error : search {} from {}.Info : {}", keyword, Website.FLIPKART, e.getMessage());
        }
        return null;
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
}
