package hasoffer.core.product.impl;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmMStdProduct;
import hasoffer.core.product.PtmMStdProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created by hs on 2017年03月02日.
 * Time 18:02
 */
@Service
public class PtmMStdProductServiceImpl implements PtmMStdProductService {
    @Resource
    IDataBaseManager dbm;

    @Transactional
    @Override
    public Long savePtmMStdProduct(PtmMStdProduct ptmMStdProduct) {
        return dbm.create(ptmMStdProduct);
    }
}
