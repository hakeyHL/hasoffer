package hasoffer.core.product;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.mongo.SummaryProduct;

/**
 * Created on 2016/5/5.
 */
public interface ISummaryProductService {

    PageableResult<SummaryProduct> getPagedSummaryProductByTime(long startLongTime, boolean gt, int sort, int page, int size);

    void updateCmpSkuByFetchResult(SummaryProduct summaryProduct,long id);

}
