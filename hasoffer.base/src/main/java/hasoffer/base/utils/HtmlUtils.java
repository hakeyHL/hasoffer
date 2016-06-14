package hasoffer.base.utils;

import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.http.HttpUtils;
import org.htmlcleaner.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class HtmlUtils {

    public static final HtmlCleaner CLEANER = new HtmlCleaner();

    private static final int DEFAULT_TRY_TIMES = 1;

    public static HttpResponseModel getResponse(String url, int tryTimes) {
        HttpResponseModel responseModel = new HttpResponseModel(-1, "", "UTF-8", new byte[0], "", null);

        if (tryTimes <= 0) {
            tryTimes = 1;
        }

        for (int retry = 0; retry < tryTimes; retry++) {
            responseModel = HttpUtils.get(url, null);

            if (responseModel.getStatusCode() > 300 && responseModel.getStatusCode() < 400) {
                if(StringUtils.isEmpty(responseModel.getRedirect())){
                    responseModel = HttpUtils.get(responseModel.getRedirect(),null);
                }
            }

            boolean hasError = responseModel.isHasException();
            if (hasError) {
                continue;
            } else {
                return responseModel;
            }
        }
        return responseModel;
    }

    public static TagNode getUrlRootTagNode(String url) throws HttpFetchException {
        HttpResponseModel response = getResponse(url, DEFAULT_TRY_TIMES);
        if (response.isHasException() || response.getStatusCode() != 200) {
            if (response.getStatusCode() == 500) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                response = getResponse(url, DEFAULT_TRY_TIMES);
                if (response.isHasException() || response.getStatusCode() != 200) {
                    throw new HttpFetchException("HTTP " + String.valueOf(response.getStatusCode()) +
                            " url : " + url);
                }
            } else {
                throw new HttpFetchException("HTTP " + String.valueOf(response.getStatusCode()) +
                        " url : " + url);
            }
        }
        String html = response.getBodyString();
        return CLEANER.clean(html);
    }

    public static String getUrlHtml(String url) throws HttpFetchException {
        HttpResponseModel response = getResponse(url, DEFAULT_TRY_TIMES);
        if (response.isHasException() || response.getStatusCode() != 200) {
            throw new HttpFetchException("HTTP " + String.valueOf(response.getStatusCode()) +
                    " url : " + url);
        }
        return response.getBodyString();
    }

    public static TagNode getTagNode(String html) {
        return CLEANER.clean(html);
    }

    public static String getHtml(TagNode node) {
        return CLEANER.getInnerHtml(node);
    }

    public static TagNode getFirstNodeByXPath(TagNode node, String xpath) throws XPatherException {
        List<TagNode> nodes = getSubNodesByXPath(node, xpath);
        if (nodes != null && nodes.size() > 0) {
            return nodes.get(0);
        }
        return null;
    }

    public static List<TagNode> getSubNodesByXPath(TagNode node, String xpath) throws XPatherException {
        ArrayList<TagNode> result = new ArrayList<TagNode>();
        try {
            Object[] nodes = node.evaluateXPath(xpath);
            if (nodes == null) {
                return null;
            }
            for (Object subNode : nodes) {
                result.add((TagNode) subNode);
            }
            return result;
        } catch (XPatherException e) {
            throw e;
        }
    }

    public static String getNodeSelfTextByXPath(TagNode node) {
        List childEles = node.getAllChildren();
        String text = "";
        for (Object child : childEles) {
            if (child.getClass() == ContentNode.class) {
                text += ((ContentNode) child).getContent();
            }
        }

        return text;
    }

    public static String getInnerHTML(TagNode node) {
        CleanerProperties properties = new CleanerProperties();
        properties.setOmitXmlDeclaration(true);
        final XmlSerializer xmlSerializer = new PrettyXmlSerializer(properties);
        List<? extends BaseToken> children = node.getAllChildren();
        StringWriter stringWriter = new StringWriter(2048);
        for (BaseToken child : children) {
            try {
                child.serialize(xmlSerializer, stringWriter);
            } catch (IOException ignore) {
                // ignore
            }
        }
        return stringWriter.toString();
    }

    public static String getOuterHTML(TagNode node) {
        CleanerProperties properties = new CleanerProperties();
        properties.setOmitXmlDeclaration(true);
        final XmlSerializer xmlSerializer = new PrettyXmlSerializer(properties);
        StringWriter stringWriter = new StringWriter(2048);
        try {
            node.serialize(xmlSerializer, stringWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringWriter.toString();
    }

    public static String getFirstImgUrl(String htmlText) throws XPatherException {
        String xpath_img = "//img";
        TagNode node = CLEANER.clean(htmlText);
        Object[] nodes = node.evaluateXPath(xpath_img);
        if (nodes == null || nodes.length == 0) {
            return null;
        }

        TagNode imgNode = (TagNode) nodes[0];
        String url = imgNode.getAttributeByName("src");
        return url;
    }
}
