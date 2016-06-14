package hasoffer.base.test;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.HtmlUtils;
import hasoffer.base.utils.http.HttpUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/19
 */
public class HttpTest {

	private static final Pattern BRAND_PATTERN = Pattern.compile("[a-zA-Z\\s]{2,}");
	private static Logger logger = LoggerFactory.getLogger(HttpTest.class);

	@Test
	public void testRequest() {

		HttpResponseModel httpResponseModel = HtmlUtils.getResponse("https://www.allbuy.com", 3);

		logger.debug(String.format(httpResponseModel.getStatusCode() + ""));
	}

	@Test
	public void f_1() {
		String[] ts = {
				"JJRC", "sadk/ljl", "ss", "KLKLJssdsaf", "234", "lkajslkdjf4asd", "ttt5", "dd", "Y", "Li"
		};
		for (String t : ts) {
			Matcher matcher = BRAND_PATTERN.matcher(t);
			System.out.println(t + " : " + matcher.matches());
		}
	}

	@Test
	public void f_2() {
		int statusCode = 403;
		switch (statusCode) {
			case 200:
			case 403:
			case 404:
				System.out.println("111");
				return;
			default:
				System.out.println("222");
		}
	}

	@Test
	public void test3(){



		String url = "http://www.flipkart.com/celkon-a43/p/itme6gfjxsxkhepq?pid=itmebyzf6xvehsth";

		HttpResponseModel responseModel = HttpUtils.get(url, null);

		String redirect = responseModel.getRedirect();

		System.out.println(redirect);

	}

	@Test
	public void test4(){

		String requestUrl = "http://developer.shopclues.com/api/v1/product/74654614";

		Map<String,String> headerMap = new HashMap<String, String>();
		headerMap.put("Authorization", "Bearer bc1f461de4f193");
		headerMap.put("Content-Type", "application/json");

		HttpResponseModel httpResponseModel = HttpUtils.get(requestUrl, headerMap);

		String bodyString = httpResponseModel.getBodyString();

		System.out.println(bodyString);
	}
}
