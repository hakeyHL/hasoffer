package hasoffer.fetch.sites.snapdeal;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.model.ProductStatus;
import org.apache.commons.lang.StringEscapeUtils;
import org.htmlcleaner.TagNode;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created on 2016/2/29.
 */
public class SnapdealSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//h1[@class='pdp-e-i-head']";
    private static final String XPATH_PRICE = "//span[@class='payBlkBig']";
    private static final String XPATH_PRODUCT_IMAGE = "//img[@itemprop='image']";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        if (url.contains("viewAllSellers")) {
            TagNode root = HtmlUtils.getUrlRootTagNode(url);
            TagNode toProductNode = getSubNodeByXPath(root, "//a[@class='pge-lnk']", new ContentParseException("back to product not found"));
            url = toProductNode.getAttributeByName("href");
        }

        FetchedProduct fetchedProduct = new FetchedProduct();
        String sourceId = SnapdealHelper.getProductIdByUrl(url);

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();
        title = StringEscapeUtils.unescapeHtml(title);

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, new ContentParseException("price not found"));
        String priceString = priceNode.getText().toString().replace(",", "");
        float price = Float.parseFloat(priceString);

        TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, new ContentParseException("image not found"));
        String imageUrl = imageNode.getAttributeByName("src");

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setUrl(url);
        fetchedProduct.setTitle(title);
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setWebsite(Website.SNAPDEAL);
        fetchedProduct.setSourceSid(sourceId);

        return fetchedProduct;
    }
}
