package hasoffer.core.product;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.po.ptm.PtmStdSku;

/**
 * Created by hs on 2016年11月28日.
 * Time 17:20
 */
public interface IPtmStdSkuService {
    PtmStdSku getStdSkuById(Long id);

    PageableResult<PtmStdSku> getPtmStdSkuListByMinId(Long minId, int page, int pageSize);

    void importPtmStdSku2Solr(PtmStdSku ptmStdSku);
}
