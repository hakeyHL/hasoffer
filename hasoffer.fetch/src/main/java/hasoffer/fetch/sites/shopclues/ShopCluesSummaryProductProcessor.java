package hasoffer.fetch.sites.shopclues;

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
 * Created on 2016/3/1.
 */
public class ShopCluesSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_TITLE = "//div[@class='product-about']/div[@class='name']/div/h1";
    private static final String XPATH_PRICE = "//div[@class='price']";
    private static final String XPATH_PRODUCT_ID = "//div[@class='product-about']/div[@class='name']/div/span/span";
    private static final String XPATH_PRODUCT_IMAGE = "//div[@class='slide']/a[1]";
    private static final String XPATH_PRICE1 = "//div[@class='price']/span";
    private static final String XPATH_PRODUCT_STATUS = "//ul[@class='breadcrumb-pages']";
    private static final String XPATH_PRODUCT_SOLDOUT = "//span[@class='strong out-of-stock']";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws ContentParseException, HttpFetchException {

        FetchedProduct fetchedProduct = new FetchedProduct();

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        //该标记已商品详情页的分页导航为准，如果没有认为商品offsale
        TagNode statusNode = getSubNodeByXPath(root, XPATH_PRODUCT_STATUS, null);
        if (statusNode == null) {
            fetchedProduct.setProductStatus(ProductStatus.OFFSALE);
            fetchedProduct.setWebsite(Website.SHOPCLUES);
            fetchedProduct.setUrl(url);
            return fetchedProduct;
        }

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();

        float price = 0.0f;
        String priceString = "";
        TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, null);
        if (priceNode == null) {
            priceNode = getSubNodeByXPath(root, XPATH_PRICE1, new ContentParseException("price not found"));
            priceString = StringUtils.filterAndTrim(priceNode.getText().toString(), Arrays.asList(",","(approx.)"));
        } else {
            priceString = priceNode.getText().toString();
            String[] subStrs1 = priceString.split("Rs.");
            priceString = subStrs1[1].replace(",", "").replace(" ", "").replace("(approx.)","").trim();
        }
        price = Float.parseFloat(priceString);

        TagNode sourceIdNode = getSubNodeByXPath(root, XPATH_PRODUCT_ID, new ContentParseException("sourceId not found"));
        String[] subStrs2 = sourceIdNode.getText().toString().split(":");
        String sourceId = subStrs2[1].trim();

        TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, new ContentParseException("image not found"));
        String imageUrl = imageNode.getAttributeByName("href");

        //shopclues商品soldout时，还有image，title，
        TagNode soldOutNode = getSubNodeByXPath(root, XPATH_PRODUCT_SOLDOUT, null);
        if (soldOutNode != null) {
            fetchedProduct.setProductStatus(ProductStatus.OUTSTOCK);
        } else {
            fetchedProduct.setProductStatus(ProductStatus.ONSALE);
        }

        fetchedProduct.setPrice(price);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.SHOPCLUES);
        fetchedProduct.setSourceSid(sourceId);
        fetchedProduct.setImageUrl(imageUrl);

        return fetchedProduct;
    }

}
