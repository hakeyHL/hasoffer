package hasoffer.core.product.impl;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmMStdSku;
import hasoffer.core.product.PtmMStdSkuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by hs on 2017年03月02日.
 * Time 17:03
 */
@Service
public class PtmMStdSkuServiceImpl implements PtmMStdSkuService {
    @Resource
    IDataBaseManager dbm;


    @Transactional
    @Override
    public void savePtmMStdSku(List<PtmMStdSku> mStdSkuList) {
        for (PtmMStdSku ptmMStdSku : mStdSkuList) {
            dbm.create(ptmMStdSku);
        }
    }

    @Transactional
    @Override
    public Long savePtmMStdSkuSinge(PtmMStdSku ptmMStdSku) {
        return dbm.create(ptmMStdSku);
    }
}
