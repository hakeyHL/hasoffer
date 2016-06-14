package hasoffer.core.product.iml;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.log.SkuUpdateLog;
import hasoffer.core.persistence.po.log.updater.SkuUpdateLogUpdater;
import hasoffer.core.persistence.po.stat.StatPtmCmpSkuUpdate;
import hasoffer.core.product.ICmpSkuUpdateStatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created on 2016/5/31.
 */
@Service
public class CmpSkuUpdateStatServiceImpl implements ICmpSkuUpdateStatService {

    @Resource
    IDataBaseManager dbm;

    @Override
    public StatPtmCmpSkuUpdate findStatPtmCmpSkuUpdateById(String id) {
        return dbm.get(StatPtmCmpSkuUpdate.class, id);
    }

    @Override
    public SkuUpdateLog findSkuUpdateLog(String id) {
        return dbm.get(SkuUpdateLog.class, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrAddNeedUpdateAmount(String id, Website website, long amount) {

        SkuUpdateLog log = findSkuUpdateLog(id);

        if (log == null) {

            SkuUpdateLog newLog = new SkuUpdateLog(id, amount, 0, website);
            newLog.setCreateTime(TimeUtils.toDate(TimeUtils.today()));
            dbm.create(newLog);
        } else {

            SkuUpdateLogUpdater updater = new SkuUpdateLogUpdater(id);

            updater.getPo().setNeedUpdateAmount(log.getNeedUpdateAmount() + amount);
            updater.getPo().setUpdateTime(TimeUtils.nowDate());

            dbm.update(updater);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateSkuUpdateSuccessAmount(String id, Website website, long amount) {

        SkuUpdateLog log = findSkuUpdateLog(id);

        if (log == null) {

            log = new SkuUpdateLog();
            log.setId(id);
            log.setUpdateSuccessAmount(amount);
            log.setWebsite(website);
            log.setCreateTime(TimeUtils.toDate(TimeUtils.today()));

            dbm.create(log);
        } else {

            SkuUpdateLogUpdater updater = new SkuUpdateLogUpdater(id);

            updater.getPo().setUpdateSuccessAmount(amount);
            updater.getPo().setUpdateTime(TimeUtils.nowDate());

            dbm.update(updater);
        }
    }
}
