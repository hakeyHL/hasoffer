package hasoffer.base.utils.http;


import hasoffer.base.model.HttpResponseModel;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;

public class MyHttpUtils {

    public static HttpResponseModel getByProxy(String url, HttpHost proxy) throws Exception {

        if (proxy == null) {
            return HttpUtils.get(url, null);
        }

        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        CloseableHttpClient httpclient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();

        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpclient.execute(httpGet);

        Header H_contentType = response.getFirstHeader("Content-Type");

        String contentType = H_contentType != null ? H_contentType.getValue() : "";

        return new HttpResponseModel(
                response.getStatusLine().getStatusCode(),
                contentType, "utf-8",
                EntityUtils.toByteArray(response.getEntity())
        );
    }

}

