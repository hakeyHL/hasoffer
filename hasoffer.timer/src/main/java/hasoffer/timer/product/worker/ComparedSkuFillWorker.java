package hasoffer.timer.product.worker;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.product.IProductService;
import hasoffer.core.search.ISearchService;
import hasoffer.core.search.SearchProductHelper;
import hasoffer.fetch.model.ListProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/3/14
 * Function :
 */
public class ComparedSkuFillWorker implements Runnable {
    private static final String Q_SEARCHLOG =
            " SELECT t FROM SrmSearchLog t " +
                    " WHERE t.ptmProductId > 0 " +
                    " ORDER BY t.createTime ASC ";
    IProductService productService;
    ISearchService searchService;
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(ComparedSkuFillWorker.class);

    public ComparedSkuFillWorker(IProductService productService, ISearchService searchService) {
        this.productService = productService;
        this.searchService = searchService;
    }

    public void run() {
        logger.debug("ComparedSkuFillWorker START");

        int page = 1, size = 100;

        while (true) {
            PageableResult<SrmSearchLog> pagedSearchLog = dbm.queryPage(Q_SEARCHLOG, page, size);

            List<SrmSearchLog> searchLogs = pagedSearchLog.getData();

            if (ArrayUtils.hasObjs(searchLogs)) {
                for (SrmSearchLog log : searchLogs) {

                    String keyword = log.getKeyword();
                    logger.debug("ComparedSkuFillWorker " + keyword);

                    try {
                        Map<Website, ListProduct> listProductMap = SearchProductHelper.getProducts(log);
                        searchService.relateUnmatchedSearchLog(log, listProductMap);
                    } catch (Exception e) {
                        continue;
                    }
                }
            } else {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    System.out.println("update job has no jobs. go to sleep!");
                } catch (InterruptedException e) {
                    return;
                }
                continue;
            }

        }
    }

}
