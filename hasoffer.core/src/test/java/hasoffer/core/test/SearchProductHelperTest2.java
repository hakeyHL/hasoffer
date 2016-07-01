package hasoffer.core.test;

import hasoffer.base.model.Website;
import hasoffer.core.bo.product.SearchedSku;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductService;
import hasoffer.core.task.ListAndProcessTask;
import hasoffer.core.task.worker.IProcess;
import hasoffer.fetch.model.ProductStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created on 2016/4/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class SearchProductHelperTest2 {

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;
    @Resource
    ISearchService searchService;
    @Resource
    SearchProductService searchProductService;
    private Logger logger = LoggerFactory.getLogger(SearchProductHelperTest2.class);

    @Test
    public void testFetch() {
        String sql = "SELECT t FROM SrmSearchLog t WHERE t.site='FLIPKART' AND t.precise<>'MANUALSET'";

        ListAndProcessTask task = new ListAndProcessTask(dbm);
        task.go(sql, new IProcess<SrmSearchLog>() {
            @Override
            public void process(SrmSearchLog searchLog) {
                try {
                    SrmAutoSearchResult autoSearchResult = new SrmAutoSearchResult(searchLog);

                    autoSearchResult.setRelatedProId(0L);

                    searchProductService.searchProductsFromSites(autoSearchResult);
                    searchService.relateUnmatchedSearchLogx(autoSearchResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Test
    public void saveTest2() {
        try {
            String logId = "4a0aa34fd0fcd740f4720a9be98cf83c";

            SrmAutoSearchResult autoSearchResult = mdm.queryOne(SrmAutoSearchResult.class, logId);
            boolean isCleaned = true;// searchProductService.analysisProducts(autoSearchResult);
            if (isCleaned) {
                searchService.relateUnmatchedSearchLogx(autoSearchResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void show(Map<Website, List<SearchedSku>> listMap) {
        for (Map.Entry<Website, List<SearchedSku>> kv : listMap.entrySet()) {
            Website website = kv.getKey();
            List<SearchedSku> products = kv.getValue();
            System.out.println("--------------------------" + website + "--------------------------");
            for (SearchedSku ss : products) {
                System.out.println(ss.getTitle() + "\t" + ss.getUrl());
            }
        }
    }

    @Test
    public void compareTest() {
        List<SearchedSku> searchedSkus = new ArrayList<SearchedSku>();
        searchedSkus.add(new SearchedSku(Website.AMAZON, "111", 0.5f, 200, 0.6f, "1", "", "", ProductStatus.ONSALE));
        searchedSkus.add(new SearchedSku(Website.AMAZON, "222", 0.4f, 200, 0.1f, "1", "", "", ProductStatus.ONSALE));
        searchedSkus.add(new SearchedSku(Website.AMAZON, "333", 0.4f, 200, 0.8f, "1", "", "", ProductStatus.ONSALE));

        Comparator comparator = new Comparator<SearchedSku>() {
            @Override
            public int compare(SearchedSku p1, SearchedSku p2) {
                float score1 = p1.getTitleScore();
                float score2 = p2.getTitleScore();

                if (score1 > score2) {
                    return -1;
                } else if (score1 < score2) {
                    return 1;
                } else if (score1 == score2) {
                    float priceScore1 = p1.getPriceScore();
                    float priceScore2 = p2.getPriceScore();

                    if (priceScore1 == 0 && priceScore2 == 0) {
                        return 0;
                    } else {
                        if (priceScore1 > priceScore2) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
                return 0;
            }
        };

        Collections.sort(searchedSkus, comparator);

        for (SearchedSku sku : searchedSkus) {
            System.out.println(sku.getTitle() + "\t" + sku.getTitleScore() + "\t" + sku.getPriceScore());
        }
    }

}
