package hasoffer.fetch.sites.ebay;

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
 * Created on 2016/3/1.
 */
public class EbaySummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//span[@id='vi-lkhdr-itmTitl']";
    private static final String XPATH_TITLE1 = "//h1[@id='itemTitle']";
    private static final String XPATH_PRICE = "//span[@id='prcIsum']";
    private static final String XPATH_PRICE1 = "//span[@id='mm-saleDscPrc']";
    private static final String XPATH_IMAGE = "//div[@id='mainImgHldr']/img";
    private static final String XPATH_LISTURL = "//div[@id='LeftPanelInner']";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();
        String sourceId = EbayHelper.getProductIdByUrl(url);

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode leftPanelNode = getSubNodeByXPath(root, XPATH_LISTURL, null);
        if (leftPanelNode != null) {
            fetchedProduct.setTitle("list url");
            fetchedProduct.setUrl(url);
            fetchedProduct.setWebsite(Website.EBAY);
            fetchedProduct.setProductStatus(ProductStatus.OFFSALE);
            return fetchedProduct;
        }

        String title = "";
        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, null);
        if (titleNode == null) {
            titleNode = getSubNodeByXPath(root, XPATH_TITLE1, new ContentParseException("title not found"));
            String[] subStrs = titleNode.getText().toString().split("about");
            title = subStrs[1].trim();
        } else {
            title = titleNode.getText().toString().trim();
        }

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, null);
        if (priceNode == null) {
            priceNode = getSubNodeByXPath(root, XPATH_PRICE1, new ContentParseException("price not found"));
        }
        String[] subStrs1 = priceNode.getText().toString().split("Rs.");
        String priceString = subStrs1[1].trim().replace(",", "");
        float price = Float.parseFloat(priceString);

        TagNode imageNode = null;
        List<TagNode> imageNodes = getSubNodesByXPath(root, XPATH_IMAGE, new ContentParseException("image not found"));
        if (imageNodes.size() > 0) {
            imageNode = imageNodes.get(imageNodes.size() - 1);
        }
        String imageUrl = imageNode.getAttributeByName("src");

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.EBAY);
        fetchedProduct.setSourceSid(sourceId);

        return fetchedProduct;
    }
}
