package hasoffer.core.test.basetest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.StringUtils;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.mongo.MobileCateDescription;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2016/5/16.
 */
public class StringTest {

    @Test
    public void testJson() {
        String json = "{\"sImg\":\"//img.dxcdn.com/productimages/sku_344792_2_smal.jpg\", \"mImg\":\"//img.dxcdn.com/productimages/sku_344792_2.jpg\", \"bImg\":\"//img.dxcdn.com/productimages/sku_344792_2.jpg\"}";

        JSONObject object = JSONObject.parseObject(json);

        String sImg = (String) object.get("sImg");
        String mImg = (String) object.get("mImg");
        String bImg = (String) object.get("bImg");
    }

    @Test
    public void beanTest() throws InvocationTargetException, IllegalAccessException {

        MobileCateDescription mobileCateDescription = new MobileCateDescription();

        Map<String, String> specMap = new LinkedHashMap<>();
        Map<String, String> newSpecMap = new LinkedHashMap<>();


        //General
        specMap.put("Launch Date", "1");
        specMap.put("Brand", "2");
        specMap.put("Model", "wdq");
        specMap.put("Operating System", "asdasdw");
        specMap.put("Custom UI", "189hd9");
        specMap.put("SIM Slot(s)", "*(@HH&@!");

        //Design
        specMap.put("Dimensions", "shfwihfewf");
        specMap.put("Weight", "!()@JF)!JF");
        specMap.put("Build Material", "N!*(H(!BV(!");

        //Display
        specMap.put("Screen Size", "*(h91hd1ndiaw");
        specMap.put("Screen Resolution", "N*(!H(*!V!#");
        specMap.put("Pixel Density", "afn89023nf2");

        //Performance
        specMap.put("Chipset", "afn923hf29fn");
        specMap.put("Processor", "123123");
        specMap.put("Architecture", "(!GBV(!BV");
        specMap.put("Graphics", "aohwf");
        specMap.put("RAM", "121eh198h9");

        //Storage
        specMap.put("Internal Memory", "acm8932fn92");
        specMap.put("Expandable Memory", ",.,lmoca");
        specMap.put("USB OTG Support", ",.?");

        //Camera
        specMap.put("MAIN CAMERA Resolution", "1231");
        specMap.put("MAIN CAMERA Sensor", "123123");
        specMap.put("MAIN CAMERA Autofocus", "13123");
        specMap.put("MAIN CAMERA Aperture", "sfsdf");
        specMap.put("MAIN CAMERA Optical Image Stabilisation", "asfaf");
        specMap.put("MAIN CAMERA Flash", "adfafd");
        specMap.put("MAIN CAMERA Image Resolution", "adfadfa");
        specMap.put("MAIN CAMERA Camera Features", "1231d1d1");
        specMap.put("MAIN CAMERA Video Recording", "12d1d21d");
        specMap.put("FRONT CAMERA Resolution", "1d12d1");
        specMap.put("FRONT CAMERA Sensor", "1d1d1");
        specMap.put("FRONT CAMERA Autofocus", "d12d12d1");

        //Battery
        specMap.put("Capacity", "1d21d1");
        specMap.put("Type", "1d12d1");
        specMap.put("User Replaceable", "1d1d21");
        specMap.put("Quick Charging", "1d1d21");

        //Network&Connectivity
        specMap.put("SIM Size", "1d1d12d");
        specMap.put("Network Support", "13c4f5f");
        specMap.put("VoLTE", "2f2f424f");
        specMap.put("SIM 1", "f3f2f4");
        specMap.put("SIM 2", "f24f2f");
        specMap.put("Bluetooth", "f2424");
        specMap.put("GPS", "f2f423");
        specMap.put("NFC", "f24f2");
        specMap.put("USB Connectivity", "2f42f4");

        //Multimedia
        specMap.put("FM Radio", "f2f423");
        specMap.put("Loudspeaker", "2f42f4");
        specMap.put("Audio Jack", "2f42f2");

        //Special Features
        specMap.put("Fingerprint Sensor", "2f2f");
        specMap.put("Fingerprint Sensor Position", "2f2342");
        specMap.put("Other Sensors", "f2f423f3");

        for (Map.Entry<String, String> entry : specMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            key = key.replaceAll(" ", "_").replace("(s)", "").toLowerCase();

            newSpecMap.put(key, value);
        }

        BeanUtils.populate(mobileCateDescription, newSpecMap);

        System.out.println(mobileCateDescription);

    }

    @Test
    public void testDiscount() {
        float newPrice = 54090;
        float oriOrice = 54169;

        int i = (int) ((1 - newPrice / oriOrice) * 100);

        System.out.println(i);
    }

    @Test
    public void testFloatToInt() {

        float price = 1231.123f;

        System.out.println((int) price);

    }

    @Test
    public void testuuid() {

        String url = "https://www.amazon.com/gp/product/B000MXKMG2/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B000MXKMG2&linkCode=as2&tag=ascsubtag-20&linkId=0d86b0ec42f12cc0ed8f9e9c58965ade";

        url = url.replace("/gp/product/", "/dp/");
        String[] urlParamArray = url.split("/dp/");
        String sourceIdString = urlParamArray[1];
        url = "https://www.amazon.com/dp/" + sourceIdString.substring(0, sourceIdString.indexOf("/")) + "/";

        System.out.println(url);
    }

