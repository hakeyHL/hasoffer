package hasoffer.core.test.basetest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.persistence.mongo.PtmProductDescription;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.utils.Httphelper;
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
    public static final String WEBSITE_BONEPRICE_URL_PREFIEX = "http://www.boneprice.com/";

    @Test
    public void testBonepriceMobileFetch() throws Exception {

        //http://www.boneprice.com/category.html?s=7392&cat=Mobile_Phone&vt=list

        String prefix = "http://www.boneprice.com/category.html?s=";
        String suffix = "&cat=Mobile_Phone&vt=list";

        for (int i = 0; i < 353; i++) {

            int result = i * 20 + i;

            String url = prefix + result + suffix;

            String html = HtmlUtils.getUrlHtml(url);

            TagNode listPageRoot = HtmlUtils.getUrlRootTagNode(url);

            List<TagNode> productNodeList = getSubNodesByXPath(listPageRoot, "//ul[@class='listPageRoot']");

            for (TagNode productNode : productNodeList) {

                TagNode productUrlNode = getSubNodeByXPath(productNode, "/div[@class='back_link_layer']", null);

                String productUrl = productUrlNode.getAttributeByName("onclick");

                productUrl = StringUtils.filterAndTrim(productUrl, Arrays.asList("location.href=", "'"));

                productUrl = WEBSITE_BONEPRICE_URL_PREFIEX + productUrl;

                System.out.println("product url = " + productUrl);

                PtmProduct ptmProduct = new PtmProduct();

                List<PtmCmpSku> skuList = new ArrayList<>();

                PtmProductDescription productDescription = new PtmProductDescription();

                bonepriceMobileFetch(ptmProduct, skuList, productDescription, productUrl);

                //存数据的时候注意判断sourceId

            }
        }

        System.out.println();
    }

    /**
     * 该方法根据主商品的url，直接抓取ptmproduct和其对应的ptmcmpskuList，返回true时候，可以创建，返回false，建立跳过不创建
     *
     * @param ptmProduct
     * @param skuList
     * @param productDescription
     * @param productUrl
     * @return
     * @throws Exception
     */
    private boolean bonepriceMobileFetch(PtmProduct ptmProduct, List<PtmCmpSku> skuList, PtmProductDescription productDescription, String productUrl) throws Exception {

        /*
        主商品，设置标题、图片、价格（使用支持的skuList的最低价格）、主商品评分、主商品描述、sourceId（//input[@id='item_id']/@value）、sourceSite、sourceUrl
        skuList 设置标题、图片、价格、网站、oriUrl、url（lazada特殊处理）、站内评分
         */

        String html = HtmlUtils.getUrlHtml(productUrl);

        TagNode productDetailPageRootNode = HtmlUtils.getUrlRootTagNode(productUrl);

        getSubNodesByXPath(productDetailPageRootNode, "//ul[@class='p_list']/li");


        return true;
    }

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

//            fetchProductAndSkuList(ptmProduct, ptmCmpSkuList, productUrl, sourceId);//has bean remove to fixcontroller
        }

        System.out.println();
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
