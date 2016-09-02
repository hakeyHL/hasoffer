package hasoffer.fetch.sites.amazon;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.OriFetchedProduct;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;

import java.util.Arrays;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created by Administrator on 2016/9/2.
 */
public class UsaAmazonSummaryProductProcessor implements ISummaryProductProcessor {

    @Override
    public OriFetchedProduct getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        OriFetchedProduct oriFetchedProduct = new OriFetchedProduct();

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, "//span[@id='productTitle']", new ContentParseException("title not found"));

        String title = StringUtils.filterAndTrim(titleNode.getText().toString(), null);

        TagNode imageNode = getSubNodeByXPath(root, "//div[@id='imgTagWrapperId']", new ContentParseException("image node not found"));

        String imageUrl = imageNode.getAttributeByName("src");

//        File file = ;

//        HttpUtils.getImage(imageUrl,file);

        float price = 0.0f;

        TagNode priceNode = getSubNodeByXPath(root, "//span[@id='priceblock_dealprice']", null);

        if (priceNode == null) {

            priceNode = getSubNodeByXPath(root, "//span[@id='priceblock_ourprice']", new ContentParseException("price node not found"));

        }

        String priceString = StringUtils.filterAndTrim(priceNode.getText().toString(), Arrays.asList(",", "$"));
        if(NumberUtils.isNumber(priceString)){
            price = Float.parseFloat(priceString);
        }else{
            System.out.println("priceString is "+priceString+" parse fail");
        }

        oriFetchedProduct.setTitle(title);
        oriFetchedProduct.setImageUrl(imageUrl);
        oriFetchedProduct.setPrice(price);

        return oriFetchedProduct;
    }
}
