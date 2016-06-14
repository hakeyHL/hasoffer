package hasoffer.fetch.test.sites;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.fetch.core.ICategoryProcessor;
import hasoffer.fetch.core.IProductProcessor;
import hasoffer.fetch.core.ISummaryProductProcessor;
import hasoffer.fetch.model.Product;
import hasoffer.fetch.model.FetchedProduct;
import hasoffer.fetch.sites.amazon.AmazonCategoryProcessor;
import hasoffer.fetch.sites.amazon.AmazonProductProcessor;
import hasoffer.fetch.sites.amazon.AmazonSummaryProductProcessor;
import org.apache.http.conn.ConnectTimeoutException;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/19
 */
public class AmazonProcessorTest {

	private Logger logger = LoggerFactory.getLogger(AmazonProcessorTest.class);

	@Test
	public void f2() throws Exception {
		String url = "http://www.amazon.in/Reebok-Cotton-Sweatshirts-4055008581583_B89946_A-L_Blue/dp/B017D3KB32";
		ISummaryProductProcessor productProcessor = new AmazonSummaryProductProcessor();
		FetchedProduct fetchedProduct = productProcessor.getSummaryProductByUrl(url);
		System.out.println(fetchedProduct.getPrice());
	}

	@Test
	public void f() {
		String url = "http://www.amazon.in/gp/offer-listing/B00PY5SYB8/";
		IProductProcessor productProcessor = new AmazonProductProcessor();

		try {
			Product product = productProcessor.parseProduct(url);

			logger.debug(product.toString());

		} catch (ContentParseException e) {
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (HttpFetchException e) {
			e.printStackTrace();
		} catch (XPatherException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCategoryProcessor() {
		ICategoryProcessor categoryProcessor = new AmazonCategoryProcessor();
		try {
			categoryProcessor.parseCategories();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
