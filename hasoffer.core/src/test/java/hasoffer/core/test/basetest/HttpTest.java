package hasoffer.core.test.basetest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.utils.Httphelper;
import org.apache.commons.lang3.math.NumberUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.Test;

import java.util.*;

import static hasoffer.base.utils.HtmlUtils.getSubNodesByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Date : 2016/5/31
 * Function :
 */
public class HttpTest {

    public static final String WEBSITE_91MOBILE_URL_PREFIEX = "http://www.91mobiles.com";

    @Test
    public void test91Mobile() throws HttpFetchException, XPatherException, ContentParseException {

        String url = "http://www.91mobiles.com/template/category_finder/finder_ajax.php?ord=0.5544784158021026&requestType=2&listType=list&selMobSort=views&amount=1000%3B45000&sCatName=phone&price_range_apply=0&tr_fl%5B%5D=mob_market_status_filter.marketstatus_filter%3Aava_stores&search=&hidFrmSubFlag=1&page=2&category=mobile&unique_sort=&hdnCategory=mobile&user_search=&url_feat_rule=";

        String html = HtmlUtils.getUrlHtml(url);

        JSONObject object = JSONObject.parseObject(html);

        html = object.getString("response");

        html = html.substring(html.indexOf('<'));

        TagNode root = new HtmlCleaner().clean(html);

        List<TagNode> productListNode = getSubNodesByXPath(root, "//div[@class='filter filer_finder']");

        for (TagNode productNode : productListNode) {

            TagNode productUrlNode = getSubNodeByXPath(productNode, "//a[@target='_blank']", null);

            String productUrl = productUrlNode.getAttributeByName("href");

            productUrl = WEBSITE_91MOBILE_URL_PREFIEX + productUrl;

            TagNode sourceIdNode = getSubNodeByXPath(productNode, "//span[@title='Add to favourites']", null);
            String sourceId = sourceIdNode.getAttributeByName("data-product-id");

            System.out.println(productUrl + "___" + sourceId);

            PtmProduct ptmProduct = new PtmProduct();

            List<PtmCmpSku> ptmCmpSkuList = new ArrayList<>();

            fetchProductAndSkuList(ptmProduct, ptmCmpSkuList, productUrl, sourceId);

        }

        System.out.println();
    }

