package hasoffer.api.helper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Created by hs on 2016/7/4.
 */
public class Httphelper {
    public static String getJsonFromRequest(HttpServletRequest request) {
        StringBuffer info = new java.lang.StringBuffer();
        try {
            InputStream in = request.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);
            byte[] buffer = new byte[1024];
            int iRead;
            while ((iRead = buf.read(buffer)) != -1) {
                info.append(new String(buffer, 0, iRead, "UTF-8"));
            }
        } catch (Exception ee) {
        }
        return URLDecoder.decode(info.toString());
    }

    public static String doPost(String url, List<NameValuePair> nvps) throws Exception {
        String responseText = null;
        CloseableHttpClient closeableHttpClient = createHttpsClient();
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        HttpResponse httpResponse = closeableHttpClient.execute(httppost);
        HttpEntity httpEntity2 = httpResponse.getEntity();
        System.out.println("httpResponse.getStatusLine().getStatusCode():" + httpResponse.getStatusLine().getStatusCode());
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(httpEntity2);
            responseText = result;
        } else {
            String result = EntityUtils.toString(httpEntity2);
            responseText = result;
        }
        closeableHttpClient.close();
        return responseText;
    }

    public static String doPost(String url, String json) throws Exception {
        String responseText = null;
        CloseableHttpClient closeableHttpClient = createHttpsClient();
        HttpPost method = new HttpPost(url);
        StringEntity entity = new StringEntity(json, "utf-8");// 解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        method.setEntity(entity);
        HttpResponse httpResponse = closeableHttpClient.execute(method);
        HttpEntity httpEntity2 = httpResponse.getEntity();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(httpEntity2);
            responseText = result;
        }
        closeableHttpClient.close();
        return responseText;
    }


    public static CloseableHttpClient createHttpsClient() throws Exception {
        X509TrustManager x509mgr = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{x509mgr}, new java.security.SecureRandom());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }

    public static void sendJsonMessage(String message, HttpServletResponse response) {
        PrintWriter out = null;
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        try {
            out = response.getWriter();
            out.println(message);
        } catch (Exception ee) {
        } finally {
            out.close();
        }
    }
}
