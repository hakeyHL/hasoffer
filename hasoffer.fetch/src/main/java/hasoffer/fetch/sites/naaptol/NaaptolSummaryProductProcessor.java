package hasoffer.fetch.sites.naaptol;

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
public class NaaptolSummaryProductProcessor implements ISummaryProductProcessor {

    private static final String XPATH_PRODUCT_STATUS = "//p[@class='button_head']";
    private static final String XPATH_TITLE = "//div[@id='square_Details']/h1";
    private static final String XPATH_PRICE = "//li[@id='productPriceDisplay']/span[@class='offer-price']";
    private static final String XPATH_PRODUCTID = "//span[@itemprop='productID']";
    private static final String XPATH_PRODUCT_IMAGE = "//div[@class='zoomPad']/img";
    private static final String XPATH_PRODUCT_IMAGE1 = "//div[@id='main_image']/a/img";

    @Override
    public FetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        FetchedProduct fetchedProduct = new FetchedProduct();
        fetchedProduct.setProductStatus(ProductStatus.ONSALE);

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode productStatusNode = getSubNodeByXPath(root, XPATH_PRODUCT_STATUS, null);
        float price = 0.0f;
        if(productStatusNode!=null){
            fetchedProduct.setProductStatus(ProductStatus.OUTSTOCK);
        }else{
            TagNode priceNode = getSubNodeByXPath(root, XPATH_PRICE, null);
            if (priceNode == null) {
                List<TagNode> priceNodeList = getSubNodesByXPath(root, XPATH_PRICE, new ContentParseException("price not found"));

                priceNode = priceNodeList.get(0);
            }
            String[] subStrs1 = priceNode.getText().toString().split("\\+");
            price = Float.parseFloat(subStrs1[0].replace(",", "").replace("*", "").trim());
        }

        TagNode titleNode = getSubNodeByXPath(root, XPATH_TITLE, new ContentParseException("title not found"));
        String title = titleNode.getText().toString().trim();

        TagNode sourceIdNode = getSubNodeByXPath(root, XPATH_PRODUCTID, new ContentParseException("productId not found"));
        String sourceId = sourceIdNode.getText().toString().trim();

        String imageUrl = "";
        TagNode imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE, null);
        if (imageNode == null) {
            imageNode = getSubNodeByXPath(root, XPATH_PRODUCT_IMAGE1, new ContentParseException("image not found"));
        }
        imageUrl = imageNode.getAttributeByName("src");

        fetchedProduct.setImageUrl(imageUrl);
        fetchedProduct.setPrice(price);
        fetchedProduct.setTitle(title);
        fetchedProduct.setUrl(url);
        fetchedProduct.setWebsite(Website.NAAPTOL);
        fetchedProduct.setSourceSid(sourceId);

        return fetchedProduct;
    }
}
