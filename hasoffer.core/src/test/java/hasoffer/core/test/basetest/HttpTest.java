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
import hasoffer.base.utils.http.XPathUtils;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import hasoffer.core.persistence.po.ptm.PtmImage2;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.utils.Httphelper;
import org.apache.commons.io.FileUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.*;

import static hasoffer.base.utils.HtmlUtils.getSubNodesByXPath;
import static hasoffer.base.utils.http.XPathUtils.getSubNodeByXPath;

/**
 * Date : 2016/5/31
 * Function :
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class HttpTest {

    public static final String WEBSITE_91MOBILE_URL_PREFIEX = "http://www.91mobiles.com";
    public static final String WEBSITE_IDEALO_URL_PREFIEX = "http://www.idealo.in";
    public static final String WEBSITE_DX_URL_PREFIEX = "http://www.dx.com/";

//    @Resource
//    IProductService productService;

    @Test
    public void testKeywordRepository() throws Exception {

        List<KeywordRepository> keywordRepositoryList = new ArrayList<>();

//        getFlipkartKeyword(keywordRepositoryList);
//        getSnapdealKeyword(keywordRepositoryList);
        getMyntraKeyword(keywordRepositoryList);

        System.out.println(keywordRepositoryList);

    }

    private void getMyntraKeyword(List<KeywordRepository> keywordRepositoryList) {

        try {

            String category = "CLOTHING";

            HttpResponseModel responseModel = HttpUtils.get("http://www.myntra.com/", null);

            String urlHtml = responseModel.getBodyString();

//            String urlHtml = HtmlUtils.getUrlHtml("http://www.myntra.com/");

            String[] subStr = urlHtml.split("window.__myx_seo__ = \\[\\[");

            String keywordString = StringUtils.filterAndTrim(subStr[1].substring(0, subStr[1].indexOf(';')), Arrays.asList("\\]", "\\["));

            String[] subStr1 = keywordString.split("\"name\":\"");

            for (int i = 1; i < subStr1.length; i++) {

                String keyword = subStr1[i].substring(0, subStr1[i].indexOf('\"'));

                KeywordRepository keywordRepository = new KeywordRepository();

                keywordRepository.categoryName = category;
                keywordRepository.keyword = keyword;
                keywordRepository.sourceSite = Website.MYNTRA;

                keywordRepositoryList.add(keywordRepository);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getSnapdealKeyword(List<KeywordRepository> keywordRepositoryList) {

        try {

            TagNode root = HtmlUtils.getUrlRootTagNode("https://www.snapdeal.com/");

            List<TagNode> divNodeList = getSubNodesByXPath(root, "//div[@class='brandContainerFooter']");

            for (int i = 0; i < divNodeList.size(); i++) {

                TagNode divNode = divNodeList.get(i);

                TagNode categoryNode = getSubNodeByXPath(divNode, "/span/ul/a", null);

                String category = StringUtils.filterAndTrim(categoryNode.getText().toString(), Arrays.asList(":"));

                List<TagNode> keywordList = getSubNodesByXPath(divNode, "/span/ul/li/a");

                for (TagNode keywordNode : keywordList) {

                    String keyword = keywordNode.getText().toString().trim();

                    KeywordRepository keywordRepository = new KeywordRepository();

                    keywordRepository.categoryName = category;
                    keywordRepository.keyword = keyword;
                    keywordRepository.sourceSite = Website.SNAPDEAL;

                    keywordRepositoryList.add(keywordRepository);

                }
            }

        } catch (Exception e) {

        }

    }

    public void getFlipkartKeyword(List<KeywordRepository> keywordRepositoryList) {

        try {

            TagNode root = HtmlUtils.getUrlRootTagNode("https://www.flipkart.com/");

            List<TagNode> divNodeList = getSubNodesByXPath(root, "//div[@class='col gu12 gY-jFd']/div");

            for (int i = 1; i < divNodeList.size(); i++) {

                TagNode divNode = divNodeList.get(i);

                TagNode categoryNode = getSubNodeByXPath(divNode, "/div/div", null);

                String category = categoryNode.getText().toString();

                String[] subStr = category.split(":");

                category = subStr[0].trim();

                List<TagNode> keywordList = getSubNodesByXPath(divNode, "/div/div/a");

                for (TagNode keywordNode : keywordList) {

                    String keyword = keywordNode.getText().toString().trim();

                    KeywordRepository keywordRepository = new KeywordRepository();

                    keywordRepository.categoryName = category;
                    keywordRepository.keyword = keyword;
                    keywordRepository.sourceSite = Website.FLIPKART;

                    keywordRepositoryList.add(keywordRepository);

                }
            }

        } catch (Exception e) {

        }
    }

    @Test
    public void testIdealo() throws Exception {

        Map<String, String> header = new HashMap<>();

        header.put("Cookie", "__gads=ID=88ee8312d09581a1:T=1481079407:S=ALNI_MbWY7_4E5ElST-ZCBViQzNrynrArQ; _ga=GA1.2.870805199.1481268002; cbk=e1d4b1e04818f98be820dcd6ef93b3177114f81367feb64750ea740b25cc14fb; JSESSIONID=aaaPzf_0boE9s6aYfX3Jv; wt_fa=lv~1481684713161|1497236713161#cv~4|1497236713161#fv~2016-12|1496820006850#vf~1|1497236713160#; nmatf=1; wt3_eid=%3B152479990235431%7C2148161758100055317%232148168474600441450; wt3_sid=%3B152479990235431; wt_fa_s=start~1|1513220746465#; ab_tests=%7B%22variants%22%3A%7B%22optin_modal%22%3A%7B%22variant%22%3A%22A%22%7D%2C%22cat_navigation%22%3A%7B%22variant%22%3A%22A%22%7D%2C%22oop_rwd_de_1%22%3A%7B%22variant%22%3A%22A%22%7D%2C%22pcat_by_opensearch%22%3A%7B%22variant%22%3A%22B%22%7D%2C%22sp_personalize%22%3A%7B%22variant%22%3A%22B%22%7D%2C%22oop_rwd_fr_2%22%3A%7B%22variant%22%3A%22B%22%7D%7D%7D; ipcuid=01y90rb700iwn8rg1c; icda=1");
        header.put("Host", "www.idealo.in");
        header.put("Proxy-Connection", "keep-alive");
        header.put("Cache-Control", "max-age=0");
        header.put("Upgrade-Insecure-Requests", "1");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36");
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Accept-Encoding", "gzip, deflate, sdch");
        header.put("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6,it;q=0.4");

        HttpResponseModel httpResponseModel = HttpUtils.getByRedirect("http://www.idealo.in/go/845649125.html?categoryId=26151&pos=1&price=1606.0&productid=4403794&sid=300314&type=offer", header);

        System.out.println(httpResponseModel);
    }

    @Test
    public void test_desc() throws Exception {
        File file = new File("e:/test_desc.html");
        FileReader fr = new FileReader(file);

        TagNode tagNode = HtmlUtils.getTagNode(fr);
        List<TagNode> tagNodes = getSubNodesByXPath(tagNode, "//div[@class='postblock']//p");

        for (TagNode tn : tagNodes) {
            System.out.println(tn.getText());
        }
    }

    @Test
    public void test() throws HttpFetchException, ContentParseException {
        String url = "http://www.t-cat.com.tw/Inquire/Trace.aspx?no=620020081615";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        boolean contains = root.getText().toString().contains("非有效單號");
        String statusString = "";
        int shipStatus = 0;

        if (contains) {
            shipStatus = 3;
            statusString = "无效运单号";
        } else {
            TagNode shippingNode = XPathUtils.getSubNodeByXPath(root, "//span[@id='ctl00_ContentPlaceHolder1_lblTNs']", null);

            if (shippingNode != null) {
                shipStatus = 0;
                statusString = "配送 中";
            } else {
                TagNode statusNode = XPathUtils.getSubNodeByXPath(root, "//div[@id='ctl00_ContentPlaceHolder1_tblResult']/table[1]/tbody/tr[2]/td[2]", null);
                statusString = statusNode.getText().toString().trim();
                if ("順利送達".equals(statusString)) {
                    shipStatus = 1;
                } else if ("拒收(調查處理中)".equals(statusString)) {
                    shipStatus = 2;
                } else {
                    shipStatus = 0;
                }
            }
        }

        System.out.println(shipStatus);
        System.out.println(statusString);

    }

    @Test
    public void test_desc_() throws Exception {
        String url = "http://www.desidime.com/forums/hot-deals-online/topics/get-up-to-600-cashback-has-giftcard-amazon-pantry";
        HttpResponseModel responseModel = HttpUtils.get(url, null);

        File file = new File("e:/test_desc.html");

        file.createNewFile();

        FileUtils.write(file, responseModel.getBodyString());
    }

    @Test
    public void fetchHuiji() throws Exception {

        String[] urlArray = new String[]{
                "http://www.dx.com/c/hobbies-toys-899/rc-airplanes-quadcopters-805?pageSize=200&page=1",
                "http://www.dx.com/c/hobbies-toys-899/rc-airplanes-quadcopters-805?pageSize=200&page=2",
                "http://www.dx.com/c/hobbies-toys-899/rc-airplanes-quadcopters-805?pageSize=200&page=3",
        };

        int count = 1;
        for (String url : urlArray) {
            try {
                TagNode pageRoot = HtmlUtils.getUrlRootTagNode(url);

                List<TagNode> productNodeList = getSubNodesByXPath(pageRoot, "//ul[@class='productList subList']/li");

                for (TagNode productNode : productNodeList) {
                    try {
                        TagNode urlNode = getSubNodeByXPath(productNode, "//div[@class='photo']/a", new ContentParseException("url node not found"));
                        String productUrl = WEBSITE_DX_URL_PREFIEX + urlNode.getAttributeByName("href");

                        System.out.println(count++ + " : " + productUrl);
                        fetchOne(productUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void fetchOne(String productUrl) throws Exception {


        TagNode productInfoPageRoot = HtmlUtils.getUrlRootTagNode(productUrl);
        String urlHtml = HtmlUtils.getUrlHtml(productUrl);

        Huiji huiji = new Huiji();
        huiji.setUrl(productUrl);

        TagNode imagebrotherNode = getSubNodeByXPath(productInfoPageRoot, "//div[@id='midPicBox']", new ContentParseException("image brother node not found"));

        TagNode parentNode = imagebrotherNode.getParent();

        List<TagNode> imageListNode = getSubNodesByXPath(parentNode, "//div[@class='small_photo']/div/ul");

        imageListNode = getSubNodesByXPath(imageListNode.get(0), "/li");

        for (TagNode imageNode : imageListNode) {

            imageNode = getSubNodeByXPath(imageNode, "/a", new ContentParseException("image node not found"));

            String imageUrlString = imageNode.getAttributeByName("rel");

            JSONObject json = JSONObject.parseObject(imageUrlString);

            String simageUrl = json.getString("sImg");
            String mimageUrl = StringUtils.filterAndTrim(simageUrl, Arrays.asList("_small"));

            Map<String, String> imageMap = new HashMap<>();

            imageMap.put("simg", "http:" + simageUrl);
            imageMap.put("mimg", "http:" + mimageUrl);
            imageMap.put("bimg", "http:" + mimageUrl);

            huiji.getImageList().add(imageMap);
        }

        for (Object obj : huiji.getImageList()) {
            Map<String, String> image = (Map<String, String>) obj;

            PtmImage2 image2 = new PtmImage2(huiji.getUrl(), image.get("bimg"), image.get("mimg"), image.get("simg"));
//            productService.saveImage222(image2);
        }

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

    class KeywordRepository {

        public String keyword;
        public Website sourceSite;
        public String categoryName;

    }

    class Huiji {
        private String id;
        private String url;
        private List<Object> imageList = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<Object> getImageList() {
            return imageList;
        }

        public void setImageList(List<Object> imageList) {
            this.imageList = imageList;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "Huiji{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", imageList=" + imageList +
                    '}';
        }
    }
}
