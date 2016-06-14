package hasoffer.base.utils.http;

import com.gargoylesoftware.htmlunit.HttpMethod;
import hasoffer.base.model.HttpResponseModel;
import jodd.http.HttpBrowser;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Date : 2016/3/15
 * Function :
 */
public class HttpUtils {

    public static HttpResponseModel getByRedirect(String url, Map<String, String> headerMap) {

        HttpBrowser browser = new HttpBrowser();

        HttpRequest httpRequest = HttpRequest.get(url);
        HttpResponse httpResponse = browser.sendRequest(httpRequest);

        return getResponseModel(httpResponse);
    }

    public static HttpResponseModel get(String url, Map<String, String> headerMap) {

        try {
            HttpRequest httpRequest = getHttpRequest(url, HttpMethod.GET, headerMap);
            HttpResponse httpResponse = httpRequest.send();

            if (url.contains("offer-listing")) {
                return getResponseModelAmazon(httpResponse);
            }

            return getResponseModel(httpResponse);

        } catch (Exception e) {
            return new HttpResponseModel("request error");
        }
    }

    public static HttpResponseModel get(String url, Map<String, String> headerMap, Map<String, String> parameterMap) {

        try {
            HttpRequest httpRequest = getHttpRequest(url, HttpMethod.GET, headerMap, parameterMap);

            HttpResponse httpResponse = httpRequest.send();

            if (url.contains("offer-listing")) {
                return getResponseModelAmazon(httpResponse);
            }

            return getResponseModel(httpResponse);

        } catch (Exception e) {
            return new HttpResponseModel("request error");
        }
    }

    public static HttpResponseModel post(String uri, Map<String, Object> formMap, Map<String, String> headerMap) {
        HttpRequest httpRequest = getHttpRequest(uri, HttpMethod.POST, headerMap);

        httpRequest.form(formMap);

        HttpBrowser browser = new HttpBrowser();
        return getResponseModel(browser.sendRequest(httpRequest));
    }

    public static HttpResponseModel postByRaw(String uri, String bodyStr, Map<String, String> headerMap) {
        HttpRequest httpRequest = getHttpRequest(uri, HttpMethod.POST, headerMap);

        httpRequest.body(bodyStr);

        HttpBrowser browser = new HttpBrowser();
        return getResponseModel(browser.sendRequest(httpRequest));
    }

    private static HttpRequest getHttpRequest(String url, HttpMethod method, Map<String, String> headerMap) {
        return getHttpRequest(url, method, headerMap, null);
    }

