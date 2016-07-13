package hasoffer.fetch.sites.flipkart;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.utils.HtmlUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Created on 2016/7/13.
 */
public class FlipkartImageProcessor {


    private static final String XPATH_IMAGE = "//div[@class='imgWrapper']/img[1]";

    public static String getFlipkartImageUrl(String url) throws HttpFetchException, ContentParseException {

        if (url != null && url.contains("dl.flipkart.com/dl/")) {
            url = FlipkartHelper.getUrlByDeeplink(url);
        }

        String pageHtml = HtmlUtils.getUrlHtml(url);

        TagNode root = new HtmlCleaner().clean(pageHtml);

        TagNode imageNode = getSubNodeByXPath(root, XPATH_IMAGE, new ContentParseException("image not found"));
        String imageUrl = imageNode.getAttributeByName("data-src");

        return imageUrl;
    }

}