    private void fetchProductAndSkuList(PtmProduct ptmProduct, List<PtmCmpSku> ptmCmpSkuList, String productUrl, String sourceId) throws HttpFetchException, ContentParseException, XPatherException {

        TagNode productPageRootTagNode = HtmlUtils.getUrlRootTagNode(productUrl);

        //查询sourceId是否有重复，发现就continue

        TagNode productTitleNode = getSubNodeByXPath(productPageRootTagNode, "//h1", null);

        //主商品title
        String productTitle = StringUtils.filterAndTrim(productTitleNode.getText().toString(), null);

        //主商品图片
        TagNode productImageNode = getSubNodeByXPath(productPageRootTagNode, "//img[@id='mainImage']", null);

        String imageUrl = productImageNode.getAttributeByName("data-zoom-image");

        List<TagNode> skuNodeList = getSubNodesByXPath(productPageRootTagNode, "//ul[@id='found_store_list']/li[@data-stores='yes']");

        Map<String, String> specMap = getSpecMap();

        List<TagNode> specSectionNodeList = getSubNodesByXPath(productPageRootTagNode, "//div[@class='specs_table_wrap']/div/table");

        for (TagNode specSctionNode : specSectionNodeList) {

            List<TagNode> specInfoNode = getSubNodesByXPath(specSctionNode, "/tbody/tr");

            if (specInfoNode != null && getSubNodesByXPath(specInfoNode.get(0), "//table[@class='border specs_table_sub']").size() > 0) {//判断是否还有分块节点

                for (TagNode subSpecNode : specInfoNode) {

                    List<TagNode> subSpecInfoNodeList = getSubNodesByXPath(subSpecNode, "//table[@class='border specs_table_sub']/tbody/tr");

                    for (TagNode subSpecInfoNode : subSpecInfoNodeList) {

                        TagNode subTitleNode = getSubNodeByXPath(subSpecNode, "//div[@class='sub_head']/p/span", null);
                        String subTitle = subTitleNode.getText().toString();

                        fetchSpecInfo(subSpecInfoNode, specMap, subTitle.toUpperCase() + " ");//转换成大写
                    }

                }
            } else {
                for (TagNode specNode : specInfoNode) {
                    fetchSpecInfo(specNode, specMap, "");
                }
            }
        }

        for (TagNode skuNode : skuNodeList) {

            String websiteString = skuNode.getAttributeByName("data-relevance");

            String[] subStr = websiteString.split("\\.");

            if (subStr != null && subStr.length == 2) {
                websiteString = subStr[0].toUpperCase();
            }

            try {

                Website website = Website.valueOf(websiteString);

                TagNode skuTitleNode = getSubNodeByXPath(skuNode, "//p[@class='heading instock div_delivery']", null);
                String skuTitle = skuTitleNode.getText().toString();
                if (StringUtils.isEmpty(skuTitle)) {
                    skuTitle = productTitle;
                }

                float price = 0.0f;
                TagNode skuPriceNode = getSubNodeByXPath(skuNode, "//span[@class='price price_price_color']", null);
                String priceString = StringUtils.filterAndTrim(skuPriceNode.getText().toString(), Arrays.asList("Rs.", ","));
                if (NumberUtils.isNumber(priceString)) {
                    price = Float.parseFloat(priceString);
                }

                int rating = 0;
                TagNode skuStarNode = getSubNodeByXPath(skuNode, "//div[@class='rating prclst']", null);
                String skuStarString = skuStarNode.getAttributeByName("style");
                skuStarString = skuStarString.substring(skuStarString.indexOf(':') + 1, skuStarString.indexOf('%'));
                if (NumberUtils.isNumber(skuStarString)) {
                    rating = Integer.parseInt(skuStarString) / 20;
                }

                //

                PtmCmpSku ptmCmpSku = new PtmCmpSku();

                ptmCmpSku.setWebsite(website);
                ptmCmpSku.setTitle(skuTitle);
                ptmCmpSku.setPrice(price);
                ptmCmpSku.setRatings(rating);

            } catch (Exception e) {
                continue;
            }

        }
    }


    //此处要求，不是区域内部继续分块，subTitle传空值
    private void fetchSpecInfo(TagNode specNode, Map<String, String> specMap, String subTitle) throws ContentParseException {


        TagNode specKeyNode = getSubNodeByXPath(specNode, "//th[@class='scnd']", null);
        TagNode specValueNode = getSubNodeByXPath(specNode, "//td[@class='frth']", null);

        String key = specKeyNode.getText().toString();
        String value = StringUtils.filterAndTrim(specValueNode.getText().toString(), null);

        if (StringUtils.isEmpty(value)) {//如果后面信息为空，判断是否为对勾
            TagNode specValueChildNode = getSubNodeByXPath(specValueNode, "/span", null);
            if (specValueChildNode != null) {
                String classString = specValueChildNode.getAttributeByName("class");
                if (StringUtils.isEqual(classString, "Stylus_check")) {
                    value = "yes";
                }
            }
        }

//        List<TagNode> valueChildNode = specValueNode.getChildTagList();
//        if (valueChildNode.size() > 0) {
//            value = "";//暂时没有想到解决办法，所以先置为空串
//        }

        if (specMap.containsKey(subTitle + key)) {
            if (StringUtils.isEmpty(subTitle)) {
                specMap.put(key, value);
            } else {
                specMap.put(subTitle + key, value);
            }
        }

    }


