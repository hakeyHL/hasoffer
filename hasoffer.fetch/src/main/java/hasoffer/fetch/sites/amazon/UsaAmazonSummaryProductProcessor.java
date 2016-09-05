package hasoffer.fetch.sites.amazon;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.fetch.sites.amazon.ext.model.UsaAmazonData;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.TagNode;

import java.util.Arrays;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created by Administrator on 2016/9/2.
 */
public class UsaAmazonSummaryProductProcessor {

    public UsaAmazonData getSummaryProductByUrl(String url) throws HttpFetchException, ContentParseException {

        UsaAmazonData usaAmazonData = new UsaAmazonData();

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        TagNode titleNode = getSubNodeByXPath(root, "//span[@id='productTitle']", new ContentParseException("title not found"));

        String title = StringUtils.filterAndTrim(titleNode.getText().toString(), null);

        String imageUrl = "";

        TagNode imageNode = getSubNodeByXPath(root, "//img[@id='landingImage']", null);

        if (imageNode != null) {
            imageUrl = imageNode.getAttributeByName("data-old-hires");
            if (StringUtils.isEmpty(imageUrl)) {
                imageUrl = imageNode.getAttributeByName("data-a-dynamic-image");
            }
            if (!StringUtils.isEmpty(imageUrl)) {
                imageUrl = imageUrl.substring(imageUrl.indexOf("http"), imageUrl.indexOf(".jpg") + 4);
            }
        }

        float price = 0.0f;
        float disPrice = -1f;

        TagNode disPriceNode = getSubNodeByXPath(root, "//span[@id='priceblock_dealprice']", null);
        TagNode priceNode = getSubNodeByXPath(root, "//span[@class='a-text-strike']", new ContentParseException("price node not found"));

        String priceString = StringUtils.filterAndTrim(priceNode.getText().toString(), Arrays.asList(",", "$"));
        if (NumberUtils.isNumber(priceString)) {
            price = Float.parseFloat(priceString);
        } else {
            System.out.println("priceString is " + priceString + " parse fail");
        }

        if (disPriceNode == null) {
            disPrice = price;
        } else {
            String disPriceString = StringUtils.filterAndTrim(disPriceNode.getText().toString(), Arrays.asList(",", "$"));
            if (NumberUtils.isNumber(disPriceString)) {
                price = Float.parseFloat(disPriceString);
            } else {
                System.out.println("disPriceString is " + disPriceString + " parse fail" + url);
            }
        }

        //先将gp/product换成
        url = url.replace("/gp/product/", "/dp/");
        String[] urlParamArray = url.split("/dp/");
        String sourceIdString = urlParamArray[1];
        String secondUrl = "://www.amazon.com/dp/" + sourceIdString.substring(0, sourceIdString.indexOf("/")) + "/";

        if (url.startsWith("https:")) {
            url = "https" + secondUrl;
        } else {
            url = "http" + secondUrl;
        }

        usaAmazonData.setTitle(title);
        usaAmazonData.setImageUrl(imageUrl);
        usaAmazonData.setPrice(price);
        usaAmazonData.setDisPrice(disPrice);
        usaAmazonData.setLink(url);

        return usaAmazonData;
    }
}
