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
        if (disPriceNode == null) {
            disPriceNode = getSubNodeByXPath(root, "//span[@id='priceblock_ourprice']", null);
            if (disPriceNode == null) {
                disPriceNode = getSubNodeByXPath(root, "//span[@id='priceblock_saleprice']", null);
            }
        }

        TagNode priceNode = getSubNodeByXPath(root, "//span[@class='a-text-strike']", null);
        if (priceNode == null) {
            priceNode = getSubNodeByXPath(root, "//span[@id='priceblock_ourprice']", null);
        }

        if (priceNode != null) {
            String priceString = StringUtils.filterAndTrim(priceNode.getText().toString(), Arrays.asList(",", "$"));

            if (priceString.contains("-")) {
                priceString = priceString.split("-")[0];
                System.out.println("subString priceString " + priceString);
                priceString = StringUtils.filterAndTrim(priceString, null);
            }

            if (NumberUtils.isNumber(priceString)) {
                price = Float.parseFloat(priceString);
            } else {
                System.out.println("priceString is " + priceString + " parse fail");
            }
        }

        if (disPriceNode == null) {
            disPrice = price;
        } else {
            String disPriceString = StringUtils.filterAndTrim(disPriceNode.getText().toString(), Arrays.asList(",", "$"));

            if (disPriceString.contains("-")) {
                disPriceString = disPriceString.split("-")[0];
                System.out.println("subString disPriceString " + disPriceString);
                disPriceString = StringUtils.filterAndTrim(disPriceString, null);
            }

            if (NumberUtils.isNumber(disPriceString)) {
                disPrice = Float.parseFloat(disPriceString);
            } else {
                System.out.println("disPriceString is " + disPriceString + " parse fail" + url);
            }
        }

        if (priceNode == null) {
            price = disPrice;
        }

        //先将gp/product换成
        url = url.replace("/gp/product/", "/dp/");
        String[] urlParamArray = url.split("/dp/");
        String sourceIdString = urlParamArray[1];
        int index = -1;
        if (sourceIdString.contains("/")) {
            index = sourceIdString.indexOf("/");
        } else {
            index = sourceIdString.indexOf("?");
        }

        if (index == -1) {
            throw new RuntimeException("subString don't hava / or ?");
        }

        String secondUrl = "://www.amazon.com/dp/" + sourceIdString.substring(0, index) + "/";

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
