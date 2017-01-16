package hasoffer.core.product;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.spider.model.FetchedProduct;

import java.util.List;

/**
 * Created by hs on 2016年11月29日.
 * Time 10:49
 */
public interface IPtmStdPriceService {

    List<PtmStdPrice> getPtmStdPriceList(Long id, SkuStatus skuStatus);

    PtmStdPrice getPtmStdPriceById(long id);

    PageableResult<PtmStdPrice> getPagedPtmStdPriceList(Long id, SkuStatus skuStatus, int page, int pageSize);

    PageableResult<PtmStdPrice> getPagedPtmStdPriceByMinId(Long minId, int page, int pageSize);

    List<PtmStdPrice> getPtmstdPriceListByUrlKey(String urlKey);

    void updatePtmStdPriceBySpiderFetchedProduct(long stdPriceId, FetchedProduct fetchedProduct);

    void createPtmStdPriceImage(long stdPriceId, FetchedProduct fetchedProduct);

    //初始化urlKey for ptmstdprice
    void initUrlKey(long stdPriceId);

    void importPtmStdPrice2Solr(PtmStdPrice ptmStdPrice);

    void importPtmStdPrice2Solr(Long priceId);

    void removePriceById(Long priceId);

    void update(PtmStdPrice ptmStdPrice);

    Long create(PtmStdPrice ptmStdPrice);
}
