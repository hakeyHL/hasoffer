package hasoffer.core.product.impl;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.mexico.MexicoAppDeal;
import hasoffer.core.product.IMexicoAppdealService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by wing on 2017/1/12.
 */
@Service
public class MexicoAppdealServiceImpl implements IMexicoAppdealService {

    @Resource
    IDataBaseManager dbm;

    @Override
    public void createMexicoAppdeal(MexicoAppDeal mexicoAppDeal) {
        dbm.create(mexicoAppDeal);
    }
}
