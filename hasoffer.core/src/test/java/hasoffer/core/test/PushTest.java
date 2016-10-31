package hasoffer.core.test;

import com.google.android.gcm.server.MulticastResult;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.Website;
import hasoffer.core.bo.push.*;
import hasoffer.core.push.IPushService;
import hasoffer.core.push.impl.PushServiceImpl;
import hasoffer.core.user.IDeviceService;
import hasoffer.fetch.helper.WebsiteHelper;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Date : 2016/4/27
 * Function :
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class PushTest {

    @Resource
    IDeviceService deviceService;
    @Resource
    IPushService pushService;

    @Test
    public void f1() {
        String deeplinkWithAff = WebsiteHelper.getDeeplinkWithAff(Website.AMAZON, "http://www.amazon.in/dp/B00FXLC9V4", new String[]{MarketChannel.GOOGLEPLAY.name(), ""});

        System.out.println(deeplinkWithAff);
    }

    @Test
    public void f() {
        PushServiceImpl p = new PushServiceImpl();
//        String to3 = "d8jxhUTX_V8:APA91bERHojtZm7qCDNgdeUU3PXgHEkgDVXA8xbxdEZ-3MUb-HaisGuRL-IvfQ-lgoIx9vdZ38IJVHblHszk_OkOz5F548r80UUEqqDbY8XI6Jonv_LYYEQ6kuXgxOl6uNAIcwlhs2Ao";
//        String to2 = "ewZ71EnFqlw:APA91bEna9Fvlkcp1qo2yNfSnDyKBOtUnnC-4GSfeEUPoCHZO6PG2EjL4oHO_XpDylqagW1NHCFwS2GZq7M6MuR5ZaphlFVyqJ7MOOS6YEvv9cCSwID1u5qRIg5srI8oU5qBzo8XCVCI";
//        String to1 = "cTJQKR5lAX0:APA91bHKSxBXExt2Rj26HSL9NG4503f-z1dgmAX2rftijut6IPwg_6WDu4xUSv4nEzzBWaxd6vlqjNAWzB4UqCn7-NktO6MsQbCA0bnfBHhisqFtYasJLnf9RQJZ4iXH8l23-c1WiHYl";
        String to2 = "f2CyNQ8nbNw:APA91bEwvofMCGEHZBpn_6iddWRkk5OtG51-u5L016Gcr6X3Rq94panrblt1p-3R9s35Isht9KljA9_267O1N77pyiYdMpvpLG_NpQZDu3RngId66cslD9i6VMi1eVFO7eNswDW3nk4l";
//        String to3 = "de1R9kclQ4g:APA91bEzvw6cS2i_iDa3Xd-b4x1rRcDhlIwB4yDQ1R52TZ20eJRCjCsQBtRmfCbck2n48-XHHXzM_ymdJpVXDEg8_YX2AzFEd6bu8ZG7I7Gz5oK09dl-bUgLhFMx4S-vtOYIFi-JvaaL";

//        System.out.println(to2.equals(to3));

        AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) Now available at Rs.10,999, click to view details.  ", "Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) ", "Now available at Rs.10,999, click to view details. ", "https://www.baidu.com/img/bd_logo1.png"),
                new AppMsgClick(AppMsgClickType.DEAL, "99000264", "com.flipkart.android")
        );

        /*AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("hello hasoffer - DEEPLINK", "DEEPLINK", "DEEPLINK"),
                new AppMsgClick(AppMsgClickType.DEEPLINK, "http://dl.flipkart.com/dl/htc-desire-600-dual-sim/p/itme6g3ubuzgd2gm?pid=MOBDMEQVZJEMWTDG", WebsiteHelper.getPackage(Website.FLIPKART))
        );*/

        /*AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("hello hasoffer - webview", "webview", "webview"),
                new AppMsgClick(AppMsgClickType.WEBVIEW, "https://m.snapdeal.com/product/apple-iphone-5s-16-gb/1204769399?aff_id=82856&utm_source=aff_prog&utm_campaign=afts&offer_id=17&aff_sub=168567", "")
        );*/

        /*AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("hello hasoffer", "hello", "hasoffer"),
                new AppMsgClick(AppMsgClickType.GOOGLEPLAY, "", "com.india.hasoffer")
        );*/

        AppPushBo pushBo = new AppPushBo("678678", "19:50", message);

