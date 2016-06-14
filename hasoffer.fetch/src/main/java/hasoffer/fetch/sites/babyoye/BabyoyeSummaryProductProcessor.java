package hasoffer.fetch.sites.babyoye;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.ProductStatus;
import hasoffer.fetch.model.FetchedProduct;
import org.htmlcleaner.TagNode;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created on 2016/3/1.
 */
public class BabyoyeSummaryProductProcessor implements ISummaryProductProcessor{

    private static final String XPATH_TITLE = "//span[@itemprop='name']";
    private static final String XPATH_PRICE = "//span[@id='current_product_price']";
    private static final String XPATH_IMAGE= "//a[@id='Zoomer']/img";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, new ContentParseException("price not found"));
        String priceNodeString = priceNode.getText().toString();
        String priceString = StringUtils.unescapeHtml(priceNodeString).replace(",","").trim();
        //todo priceString有个空格，待改进
        float price = Float.parseFloat(priceString.substring(1,priceString.length()));

        TagNode imageNode = getSubNodeByXPath(root, XPATH_IMAGE, new ContentParseException("image not found"));
        String imageUrl = imageNode.getAttributeByName("src");

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.BABYOYE);
        fetchedProduct.setSourceSid(BabyoyeHelper.getProductIdByUrl(url));

        return fetchedProduct;
    }
}
