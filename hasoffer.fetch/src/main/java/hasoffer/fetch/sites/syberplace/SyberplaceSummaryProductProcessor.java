package hasoffer.fetch.sites.syberplace;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import org.htmlcleaner.TagNode;

import java.util.List;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodesByXPath;

/**
 * Created on 2016/2/29.
 */
public class SyberplaceSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//form[@id='product_addtocart_form']/div[@class='product-shop']/div/h1";
    private static final String XPATH_PRICE = "//*[@id='product_addtocart_form']/div[@class='product-shop']/div[@class='price-box']/p[@class='special-price']/span";
    private static final String XPATH_PRICE1 = "//form[@id='product_addtocart_form']/div[@class='product-shop']/div[@class='short-description']/div[@class='price-box']/p[@class='special-price']/span";
    private static final String XPATH_PRICE2 = "//span[@class='regular-price']/span";
    private static final String XPATH_PRODUCTID = "//input[@name='product']";
    private static final String XPATH_PRODUCT_IMAGE = "//div[@class='MagicZoomPup']/img";
    private static final String XPATH_PRODUCT_IMAGE1 = "//div[@class='MagicToolboxContainer']/a/img";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, null);
        if (priceNode == null) {
            priceNode = getSubNodeByXPath(root, XPATH_PRICE1, null);
        }
        if (priceNode == null) {
            priceNode = getSubNodeByXPath(root, XPATH_PRICE2, null);
        }
        if (priceNode == null) {
            List<TagNode> priceNodes = getSubNodesByXPath(root, XPATH_PRICE2, new ContentParseException("price not found"));
            if (priceNodes.size() > 0) {
                priceNode = priceNodes.get(0);
            }
        }
        String priceString = priceNode.getText().toString().trim();
        priceString = priceString.replace(",", "").replace("Rs.", "").replace(" ", "");
        float price = Float.parseFloat(priceString);

        TagNode sourceIdNode = getSubNodeByXPath(root, XPATH_PRODUCTID, new ContentParseException("sourceId not found"));
        String sourceId = sourceIdNode.getAttributeByName("value");

        TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, null);
        if (imageNode == null) {
            imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE1, new ContentParseException("image not found"));
        }
        String imageUrl = imageNode.getAttributeByName("src");

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.SYBERPLACE);
        fetchedProduct.setSourceSid(sourceId);

        return fetchedProduct;
    }
}
