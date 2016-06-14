package hasoffer.core.product;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.log.SkuUpdateLog;
import hasoffer.core.persistence.po.stat.StatPtmCmpSkuUpdate;

/**
 * Created on 2016/5/31.
 */
public interface ICmpSkuUpdateStatService {

    //用于全站更新策略，过时的
    StatPtmCmpSkuUpdate findStatPtmCmpSkuUpdateById(String id);

    //用于访问更新策略
    SkuUpdateLog findSkuUpdateLog(String id);

    void saveOrAddNeedUpdateAmount(String id, Website website, long amount);

    void saveOrUpdateSkuUpdateSuccessAmount(String id, Website website, long amount);
}
