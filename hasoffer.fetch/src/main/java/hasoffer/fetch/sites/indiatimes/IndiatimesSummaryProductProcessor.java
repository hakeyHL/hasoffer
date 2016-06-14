package hasoffer.fetch.sites.indiatimes;

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
public class IndiatimesSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//div[@class='flt productcolumone zur']/h1[@itemprop='name']";
    private static final String XPATH_TITLE1 = "//div[@class='producthead']/h1";
    private static final String XPATH_PRICE = "//span[@class='offerprice flt']";
    private static final String XPATH_PRICE1 = "//div[@class='priceinfo flt']/div[@class='newprice']/span[@class='price']";
    private static final String XPATH_IMAGE = "//a[@id='zoom1']/span/img";
    private static final String XPATH_IMAGE1 = "//div[@class='productimghigh']/table/tbody/tr/td/a/img";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();
        String sourceId = IndiatimesHelper.getProductIdByUrl(url);

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, null);
        if (titleNode == null) {
            titleNode = getSubNodeByXPath(root, XPATH_TITLE1, new ContentParseException("title not found"));
        }

        String title = titleNode.getText().toString().trim();

        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, null);
        if (priceNode == null) {
            priceNode = getSubNodeByXPath(root, XPATH_PRICE1, new ContentParseException("price not found"));
        }
        String priceString = priceNode.getText().toString();
        priceString = priceString.replace("`", "").replace(" ", "").replace(",", "");
        float price = Float.parseFloat(priceString);

        String imageUrl = "";
        TagNode imageNode = getSubNodeByXPath(root, XPATH_IMAGE, null);
        if (imageNode == null) {
            imageNode = getSubNodeByXPath(root, XPATH_IMAGE1, new ContentParseException("image not found"));
            imageUrl = imageNode.getAttributeByName("src");
        }else{
            imageUrl = imageNode.getAttributeByName("data-original");
        }

            fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setUrl(url);
        fetchedProduct.setTitle(title);
        fetchedProduct.setPrice(price);
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        fetchedProduct.setWebsite(Website.INDIATIMES);
        fetchedProduct.setSourceSid(sourceId);

        return fetchedProduct;
    }
}
