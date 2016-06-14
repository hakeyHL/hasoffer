package hasoffer.fetch.sites.bagittoday;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import org.htmlcleaner.TagNode;

import java.util.Arrays;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created on 2016/4/6.
 */
public class BagittodaySummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_PRODUCT_SOURCEID = "//input[@id='productID']";
    private static final String XPATH_PRODUCT_TITLE = "//h1[@class='pdProductHeading']";
    private static final String XPATH_PRODUCT_IMAGE = "//meta[@property='og:image']";
    private static final String XPATH_PRODUCT_PRICE = "//span[@class='main_price']";
    private static final String XPATH_PRODUCT_STATUS = "//div[@class='prdErrorMsg']";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.BAGITTODAY);


        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode statusNode = getSubNodeByXPath(root, XPATH_PRODUCT_STATUS, null);
        if (statusNode != null) {
            fetchedProduct.setProductStatus(ProductStatus.OFFSALE);
            return fetchedProduct;
        }

        TagNode sourceIdNode = getSubNodeByXPath(root, XPATH_PRODUCT_SOURCEID, new ContentParseException("sourceID not found for [" + url + "]"));
        String sourceId = sourceIdNode.getAttributeByName("value");
        fetchedProduct.setSourceSid(sourceId);

        TagNode titleNode = getSubNodeByXPath(root, XPATH_PRODUCT_TITLE, new ContentParseException("title not found for [" + url + "]"));
        String title = titleNode.getText().toString();
        fetchedProduct.setTitle(title);

        TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, new ContentParseException("image not found for [" + url + "]"));
        String imageUrl = imageNode.getAttributeByName("content");
        fetchedProduct.setImageUrl(imageUrl);

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRODUCT_PRICE, new ContentParseException("price not found for [" + url + "]"));
        String priceString = priceNode.getText().toString();
        priceString = StringUtils.filterAndTrim(priceString, Arrays.asList("r"));
        float price = Float.parseFloat(priceString);
        fetchedProduct.setPrice(price);

        fetchedProduct.setProductStatus(ProductStatus.ONSALE);

        return fetchedProduct;
    }
}