    public Map<String, String> getSpecMap() {

        Map<String, String> map = new LinkedHashMap<>();

        map.put("Launch Date", "");
        map.put("Brand", "");
        map.put("Model", "");
        map.put("Operating System", "");
        map.put("Custom UI", "");
//        map.put("General SIM Slot(s)", "");
        map.put("SIM Slot(s)", "");
//        map.put("General SIM Size", "");
        map.put("SIM Size", "");
        map.put("Network", "");
        map.put("Fingerprint Sensor", "");
        map.put("Quick Charging", "");
        map.put("Dimensions", "");
        map.put("Weight", "");
        map.put("Build Material", "");
        map.put("Screen Size", "");
        map.put("Screen Resolution", "");
        map.put("Pixel Density", "");
        map.put("Chipset", "");
        map.put("Processor", "");
        map.put("Architecture", "");
        map.put("Graphics", "");
        map.put("RAM", "");
        map.put("Internal Memory", "");
        map.put("Expandable Memory", "");
        map.put("USB OTG Support", "");
        map.put("MAIN CAMERA Resolution", "");
        map.put("MAIN CAMERA Sensor", "");
        map.put("MAIN CAMERA Autofocus", "");
        map.put("MAIN CAMERA Aperture", "");
        map.put("MAIN CAMERA Optical Image Stabilisation", "");
        map.put("MAIN CAMERA Flash", "");
        map.put("MAIN CAMERA Image Resolution", "");
        map.put("MAIN CAMERA Camera Features", "");
        map.put("MAIN CAMERAVideo Recording", "");
        map.put("FRONT CAMERA Resolution", "");
        map.put("FRONT CAMERA Sensor", "");
        map.put("FRONT CAMERA Autofocus", "");
        map.put("Capacity", "");
        map.put("Type", "");
        map.put("User Replaceable", "");
        map.put("SIM Size", "");
        map.put("Network Support", "");
        map.put("VoLTE", "");
        map.put("SIM 1", "");
        map.put("SIM 2", "");
        map.put("Bluetooth", "");
        map.put("GPS", "");
        map.put("NFC", "");
        map.put("USB Connectivity", "");
        map.put("FM Radio", "");
        map.put("Loudspeaker ", "");
        map.put("Audio Jack", "");
        map.put("Fingerprint Sensor Position ", "");
        map.put("Other Sensors", "");

        return map;
    }


    @Test
    public void testHttp() throws Exception {
        String url = "https://www.flipkart.com/mobiles-accessories/pr?sid=tyy&q=JBL+headphone";

        HttpResponseModel responseModel = HttpUtils.get(url, null);

        System.out.println(responseModel.getBodyString());
    }

    @Test
    public void testHttp2() throws Exception {
        String url = "http://60.205.57.68:8888/analysis/t";

        Map<String, Object> formMap = new HashMap<String, Object>();
        formMap.put("title", "MapmyIndia ICENAV 301 IN-Dash AVN-Universal GPS Navigation Device");

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");

        HttpResponseModel responseModel = HttpUtils.post(url, formMap, headerMap);

        JSONObject jsObj = JSON.parseObject(responseModel.getBodyString());

        System.out.println(responseModel.getBodyString());
    }

    @Test
    public void getFlipkartProductInfo() throws Exception {

        String url = "https://www.flipkart.com/api/3/page/dynamic/product";

        String json = "{\"requestContext\":{\"productId\":\"MOBEYHZ2VSVKHAZH\"}}";

        Map<String, String> header = new HashMap<>();

        header.put("x-user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36 FKUA/website/41/website/Desktop");

        String response = Httphelper.doPostJsonWithHeader(url, json, header);

        JSONObject jsonObject = JSONObject.parseObject(response.trim());

        JSONArray pathArray = jsonObject.getJSONObject("RESPONSE").getJSONObject("data").getJSONObject("product_breadcrumb").getJSONArray("data").getJSONObject(0).getJSONObject("value").getJSONArray("productBreadcrumbs");
        for (int i = 1; i < pathArray.size(); i++) {

            if (i > 3) {
                break;
            }

            String categoryPath = pathArray.getJSONObject(i).getString("title");

            System.out.println(categoryPath);

        }


    }
}