//        pushService.push(to1, pushBo);
        p.push(to2, pushBo);
//        pushService.push(to3, pushBo);

        System.out.println();
    }

    @Test
    public void f2() {

        AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("demo+test", "demo", "test"),
                new AppMsgClick(AppMsgClickType.DEAL, "123456", WebsiteHelper.getPackage(Website.FLIPKART))
        );

        AppPushBo pushBo = new AppPushBo("678678", "19:50", message);

        PushServiceImpl p = new PushServiceImpl();

        String to2 = "eRs3M4KL4nc:APA91bFDKR0fb4qd0Hwioc2bZ9c-LoWddf6Z8WiqGRcFuYPjcdVI0dfocy7mYI1aFWYDC73q-XG-BoiB0mpRGig2OXpjeb8wpStjfy8mR0W4k9vbEXqOww2iKs1p3cL88LiYk7uE97k2";

        p.push(to2, pushBo);

    }

    @Test
    public void test3() throws Exception {

        List<String> tokenList = new ArrayList<>();

        tokenList.add("dxLMV5s_9hI:APA91bHkfqPk5_dWodDYuq7DmUyaOnGjhb3ggbeSwRIuMOkMFMDiu6AI_q18Q6UCCPmndQjizhtO-M9g_mvoSpzMB62d3DRheq8mMlCHEO8Zf723Y8RPr8saEwr0BwXigPQF_RowK8P3");
        tokenList.add("dmFm5L9n6V8:APA91bGaHzGGwh_bTcKzGSK1yt3Lu7TNSAur5Emt47R3jazmlMEsF7cmwMuPRxrFa1NzfXTDJReZrW7DDpBnkmWiJvaD7i0t4XzRFGTB10lzVbUN4sRzvcw-vA6-zdBls4LoPcNnuZaH");

        AppPushMessage message = new AppPushMessage(
//                new AppMsgDisplay("Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) Now available at Rs.10,999, click to view details.  ", "Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) ", "Now available at Rs.10,999, click to view details. "),
                null,
                new AppMsgClick(AppMsgClickType.DEAL, "99000264", "com.flipkart.android")
        );

        AppPushBo pushBo = new AppPushBo("678678", "19:50", message);


        MulticastResult multicastResult = new PushServiceImpl().GroupPush(tokenList, pushBo);

        System.out.println(multicastResult);

    }

    @Test
    public void test4() throws Exception {

        List<String> tokenList = new ArrayList<>();
        tokenList.add("fnYp6Xli_zw:APA91bElDjle2n7JoUUlMnpQ4p-cgv8y8PGlycWzC3g4E079tVxkt71O_l8ZinJdgcFAWV9KbBsAOzvCRnZiyi_HzOY8hd47uHIvmkcoPuuMGspCJWCwxdPipCfdPM8Vydc2eG_j-HIG");

//        tokenList.add("这里将urmdeice表里面，apptype='APP' appversion = 29 的用户的gcmtoken查出来")

        AppPushMessage message = new AppPushMessage(
//                new AppMsgDisplay("第一个参数=第二个参数+第三个参数","第二个参数，商品标题","第三个参数，加个描述"),//此处可以仿照下面搞一下
                new AppMsgDisplay("Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) Now available at Rs.10,999, click to view details.  ",
                        "Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) ",
                        "Now available at Rs.10,999, click to view details. "),
//                null,这个地方为null的话，客户端不会显示
                new AppMsgClick(AppMsgClickType.DEAL, "99000264", "com.flipkart.android")
        );
        AppPushBo pushBo = new AppPushBo("678678", "19:50", message);//前俩个参数任意



        MulticastResult multicastResult = new PushServiceImpl().GroupPush(tokenList, pushBo);

        System.out.println(multicastResult);

    }


}
