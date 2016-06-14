package hasoffer.fetch.sites.saholic;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import org.htmlcleaner.TagNode;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created on 2016/2/29.
 */
public class SaholicSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//span[@class='product-name']";
    private static final String XPATH_PRICE = "//span[@id='sp']";
    private static final String XPATH_BRAND = "//span[@class='brand']";
    private static final String XPATH_PRODUCTID = "//input[@id='product_id']";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode brandNode = getSubNodeByXPath(root, XPATH_BRAND, new ContentParseException("brand not found"));
        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = brandNode.getText().toString().trim();
        title += " "+titleNode.getText().toString().trim();

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, new ContentParseException("price not found"));
        float price = Float.parseFloat(priceNode.getText().toString());

        TagNode sourceIdNode = getSubNodeByXPath(root, XPATH_PRODUCTID, new ContentParseException("sourceId not found"));
        String sourceId = sourceIdNode.getAttributeByName("value").toString().trim();

        fetchedProduct.setUrl(url);
        fetchedProduct.setTitle(title);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setPrice(price);
        fetchedProduct.setWebsite(Website.SAHOLIC);
        fetchedProduct.setSourceSid(sourceId);

        return fetchedProduct;
    }
}
