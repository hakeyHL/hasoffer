package hasoffer.fetch.sites.shopmonk;

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
 * Created by wing on 2016/3/1.
 */
public class ShopmonkSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//section/div/h1[@id='page-title']";
    private static final String XPATH_PRICE = "//span[@id='selling-price']/span[2]";
    private static final String XPATH_PRODUCT_IMAGE = "//div[@id='product-showcase']/div[@id='main-image-wrapper']/img";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();


        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, new ContentParseException("price not found"));
        String priceString = priceNode.getText().toString().replace(",", "").trim();
        float price = Float.parseFloat(priceString);

        TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, new ContentParseException("image not found"));
        String imageUrl = imageNode.getAttributeByName("src");

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.SHOPMONK);

        return fetchedProduct;
    }
}
