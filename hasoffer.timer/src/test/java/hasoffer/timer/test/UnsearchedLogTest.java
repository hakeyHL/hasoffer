package hasoffer.timer.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.enums.SearchPrecise;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductHelper;
import hasoffer.fetch.model.ListProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/3/31
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class UnsearchedLogTest {

    private static Logger logger = LoggerFactory.getLogger(UnsearchedLogTest.class);

    @Resource
    ISearchService searchService;

    @Test
    public void t() {

        Date start = TimeUtils.stringToDate("2016-01-01", "yyyy-MM-dd");

        PageableResult<SrmSearchLog> pagedSearchLogs = searchService.listNoresultSearchLogs(SearchPrecise.TIMERSET, start, 1, 100);

        List<SrmSearchLog> searchLogs = pagedSearchLogs.getData();

        for(SrmSearchLog searchLog : searchLogs){
            System.out.println(searchLog.getKeyword());
            process(searchLog);
        }
    }

    private void process(SrmSearchLog searchLog) {
        Map<Website, ListProduct> listProductMap = SearchProductHelper.getProducts(searchLog);
//        searchService.relateUnmatchedSearchLog(searchLog, listProductMap);
        for(Map.Entry<Website, ListProduct> kv : listProductMap.entrySet()){
            System.out.println(kv.getKey() + "\t" + kv.getValue().getTitle());
        }
    }

}
