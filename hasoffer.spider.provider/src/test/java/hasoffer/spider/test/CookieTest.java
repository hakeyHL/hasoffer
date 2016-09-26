package hasoffer.spider.test;

import hasoffer.base.utils.TimeUtils;
import hasoffer.spider.ext.GPHttpClientDownloader;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;

import java.util.concurrent.TimeUnit;

public class CookieTest {
    private static long currentTime = 0;

    public static void main(String[] args) {
        while (true) {
            test();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private static void test() {
        boolean b = System.currentTimeMillis() - currentTime > TimeUtils.MILLISECONDS_OF_1_MINUTE;
        if (b) {
            Request request = new Request("http://www.amazon.in/");
            GPHttpClientDownloader downloader = new GPHttpClientDownloader();
            Site me = Site.me();
            CloseableHttpResponse httpResponse = downloader.downloadForResponse(request, me.toTask());

            Header headers[] = httpResponse.getHeaders("Set-Cookie");
            String cookieStr = "";
            for (int i = 0; i < headers.length; i++) {
                String[] tmp = headers[i].getValue().split(";");
                for (String x : tmp) {
                    String[] cookieTemp = x.split("=");
                    if (cookieTemp.length != 2) {
                        continue;
                    }
                    cookieStr = cookieStr + ";" + cookieTemp[0] + ":" + cookieTemp[1];
                }
            }
            System.out.println(cookieStr);
            currentTime = System.currentTimeMillis();
        }
    }


}
