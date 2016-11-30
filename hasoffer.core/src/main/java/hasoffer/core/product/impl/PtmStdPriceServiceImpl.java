package hasoffer.core.product.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.SkuStatus;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmStdPrice;
import hasoffer.core.product.IPtmStdPriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hs on 2016年11月29日.
 * Time 10:50
 */
@Service
public class PtmStdPriceServiceImpl implements IPtmStdPriceService {
    private static final String API_PTMSTDPRICE_GET_PRICELIST_BY_SKUID = "SELECT t  from PtmStdPrice t where t.stdSkuId=?0 and t.skuStatus=?1";
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(PtmStdPriceServiceImpl.class);

    @Override
    public List<PtmStdPrice> getPtmStdPriceList(Long id, SkuStatus skuStatus) {
        return dbm.query(API_PTMSTDPRICE_GET_PRICELIST_BY_SKUID, Arrays.asList(id, skuStatus));
    }

    @Override
    public PtmStdPrice getPtmStdPriceById(long id) {
        return dbm.get(PtmStdPrice.class, id);
    }

    @Override
    public PageableResult<PtmStdPrice> getPagedPtmStdPriceList(Long id, SkuStatus skuStatus, int page, int pageSize) {
        return dbm.queryPage(API_PTMSTDPRICE_GET_PRICELIST_BY_SKUID, page, pageSize, Arrays.asList(id, skuStatus));
    }
}
