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
    public static final String WEBSITE_MYSMARTPRICE_URL_PREFIEX = "https://www.mysmartprice.com";
    public static final String WEBSITE_DX_URL_PREFIEX = "http://www.dx.com/";

    @Test
    public void mySmartPrice() throws Exception {

        String url = "https://www.mysmartprice.com/deals/index.php?viewall=true&parameter=today";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        List<TagNode> nodeList = XPathUtils.getSubNodesByXPath(root, "//div[@data-dealtype='today']/a/@href", null);

        for (TagNode hrefNode : nodeList) {

            String dealUrl = hrefNode.getAttributeByName("href");

            dealUrl = WEBSITE_MYSMARTPRICE_URL_PREFIEX + dealUrl;

            TagNode dealRootNode = HtmlUtils.getUrlRootTagNode(dealUrl);

            TagNode titleNode = XPathUtils.getSubNodeByXPath(dealRootNode, "//h1", null);
            String title = titleNode.getText().toString();

            TagNode viewAllNode = XPathUtils.getSubNodeByXPath(dealRootNode, "//a[@class='sctn__view-all']", null);
            String category = viewAllNode.getAttributeByName("href").split("tags/")[1].split("-deals")[0].replace('-',' ');

            //span[@class='prdct-dtl__mrp']原价
            TagNode originPriceNode = XPathUtils.getSubNodeByXPath(dealRootNode, "//span[@class='prdct-dtl__mrp']", null);
            String oriPriceString = StringUtils.filterAndTrim(originPriceNode.getText().toString(), Arrays.asList("₹"));

            //span[@class='prdct-dtl__dscnt']折扣
            TagNode discountNode = XPathUtils.getSubNodeByXPath(dealRootNode, "//span[@class='prdct-dtl__mrp']", null);
            String discountString = StringUtils.filterAndTrim(discountNode.getText().toString(), Arrays.asList("[", "]"));

            //div[@class='prdct-dtl__prc']现价
            TagNode presentPriceNode = XPathUtils.getSubNodeByXPath(dealRootNode, "//div[@class='prdct-dtl__prc']", null);
            String presentPriceString = StringUtils.filterAndTrim(presentPriceNode.getText().toString(), Arrays.asList("₹"));



        }

        System.out.println();
    }

    @Test
    public void testProm() throws Exception {

        String url = "https://www.promodescuentos.com";

        TagNode root = HtmlUtils.getUrlRootTagNode(url);

        List<TagNode> divNodeList = getSubNodesByXPath(root, "//div[@class='thread-cardWrapper']");

        for (TagNode divNode : divNodeList) {

            TagNode flagNode = getSubNodeByXPath(divNode, "//span[@class='hide--toW3 overflow--ellipsis']", null);

            if (flagNode == null) {
                TagNode titleNode = getSubNodeByXPath(root, "//a[@class='cept-tt linkPlain thread-title-text box--all-b']", null);
                String title = titleNode.getText().toString();
                System.out.println(title);
                continue;
            }


        }

    }


    @Test
    public void testTurkey() {

        try {

            TagNode rootTagNode = HtmlUtils.getUrlRootTagNode("https://www.promodescuentos.com/?page=2");

            List<TagNode> subNodeList = getSubNodesByXPath(rootTagNode, "//div[@class='thread-cardWrapper']");

            int i = subNodeList.size();

            for (TagNode node : subNodeList) {

                try {

                    TagNode flagNode = getSubNodeByXPath(node, "//article/div[@class='thread-content space--h-2 space--fromW3-h-3']/div[@class='tGrid width--all-12 space--mb-3']/div", new ContentParseException("div not found"));

                } catch (Exception e) {
                    System.out.println(i--);
                }

            }

            HttpResponseModel responseModel = HttpUtils.get("https://www.groupon.com/browse/search/partial?_csrf=6TYJ19Bp-fm0vKcFU3JgOftLqP7Z5iGR05es&currentPageUrl=https%253A%252F%252Fwww.groupon.com%252Fbrowse%252Fchicago%253Fcontext%253Dlocal%2526page%253D2&division=chicago&context=local&page=2", null);

            String bodyString = responseModel.getBodyString();

            TagNode infoPageNode = HtmlUtils.getUrlRootTagNode("https://www.groupon.com/deals/superdawg-drive-in-11");

            TagNode infoTitleNode = getSubNodeByXPath(infoPageNode, "//h1[@id='deal-title']", null);

            String infoTitle = infoTitleNode.getText().toString();

            TagNode root = HtmlUtils.getUrlRootTagNode("http://www.firsaton.com/firsatlar/bowling-firsatlari/istanbul");

            List<TagNode> subNodesList = getSubNodesByXPath(root, "//div[@id='deal_list']/div[@class='col-xs-12 col-sm-6 d_outer']");

            for (TagNode node : subNodesList) {

                TagNode titleNode = getSubNodeByXPath(node, "//div[@class='d2_title']/a/span", null);

                String title = titleNode.getText().toString();

                System.out.println("title = " + title);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testShipNum() throws Exception {

        List<String> shipNumList = new ArrayList<>();

        shipNumList.add("620025123665 ");
        shipNumList.add("620025123676 ");
        shipNumList.add("620025123680 ");
        shipNumList.add("620020074441 ");
        shipNumList.add("6254967471 ");
        shipNumList.add("620020075431 ");
        shipNumList.add("620020078550 ");
        shipNumList.add("620020074823 ");
        shipNumList.add("620020073552 ");
        shipNumList.add("620020074505 ");
        shipNumList.add("620020073010 ");
        shipNumList.add("620020067825 ");
        shipNumList.add("625034304260 ");
        shipNumList.add("620020073451 ");
        shipNumList.add("620020069976 ");
        shipNumList.add("620020067814 ");
        shipNumList.add("620020073530 ");
        shipNumList.add("620020066385 ");
        shipNumList.add("6254967502 ");
        shipNumList.add("6254968005 ");
        shipNumList.add("620020067851 ");
        shipNumList.add("625034304335 ");
        shipNumList.add("620020067274 ");
        shipNumList.add("620020067772 ");
        shipNumList.add("620020067454 ");
        shipNumList.add("620020066723 ");
        shipNumList.add("620020067511 ");
        shipNumList.add("620020066951 ");
        shipNumList.add("620020067046 ");
        shipNumList.add("6236220202 ");
        shipNumList.add("620025124962 ");
        shipNumList.add("620020067410 ");
        shipNumList.add("620020067432 ");
        shipNumList.add("620020067645 ");
        shipNumList.add("620020066894 ");
        shipNumList.add("620020067671 ");
        shipNumList.add("625034304324 ");
        shipNumList.add("620020067252 ");
        shipNumList.add("620020067083 ");
        shipNumList.add("620020067592 ");
        shipNumList.add("620025124973 ");
        shipNumList.add("6254967651 ");
        shipNumList.add("620020066846 ");
        shipNumList.add("620020066962 ");
        shipNumList.add("620020067151 ");
        shipNumList.add("620020067331 ");
        shipNumList.add("620020067500 ");
        shipNumList.add("620020066793 ");
        shipNumList.add("620020066940 ");
        shipNumList.add("620020067195 ");
        shipNumList.add("620020066745 ");
        shipNumList.add("620020067612 ");
        shipNumList.add("620020066850 ");
        shipNumList.add("620020067480 ");
        shipNumList.add("620020066666 ");
        shipNumList.add("620020066914 ");
        shipNumList.add("620020067476 ");
        shipNumList.add("620020066600 ");
        shipNumList.add("620025124510 ");
        shipNumList.add("620020067230 ");
        shipNumList.add("620020067421 ");
        shipNumList.add("620020066416 ");
        shipNumList.add("620020067693 ");
        shipNumList.add("620020066633 ");
        shipNumList.add("620020067353 ");
        shipNumList.add("620020067702 ");
        shipNumList.add("620020066936 ");
        shipNumList.add("620020067035 ");
        shipNumList.add("6236220224 ");
        shipNumList.add("620020066883 ");
        shipNumList.add("620020067570 ");
        shipNumList.add("620020066611 ");
        shipNumList.add("620020067241 ");
        shipNumList.add("620020067761 ");
        shipNumList.add("620020067013 ");
        shipNumList.add("620020067555 ");
        shipNumList.add("620020066782 ");
        shipNumList.add("620020067002 ");
        shipNumList.add("620020067162 ");
        shipNumList.add("620020071366 ");
        shipNumList.add("620020067296 ");
        shipNumList.add("6254967603 ");
        shipNumList.add("620020067713 ");
        shipNumList.add("6236219962 ");
        shipNumList.add("6236219973 ");
        shipNumList.add("6254967805 ");
        shipNumList.add("620025124363 ");
        shipNumList.add("6236219984 ");
        shipNumList.add("6254967572 ");
        shipNumList.add("620025124194 ");
        shipNumList.add("6236220000 ");
        shipNumList.add("620025124521 ");
        shipNumList.add("6254967704 ");
        shipNumList.add("620025124835 ");
        shipNumList.add("620025124846 ");
        shipNumList.add("620025124936 ");
        shipNumList.add("620025124936 ");
        shipNumList.add("6254967695 ");
        shipNumList.add("625034304245 ");
        shipNumList.add("6236220044 ");
        shipNumList.add("6236220066 ");
        shipNumList.add("620025124850 ");
        shipNumList.add("6254967715 ");
        shipNumList.add("6254967662 ");
        shipNumList.add("620020066760 ");
        shipNumList.add("620020066861 ");
        shipNumList.add("620020066692 ");
        shipNumList.add("620020067601 ");
        shipNumList.add("620020067342 ");
        shipNumList.add("620020066622 ");
        shipNumList.add("620020066984 ");
        shipNumList.add("620020066756 ");
        shipNumList.add("620020067386 ");
        shipNumList.add("620020067406 ");
        shipNumList.add("620020067491 ");
        shipNumList.add("620025123961 ");
        shipNumList.add("620025124701 ");
        shipNumList.add("620020066405 ");
        shipNumList.add("6236219995 ");
        shipNumList.add("625034304256 ");
        shipNumList.add("620020066576 ");
        shipNumList.add("620025124554 ");
        shipNumList.add("620020066453 ");
        shipNumList.add("6254967401 ");
        shipNumList.add("620025124045 ");
        shipNumList.add("620025124475 ");
        shipNumList.add("620025124734 ");
        shipNumList.add("6254967842 ");
        shipNumList.add("620025124135 ");
        shipNumList.add("620025124486 ");
        shipNumList.add("620020065066 ");
        shipNumList.add("6254967381 ");
        shipNumList.add("6254967820 ");
        shipNumList.add("620025124146 ");
        shipNumList.add("6254967460 ");
        shipNumList.add("6254967921 ");
        shipNumList.add("620020065070 ");
        shipNumList.add("620025124532 ");
        shipNumList.add("620025124666 ");
        shipNumList.add("620025124745 ");
        shipNumList.add("620025124543 ");
        shipNumList.add("620025124203 ");
        shipNumList.add("625034304394 ");
        shipNumList.add("6254967561 ");
        shipNumList.add("620025124251 ");
        shipNumList.add("6254967640 ");
        shipNumList.add("625034304234 ");
        shipNumList.add("620025124183 ");
        shipNumList.add("6254967785 ");
        shipNumList.add("620025124262 ");
        shipNumList.add("620025124670 ");
        shipNumList.add("620025124681 ");
        shipNumList.add("620025124940 ");
        shipNumList.add("620025124600 ");
        shipNumList.add("620025124214 ");
        shipNumList.add("620025124161 ");
        shipNumList.add("620025124824 ");
        shipNumList.add("620025124124 ");
        shipNumList.add("6254967673 ");
        shipNumList.add("620025123950 ");
        shipNumList.add("620025124330 ");
        shipNumList.add("620025124225 ");
        shipNumList.add("620025124405 ");
        shipNumList.add("620025124341 ");
        shipNumList.add("620025124611 ");
        shipNumList.add("620025124273 ");
        shipNumList.add("620025124464 ");
        shipNumList.add("6236220011 ");
        shipNumList.add("620025124576 ");
        shipNumList.add("620020065022 ");
        shipNumList.add("6236220134 ");
        shipNumList.add("620020066475 ");
        shipNumList.add("620025124236 ");
        shipNumList.add("620020066431 ");
        shipNumList.add("6254967412 ");
        shipNumList.add("620025124760 ");
        shipNumList.add("620025124861 ");
        shipNumList.add("6254967445 ");
        shipNumList.add("620025124925 ");
        shipNumList.add("620025124925 ");
        shipNumList.add("620025124622 ");
        shipNumList.add("625034304293 ");
        shipNumList.add("6254967730 ");
        shipNumList.add("6236220145 ");
        shipNumList.add("620025124565 ");
        shipNumList.add("6236220101 ");
        shipNumList.add("620025124284 ");
        shipNumList.add("620025124374 ");
        shipNumList.add("620020066442 ");
        shipNumList.add("620025124315 ");
        shipNumList.add("620020066554 ");
        shipNumList.add("620025124326 ");
        shipNumList.add("625034304313 ");
        shipNumList.add("6236220156 ");
        shipNumList.add("620025124431 ");
        shipNumList.add("625034304181 ");
        shipNumList.add("620025124385 ");
        shipNumList.add("620025124872 ");
        shipNumList.add("620025124442 ");
        shipNumList.add("620025124172 ");
        shipNumList.add("620020066532 ");
        shipNumList.add("6254967594 ");
        shipNumList.add("620025124782 ");
        shipNumList.add("620020066521 ");
        shipNumList.add("620020066510 ");
        shipNumList.add("620020066464 ");
        shipNumList.add("6254967763 ");
        shipNumList.add("6236220112 ");
        shipNumList.add("620020066506 ");
        shipNumList.add("620020066420 ");
        shipNumList.add("6236220171 ");
        shipNumList.add("625034304271 ");
        shipNumList.add("620025124352 ");
        shipNumList.add("620020065033 ");
        shipNumList.add("620025124802 ");
        shipNumList.add("620025124113 ");
        shipNumList.add("620025124113 ");
        shipNumList.add("620025124295 ");
        shipNumList.add("6254967614 ");
        shipNumList.add("620020065294 ");
        shipNumList.add("6254967434 ");
        shipNumList.add("6236220092 ");
        shipNumList.add("6254967752 ");
        shipNumList.add("620020065193 ");
        shipNumList.add("620025123935 ");
        shipNumList.add("620025124001 ");
        shipNumList.add("620020065250 ");
        shipNumList.add("620020065134 ");
        shipNumList.add("620020065632 ");
        shipNumList.add("620020065733 ");
        shipNumList.add("620020065145 ");
        shipNumList.add("620020065101 ");
        shipNumList.add("620025124903 ");
        shipNumList.add("620020065404 ");
        shipNumList.add("620025124012 ");
        shipNumList.add("620025124150 ");
        shipNumList.add("620025124071 ");
        shipNumList.add("620025124056 ");
        shipNumList.add("620025124453 ");
        shipNumList.add("620025124102 ");
        shipNumList.add("620020065770 ");
        shipNumList.add("6236220081 ");
        shipNumList.add("620020065044 ");
        shipNumList.add("620025124633 ");
        shipNumList.add("620025124591 ");
        shipNumList.add("620025123972 ");
        shipNumList.add("620025124034 ");
        shipNumList.add("620025123983 ");
        shipNumList.add("620025124082 ");
        shipNumList.add("620025124023 ");
        shipNumList.add("620025124060 ");
        shipNumList.add("620020065055 ");
        shipNumList.add("6254967392 ");
        shipNumList.add("6254967853 ");
        shipNumList.add("620025123860 ");
        shipNumList.add("620025123711 ");
        shipNumList.add("620025123733 ");
        shipNumList.add("620025123744 ");
        shipNumList.add("620025123722 ");
        shipNumList.add("620025123871 ");
        shipNumList.add("620025123882 ");
        shipNumList.add("620025123913 ");
        shipNumList.add("620025123893 ");
        shipNumList.add("6254967816 ");
        shipNumList.add("620025122780 ");
        shipNumList.add("620025123755 ");
        shipNumList.add("620025123766 ");
        shipNumList.add("620025123770 ");
        shipNumList.add("620025123781 ");
        shipNumList.add("620025121731 ");
        shipNumList.add("620025123856 ");
        shipNumList.add("620025122706 ");
        shipNumList.add("620025121742 ");
        shipNumList.add("620025122822 ");
        shipNumList.add("620025122960 ");
        shipNumList.add("620025123691 ");
        shipNumList.add("620025123092 ");
        shipNumList.add("620025123044 ");
        shipNumList.add("620025123632 ");
        shipNumList.add("620025122324 ");
        shipNumList.add("620025121992 ");
        shipNumList.add("620025122574 ");
        shipNumList.add("620025122686 ");
        shipNumList.add("620025122440 ");
        shipNumList.add("620025122552 ");
        shipNumList.add("620025122346 ");
        shipNumList.add("620025122032 ");
        shipNumList.add("620025122515 ");
        shipNumList.add("620025122256 ");
        shipNumList.add("620025121843 ");
        shipNumList.add("620025122383 ");
        shipNumList.add("620025122495 ");
        shipNumList.add("620025121880 ");
        shipNumList.add("620025122484 ");
        shipNumList.add("620025122394 ");
        shipNumList.add("620025122596 ");
        shipNumList.add("620025121911 ");
        shipNumList.add("620025122541 ");
        shipNumList.add("620025122166 ");
        shipNumList.add("620025122100 ");
        shipNumList.add("620025121900 ");
        shipNumList.add("620025121786 ");
        shipNumList.add("620025122076 ");
        shipNumList.add("620025122260 ");
        shipNumList.add("620025122403 ");
        shipNumList.add("620025122504 ");
        shipNumList.add("620025122302 ");
        shipNumList.add("620025122133 ");
        shipNumList.add("620025121775 ");
        shipNumList.add("620025123621 ");
        shipNumList.add("620025122293 ");
        shipNumList.add("620025122451 ");
        shipNumList.add("620025122170 ");
        shipNumList.add("620025122462 ");
        shipNumList.add("620025122192 ");
        shipNumList.add("620025121821 ");
        shipNumList.add("620025121970 ");
        shipNumList.add("620025121944 ");
        shipNumList.add("620025122065 ");
        shipNumList.add("620025122111 ");
        shipNumList.add("620025121865 ");
        shipNumList.add("620025122361 ");
        shipNumList.add("620025122271 ");
        shipNumList.add("620025122350 ");
        shipNumList.add("620025121810 ");
        shipNumList.add("620025120015 ");
        shipNumList.add("620025121720 ");
        shipNumList.add("620025120153 ");


        for (String shipNum : shipNumList) {

            try {


                String url = "http://www.t-cat.com.tw/Inquire/Trace.aspx?no=" + shipNum.trim();

                TagNode root = HtmlUtils.getUrlRootTagNode(url);


                TagNode statusNode = XPathUtils.getSubNodeByXPath(root, "//div[@id='ctl00_ContentPlaceHolder1_tblResult']/table[1]/tbody/tr[2]/td[2]", null);

                String statusString = statusNode.getText().toString().trim();

                System.out.println(shipNum + statusString);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("exception for " + shipNum);
            }


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
