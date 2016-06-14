package hasoffer.timer.test;

import hasoffer.base.exception.ContentParseException;
import hasoffer.base.exception.HttpFetchException;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.thd.IThdService;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.helper.WebsiteHelper;
import hasoffer.fetch.helper.WebsiteProcessorFactory;
import hasoffer.fetch.model.ListProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created on 2016/3/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ThdFetchTest {

    private static Logger logger = LoggerFactory.getLogger(ThdFetchTest.class);

    @Resource
    IThdService thdService;
    @Resource
    IDataBaseManager dbm;

    @Test
    public void testThdFetch() throws HttpFetchException, ContentParseException {

        String urlTemplate = "http://www.shopclues.com/index.php?dispatch=categories.view&category_id=6072&isis=1&page=2&undefined";

        Website webSite = WebsiteHelper.getWebSite(urlTemplate);
        IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(webSite);

        List<ListProduct> products = listProcessor.getProductByAjaxUrl(urlTemplate,666L);

        for (ListProduct product:products){
            System.out.println(product);
        }

        System.out.println("over");
    }

    @Test
    public void testThdFetch1(){

//        ThdProductFetchByAjaxUrlTask task = new ThdProductFetchByAjaxUrlTask();
//        task.dbm = this.dbm;
//        task.thdService = this.thdService;
//        task.fetchFlipkartProductByAjaxUrl();

    }
}
