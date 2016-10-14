package hasoffer.core.admin.impl;

import hasoffer.core.admin.IGiftService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.app.HasofferCoinsExchangeGift;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created on 2016/9/29.
 */
@Service
public class GiftServiceImpl implements IGiftService {

    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HasofferCoinsExchangeGift createGift(HasofferCoinsExchangeGift gift) {
        Long aLong = dbm.create(gift);
        gift.setId(aLong);
        return gift;
    }
}
