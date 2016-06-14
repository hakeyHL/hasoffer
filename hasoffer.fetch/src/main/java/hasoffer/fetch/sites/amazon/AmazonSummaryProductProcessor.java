package hasoffer.fetch.sites.amazon;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.exception.amazon.AmazonRobotCheckException;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.model.ProductStatus;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodesByXPath;

/**
 * Created on 2016/2/29.
 */
public class AmazonSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//span[@id='productTitle']";
    private static final String XPATH_NOTFOUND = "//p[@class='a-last']";
    private static final String XPATH_STATUS = "//div[@id='availability']";
    private static final String XPATH_PRICE = "//span[@id='priceblock_ourprice']";
    private static final String XPATH_PRICE1 = "//span[@id='priceblock_saleprice']";
    private static final String XPATH_PRICE2 = "//span[@id='priceblock_dealprice']";
    private static final String XPATH_PRODUCT_IMAGE = "//div[@id='imgTagWrapperId']/img";
    private static Logger logger = LoggerFactory.getLogger(AmazonSummaryProductProcessor.class);

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        url = url.replace("dp", "gp/offer-listing");

        String sourceId = AmazonHelper.getProductIdByUrl(url);
        fetchedProduct.setWebsite(Website.AMAZON);

        fetchedProduct.setUrl(url);
        fetchedProduct.setSourceSid(sourceId);

        if (url.contains("offer-listing")) {

            final String XPATH_PRODUCT_IMAGE = "//div[@id='olpProductImage']/a/img";
            final String XPATH_PRODUCT_TITLE = "//div[@id='olpProductDetails']/h1";
            final String XPATH_PRODUCT_TITLE1 = "//div[@id='olpProductDetails']/h1[1]";
            final String XPATH_PRODUCT_LIST = "//div[@role='main']";
            final String XPATH_NOPRODUCT = "//div[@class='a-section a-spacing-medium a-spacing-top-medium']/h2";
            final Pattern pricePattern = Pattern.compile(".*Rs. ([0-9]+).*");

            TagNode productListRoot = HtmlUtils.getUrlRootTagNode(url);

            TagNode bodyNode = getSubNodeByXPath(productListRoot, "//meta[@http-equiv='refresh']", null);
            if (bodyNode != null) {
                String contentString = bodyNode.getAttributeByName("content");
                if (contentString.contains("404")) {
                    fetchedProduct.setProductStatus(ProductStatus.OFFSALE);
                    fetchedProduct.setTitle("url expired");
                    return fetchedProduct;
                }
            }

            TagNode productNotFoundNode = getSubNodeByXPath(productListRoot, XPATH_NOTFOUND, null);
            if (productNotFoundNode != null) {
                throw new AmazonRobotCheckException(url);
            }

            TagNode imageNode = getSubNodeByXPath(productListRoot, XPATH_PRODUCT_IMAGE, null);
            String imageUrl = "";
            if (imageNode != null) {
                imageUrl = imageNode.getAttributeByName("src");
            }
            fetchedProduct.setImageUrl(imageUrl);

            TagNode titleNode = getSubNodeByXPath(productListRoot, XPATH_PRODUCT_TITLE, null);
            if (titleNode == null) {
                titleNode = getSubNodeByXPath(productListRoot, XPATH_PRODUCT_TITLE1, new ContentParseException("title not found"));
            }
            String title = titleNode.getText().toString().replace("All offers for", "").trim();
            fetchedProduct.setTitle(title);

            TagNode noProductNode = getSubNodeByXPath(productListRoot, XPATH_NOPRODUCT, null);
            if (noProductNode != null) {
                fetchedProduct.setProductStatus(ProductStatus.OUTSTOCK);
                fetchedProduct.setTitle(title);
                fetchedProduct.setImageUrl(imageUrl);
                return fetchedProduct;
            }

            TagNode productListNodes = getSubNodeByXPath(productListRoot, XPATH_PRODUCT_LIST, null);
            if (productListNodes != null) {
                TagNode firstProductNode = getSubNodesByXPath(productListNodes, "/div[@class='a-row a-spacing-mini olpOffer']", new ContentParseException("product not found")).get(0);
                TagNode firstProductPriceNode = getSubNodeByXPath(firstProductNode, "/div[1]/span[1]/span", new ContentParseException("price not found"));
                String firstProductPriceString = firstProductPriceNode.getText().toString().replace(",", "");
                Matcher matcher = pricePattern.matcher(firstProductPriceString);
                String priceString = "0.0";
                if (matcher.matches()) {
                    priceString = matcher.group(1);
                }
                float price = Float.parseFloat(priceString);
                fetchedProduct.setProductStatus(ProductStatus.ONSALE);
                fetchedProduct.setPrice(price);
            } else {
                fetchedProduct.setProductStatus(ProductStatus.OUTSTOCK);
                fetchedProduct.setPrice(0.0f);
            }
        } else {
            TagNode root = HtmlUtils.getUrlRootTagNode(url);

            TagNode bodyNode = getSubNodeByXPath(root, "//body", null);
            if (bodyNode == null) {
                fetchedProduct.setProductStatus(ProductStatus.OFFSALE);
                fetchedProduct.setTitle("url expired");
                return fetchedProduct;
            }

            TagNode productNotFoundNode = getSubNodeByXPath(root, XPATH_NOTFOUND, null);
            if (productNotFoundNode != null) {
                throw new AmazonRobotCheckException(url);
            }

            TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
            String title = titleNode.getText().toString().trim();

            TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, null);
            String imageUrl = imageNode.getAttributeByName("data-old-hires");

            float price = 0.0f;
            TagNode statusNode = getSubNodeByXPath(root, XPATH_STATUS, null);
            //todo 这里不太确定，暂时先用of来判断，内容是否为out of stock
            if (statusNode != null && statusNode.getText().toString().contains("of")) {
                fetchedProduct.setProductStatus(ProductStatus.OUTSTOCK);
            } else {
                TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, null);
                if (priceNode == null) {
                    priceNode = getSubNodeByXPath(root, XPATH_PRICE1, null);
                }
                if (priceNode == null) {
                    priceNode = getSubNodeByXPath(root, XPATH_PRICE2, null);
                }

                if (priceNode == null) {
                    priceNode = getSubNodeByXPath(root, "//span[@class='olp-padding-right']/span[@class='a-color-price']", null);
                }

                String priceString = priceNode.getText().toString().trim();
                priceString = StringUtils.filterAndTrim(priceString, Arrays.asList("&nbsp;", ","));

                if (priceString.contains("-")) {
                    String[] ps = priceString.split("-");
                    priceString = StringUtils.filterAndTrim(ps[0], null);
                }

                if (NumberUtils.isNumber(priceString)) {
                    price = Float.parseFloat(priceString);
                }
                fetchedProduct.setPrice(price);
                fetchedProduct.setProductStatus(ProductStatus.ONSALE);
            }

            fetchedProduct.setImageUrl(imageUrl);
            fetchedProduct.setTitle(title);
        }
        return fetchedProduct;
    }
}
