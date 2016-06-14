package hasoffer.base.test;


import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.base.utils.http.UserAgentHelper;

public class HttpUtilsTest {

    public static void main(String[] args) {
        for (int i = 0; i < 31; i++) {
            String url = "http://www.snapdeal.com/product/wynncom-g32-512-mb-black/677056329309";
            HttpResponseModel responseModel = HttpUtils.get(url, null);

            System.out.println(UserAgentHelper.index + " - " + responseModel.getStatusCode());
        }
    }


}
