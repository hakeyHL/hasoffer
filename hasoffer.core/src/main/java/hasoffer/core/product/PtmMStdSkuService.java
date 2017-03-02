package hasoffer.core.product;

import hasoffer.core.persistence.po.ptm.PtmMStdSku;

import java.util.List;

/**
 * Created by hs on 2017年03月02日.
 * Time 17:03
 */
public interface PtmMStdSkuService {
    void savePtmMStdSku(List<PtmMStdSku> mStdSkuList);

    Long savePtmMStdSkuSinge(PtmMStdSku ptmMStdSku);
}
