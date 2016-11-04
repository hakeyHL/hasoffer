package hasoffer.third.proxyip;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

public class ProxyTest {

    public static void main(String[] args) {

        try {
            //Proxy类代理方法
            URL url = new URL("http://www.flipkart.com/");
            // 创建代理服务器
            InetSocketAddress addr = new InetSocketAddress("87.98.219.96", 8080);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr); // http 代理
            URLConnection conn = url.openConnection(proxy);
            InputStream in = conn.getInputStream();
            String s = IOUtils.toString(in);
            System.out.println(s);
            //if (s.indexOf("百度") > 0) {
            //    System.out.println("ok");
            //}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
