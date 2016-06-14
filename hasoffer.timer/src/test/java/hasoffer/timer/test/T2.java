package hasoffer.timer.test;

/**
 * Date : 2016/3/24
 * Function :
 */

import com.alibaba.fastjson.JSON;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.http.HttpUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2016/3/23.
 */
public class T2 {

    public static void main(String[] strings) {
        String json = JSON.toJSONString(new TO());

        Map<String, String> headerMap = new HashMap<String, String>();

        String url = "http://api.getvoodoo.in/rest/sherlock/similar-products";

        System.out.println(json);
        System.out.println(json.length());
        String key = a(a(a(String.valueOf(json.length()))));
        System.out.println(key);

        headerMap.put("key", key);
        headerMap.put("user-id", "b595aca2c906f99");
        headerMap.put("voodoo-version-code", "134");
        headerMap.put("voodoo-version-name", " 0.0.13");
        headerMap.put("User-Agent", "okhttp/2.4.0");
        headerMap.put("Accept-Encoding", "gzip");

        Map<String, Object> form = new HashMap<String, Object>();
        form.put("", json);

        HttpResponseModel responseModel = HttpUtils.post(url, form, headerMap);

        System.out.println(responseModel.getBodyString());
    }

    public static String a(String paramString) {
        try {
            String str;
            for (paramString = new BigInteger(1, MessageDigest.getInstance("MD5").digest(paramString.getBytes())).toString(16); ; paramString = "0" + paramString) {
                str = paramString;
                if (paramString.length() >= 32) {
                    break;
                }
            }
            return str;
        } catch (NoSuchAlgorithmException s) {
            return null;
        }
    }
}

class TO {
    boolean cache = true;
    String merchant = "snapdeal";
    String currentPrice = "Rs. 13,590";
    String originalPrice = "Rs. 13,590";
    String pid = "";
    String title = "UNBOXED OnePlus X (16 GB-Onyx)";

    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
    /*public static void main(String[] strings) {
        String json="{\"cache\":true,\"currentPrice\":\"Rs. 13,590\",\"merchant\":\"snapdeal\",\"originalPrice\":\"Rs. 13,590\",\"pid\":\"\",\"title\":\"UNBOXED OnePlus X (16 GB-Onyx)\"}";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://api.getvoodoo.in/rest/sherlock/similar-products")
                .post(body)
                .addHeader("key", a(a(a(String.valueOf(143)))))
                .addHeader("user-id", "b595aca2c906f99")
                .addHeader("voodoo-version-code","134")
                .addHeader("voodoo-version-name", " 0.0.13").build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                System.out.print(response.body().string());
            } else {
                System.out.print(response+"error");
            }
        }catch (Exception e){
            System.out.print(e.getLocalizedMessage());
        }
    }

    public static String a(String paramString) {
        try {
            String str;
            for (paramString = new BigInteger(1, MessageDigest.getInstance("MD5").digest(paramString.getBytes())).toString(16); ; paramString = "0" + paramString) {
                str = paramString;
                if (paramString.length() >= 32) {
                    break;
                }
            }
            return str;
        } catch (NoSuchAlgorithmException s) {
            return null;
        }
    }*/
