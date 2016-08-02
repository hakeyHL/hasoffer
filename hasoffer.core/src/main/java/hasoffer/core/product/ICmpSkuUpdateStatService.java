package hasoffer.core.product;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.log.SkuUpdateLog;

/**
 * Created on 2016/5/31.
 */
public interface ICmpSkuUpdateStatService {

    //用于访问更新策略
    SkuUpdateLog findSkuUpdateLog(String id);

    void saveOrAddNeedUpdateAmount(String id, Website website, long amount);

    void saveOrUpdateSkuUpdateSuccessAmount(String id, Website website, long amount);
}
