package hasoffer.core.test;

import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.SearchProductHelper;
import hasoffer.fetch.core.IListProcessor;
import hasoffer.fetch.helper.WebsiteProcessorFactory;
import hasoffer.fetch.model.ListProduct;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created on 2016/4/1.
 */
public class SearchProductHelperTest {

    private Logger logger = LoggerFactory.getLogger(SearchProductHelperTest.class);

    @Test
    public void testSearchProductHelper() {
        String title = "TP-LINK TD-W8968 300Mbps Wireless N USB ADSL2  Modem Router";

        SrmSearchLog searchLog = new SrmSearchLog();
        searchLog.setKeyword(title);

        Map<Website, ListProduct> products = SearchProductHelper.getProducts(searchLog);

        for (Map.Entry<Website, ListProduct> proKV : products.entrySet()) {
            System.out.println(proKV.getKey() + "\t" + proKV.getValue().getTitle() + "\t" + proKV.getValue().getPrice() + "\t" + proKV.getValue().getUrl());
        }

        System.out.println(title);
    }

    @Test
    public void testSearch(){

        Website website = Website.SNAPDEAL;
        String title = "Samsung Galaxy";

        IListProcessor listProcessor = WebsiteProcessorFactory.getListProcessor(website);

        List<ListProduct> listProducts = null;
        try {
            listProducts = listProcessor.getProductSetByKeyword(title, 5);

            if (ArrayUtils.isNullOrEmpty(listProducts)) {
                logger.debug("no results");
            }
        } catch (Exception e) {
            logger.error(String.format("error : search [%s] from [%s].Info : [%s]", title, website, e.getMessage()));
        }
    }

}