    @Test
    public void test() {

        String json = "{\n" +
                "    \"records\": [\n" +
                "        {\n" +
                "            \"domain\": \"hotel.elong.com \\n\\n\",\n" +
                "            \"VIP\": \"211.151.110.32\",\n" +
                "            \"aos_node\": 144\n" +
                "        },\n" +
                "        {\n" +
                "            \"domain\": \"hotel.elong.com \\n\\n\",\n" +
                "            \"VIP\": \"123.59.30.10\",\n" +
                "            \"aos_node\": 156\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        JSONObject jsonObject = JSONObject.parseObject(json);

        JSONArray jsonArray = jsonObject.getJSONArray("records");

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject jsonObject1 = jsonArray.getJSONObject(i);

            String domain = jsonObject1.getString("domain");
            String vipCode = jsonObject1.getString("VIP");
            int aos_code = jsonObject1.getIntValue("aos_node");

            System.out.println(domain + "_" + vipCode + "_" + aos_code);
            System.out.println();

        }

    }

    @Test
    public void test12() {

        String oldUrl = "http://www.amazon.in/gp/offer-listing/B01BK92AMK";

        String newUrl = oldUrl.replace("gp/offer-listing", "dp");

        System.out.println(oldUrl);
        System.out.println(newUrl);

    }

    @Test
    public void test11() {

        String a = "&nbsp;";

        String s = StringUtils.filterAndTrim(a, Arrays.asList("&nbsp;"));

        System.out.println(s);

    }

    @Test
    public void test10() {

        String url = "{\"Transfer Speed\\\":\\\"Read 18.62 MB/sec, Write 4.02 MB/sec\\\",\\\"Part Number\\\":\\\"Cruzer Blade 8gb\\\",\\\"Color\\\":\\\"Multicolor\\\",\\\"Dimensions\\\":\\\"7.4 mm x 17.6 mm x 41.5 mm\\\",\\\"Encryption\\\":\\\"128-bit AES Encryption\\\",\\\"Weight\\\":\\\"2.50 g\\\",\\\"Brand\\\":\\\"SanDisk\\\",\\\"USB on the go\\\":\\\"No\\\",\\\"Form Factor\\\":\\\"USB Flash Drive\\\",\\\"Type\\\":\\\"Utility Pendrive\\\",\\\"Model\\\":\\\"SDCZ50-008G-I35\\\",\\\"Features\\\":\\\"<p>Ultra Portable, Small Size</p> <p>Feather light</p> <p>Smart and stylish</p>\\\",\\\"Case Material\\\":\\\"Plastic\\\",\\\"Capacity (GB)\\\":\\\"8 GB\\\",\\\"Interface\\\":\\\"USB 2.0\\\"}";

        url = StringEscapeUtils.unescapeHtml(url);

        System.out.println(url);

    }

    @Test
    public void test9() {

        String phone = "035";

        String[] split = phone.split(",");

        System.out.println(split);

    }

    @Test
    public void testStr1() {

        String str = "http%3A%2F%2Fwww.ebay.in%2Fitm%2FMOTO-E3-POWER-16GB-2GB-RAM-8MP-2MP-4G-LTE-3500-MAH-PHONE-BLACK-%2F252594992971%3Fhash=item3acfd5b74b%3Ag%3AQ4oAAOSwal5YCJBG%26aff_source=dgm";

        str = StringUtils.urlDecode(str);

        System.out.println(str);

    }

    @Test
    public void testStr2() {

        String str = "Layer'r Shot Compact Explode And Impact Body Spray (Pack Of 2) Combo Set (Set of 2)";

        String str1 = HexDigestUtil.md5(StringUtils.getCleanChars(str));

        System.out.println(str1);

    }


    @Test
    public void testStr3() {

        String str = "Adraxx SM401098 Digital Speedometer (NA X6)";
        Website website = Website.FLIPKART;

        System.out.println(HexDigestUtil.md5(website.name() + StringUtils.getCleanChars(str)));
    }

    @Test
    public void testStr4() {

        String str = "545";

        System.out.println(str.toLowerCase());

    }


    @Test
    public void md5Str() {
        String str = "Luckie1985";

        String s = HexDigestUtil.md5(str);

        System.out.println(s);
    }

    @Test
    public void test5() {
        Long aLong = new Long(TimeUtils.now());
        int i = aLong.intValue();
        int i1 = i % 30;
        System.out.println();
    }

    @Test
    public void test6() {
        Long aLong = new Long(TimeUtils.now());
        long i = aLong.longValue();
        long i1 = i % 30;
        System.out.println();
    }

    @Test
    public void test7() {

        String url = "http://www.amazon.in/XOLO-Q1000s-Plus-Xolo-White/dp/B00PUCPQGQ";

        String[] substr = url.split("/dp/");

        String result = "http://www.amazon.in/gp/offer-listing/" + substr[1];
        System.out.println(substr[0]);
        System.out.println(substr[1]);
        System.out.println(result);
    }

    @Test
    public void test8() {

        Pattern pattern = Pattern.compile("[0-9,+,-]*");

        String phone = "0351-+5486468";

        Matcher matcher = pattern.matcher(phone);

        System.out.println(matcher.matches());

    }
}