    private static HttpRequest getHttpRequest(String url, HttpMethod method, Map<String, String> headerMap, Map<String, String> parameterMap) {
        HttpRequest httpRequest = null;
        switch (method) {
            case POST:
                httpRequest = HttpRequest.post(url);
                break;
            case GET:
            default:
                httpRequest = HttpRequest.get(url);
        }

//        httpRequest.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        httpRequest.header("Accept-Encoding", "gzip, deflate, sdch");
//        httpRequest.header("Accept-Language", "zh-CN,zh;q=0.8");
//        httpRequest.header("Cache-Control", "max-age=0");
//        httpRequest.header("Cookie", "zuserId=fcca7ff1-e9b1-48c8-98f0-b10bff9d86a5; unbxd.userId=uid-1456105305087-86761; __gads=ID=28fe8c2fc0841f84:T=1456105317:S=ALNI_MaTK5NkGY28KIs1evBRGElO5yPPng; AMCV_cluesnetwork%40AdobeOrg=136688995%7CMCMID%7C48501495318714371135635773492630309644%7CMCAID%7CNONE; _bs=2fbc5e3a-3212-0cdf-aa01-d3985bb17172; _vis_opt_exp_302_exclude=1; _vis_opt_exp_75_exclude=1; _vis_opt_exp_83_exclude=1; _vwo_uuid=D071C5A04CEFDD69DDEE0C457C75D3C5; _vis_opt_exp_88_combi=1; _vis_opt_exp_89_exclude=1; _vis_opt_exp_88_goal_1=1; _vis_opt_exp_85_exclude=1; _vis_opt_exp_86_exclude=1; _vis_opt_exp_304_combi=3; _vis_opt_exp_304_goal_1=1; _vis_opt_exp_92_combi=1; _vis_opt_exp_92_goal_1=1; _vis_opt_exp_352_exclude=1; _vis_opt_exp_344_exclude=1; _vis_opt_exp_362_combi=1; _vis_opt_exp_362_goal_1=1; __utma=187306090.1284379102.1456105304.1458023655.1458023655.1; __utmz=187306090.1458023655.1.1.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); _vis_opt_exp_365_exclude=1; _vis_opt_exp_367_exclude=1; _vis_opt_exp_377_exclude=1; _vis_opt_exp_379_exclude=1; _vis_opt_exp_378_exclude=1; _vis_opt_exp_372_exclude=1; _vis_opt_exp_385_exclude=1; _vis_opt_exp_393_exclude=1; _vis_opt_exp_141_exclude=1; _vis_opt_exp_407_exclude=1; sc_loc=2222; sc_loc_variable=2222%7Cwest%7CISP; _vis_opt_exp_420_exclude=1; _vis_opt_exp_417_exclude=1; Persistent_id_LbuGMDtWnw2GHK=0D3E426F420C:1459843598; utm_source=mysmartprice; utm_medium=CPS; _vis_opt_exp_443_exclude=1; _vis_opt_exp_445_exclude=1; AMCV_20CC138653C6496B0A490D45%40AdobeOrg=T; sess_id1=eyIyMzg4Njg3MTE1Ijp7InByb2R1Y3RfaWQiOiI4NzI5NDI1NiIsInByb2R1Y3Rfb3B0aW9ucyI6W10sImFtb3VudCI6MSwicHJpY2UiOjEzOTksIm5hbWUiOiJQYW5kYSBCbHVlIFdhbGtlciBGb3IgQmFieSJ9LCIyNjc5NjIzMzUyIjp7InByb2R1Y3RfaWQiOiI4MDUzMzY0NiIsInByb2R1Y3Rfb3B0aW9ucyI6W10sImFtb3VudCI6MiwicHJpY2UiOjIyOTAsIm5hbWUiOiJDdWJpdCAtIFRvcGF6IDIifX0%3D; _vis_opt_s=NaN%7C; _vis_opt_test_cookie=1; s_cm=undefinedTyped%2FBookmarkedTyped%2FBookmarkedundefined; s_cm7=undefinedTyped%2FBookmarkedTyped%2FBookmarkedundefined; _gat=1; unbxd.visit=repeat; dfpLMC=Mobiles%20Tablets; dfpLSC=Mobile%20Phones; dfpLLC=Android%20Phones; dfpLPID=4159472; _vwo_uuid_v2=051A4C0C45E851E6D0B90BF3A4AEAE26|49b103e50f3277562346a866c99b7974; s_fid=5D6BEA0318DE429F-3861D7D2536A46DD; _ga=GA1.2.1284379102.1456105304; mbox=PC#1459418912394-424448.22_12#1462433789|session#1461223753787-684854#1461226049|check#true#1461224249; zsessionId=1e6c84c3-66b1-49cc-968e-5c9b68caab58; s_adserv=clues-dev; source_7d=mysmartprice; s_direct=1; s_direct7=1; s_nr=1461224190798-Repeat; gpv=Home%3ASearch%20results; s_ppn=Home%3ASearch%20results; s_sq=%5B%5BB%5D%5D; s_cc=true; pv=15; ts=15538; s_ppvl=Home%253ASearch%2520results%2C15%2C15%2C415%2C1918%2C415%2C1920%2C1080%2C1%2CP; s_ppv=Home%253AMobiles%2520%2526%2520Tablets%253AMobile%2520Phones%253AAndroid%2520Phones%253ASpice%2520Xlife%2520512%2C18%2C18%2C979%2C1918%2C979%2C1920%2C1080%2C1%2CP");
//        httpRequest.header("Host", "search.shopclues.com");
//        httpRequest.header("Proxy-Connection", "keep-alive");
//        httpRequest.header("Referer", url);
//        httpRequest.header("Upgrade-Insecure-Requests", "1");
//        httpRequest.header("Content-Type", "text/html;charset=UTF-8");

//        httpRequest.header("User-Agent", UserAgentHelper.get(-1));
//        httpRequest.header("User-Agent", UserAgentHelper.get(30));
        httpRequest.header("User-Agent", UserAgentHelper.get(-1));
        if (headerMap != null) {
            for (Map.Entry<String, String> kv : headerMap.entrySet()) {
                httpRequest.header(kv.getKey(), kv.getValue());
            }
        }
        if (parameterMap != null) {
            httpRequest.query(parameterMap);
        }

        return httpRequest;
    }

    private static HttpResponseModel getResponseModel(HttpResponse httpResponse) {

        String location = httpResponse.header("location");
        //todo 拼接location，有的返回没有http://www.xxxxxxx.com/

//        httpResponse.unzip();
        httpResponse.charset("utf-8");
        return new HttpResponseModel(httpResponse.statusCode(),
                httpResponse.contentType(),
                httpResponse.charset(),
                httpResponse.bodyBytes(),
                httpResponse.bodyText(),
                location
        );
    }

    private static HttpResponseModel getResponseModelAmazon(HttpResponse httpResponse) {
        String location = httpResponse.header("location");

        httpResponse.unzip();
        httpResponse.charset("utf-8");
        return new HttpResponseModel(httpResponse.statusCode(),
                httpResponse.contentType(),
                httpResponse.charset(),
                httpResponse.bodyBytes(),
                httpResponse.bodyText(),
                location
        );
    }

    public static boolean getImage(String url, File file) {
        HttpResponseModel hrm = get(url, null);
        String contentType = hrm.getContentType();

        if (contentType != null && (contentType.toLowerCase().contains("image/") ||
                contentType.toLowerCase().contains("jpg") ||
                contentType.toLowerCase().contains("png") ||
                contentType.toLowerCase().contains("gif"))) {

            try {
                FileUtils.writeByteArrayToFile(file, hrm.getBodyBytes());
            } catch (IOException e) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public static HttpResponseModel uploadFile(String serverUri, File file) {

        HttpRequest httpRequest = HttpRequest.post(serverUri).form("file", file);

        HttpResponse response = httpRequest.send();

        return getResponseModel(response);
    }

}
