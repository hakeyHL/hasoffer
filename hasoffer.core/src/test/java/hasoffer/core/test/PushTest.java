package hasoffer.core.test;

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
        String to2 = "c3OWPuuC1Fs:APA91bHUN4NYhs2Ak-Le137B32OyWSl_zZ5lPWSXPvgNtR8jChRHn949sny4thl2HTVUPdFDIkLXeQB8YPzTR0WYLw1bnUAD4xK5IRM_Fq3KlFgog_t9EfFF0I03TpiCt8ro2DEt8akW";
//        String to3 = "de1R9kclQ4g:APA91bEzvw6cS2i_iDa3Xd-b4x1rRcDhlIwB4yDQ1R52TZ20eJRCjCsQBtRmfCbck2n48-XHHXzM_ymdJpVXDEg8_YX2AzFEd6bu8ZG7I7Gz5oK09dl-bUgLhFMx4S-vtOYIFi-JvaaL";

//        System.out.println(to2.equals(to3));

        AppPushMessage message = new AppPushMessage(
                new AppMsgDisplay("Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) Now available at Rs.10,999, click to view details.  ", "Lenovo PHAB 16 GB 6.98 inch with Wi-Fi+4G  (Ebony) ", "Now available at Rs.10,999, click to view details. "),
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

}
