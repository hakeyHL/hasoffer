package hasoffer.core.product.impl;

import hasoffer.base.utils.StringUtils;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.h5.KeywordCollection;
import hasoffer.core.persistence.po.h5.updater.KeywordCollectionUpdater;
import hasoffer.core.product.IKeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * Created on 2017/1/4.
 */
@Service
public class KeywordServiceImpl implements IKeywordService {

    @Resource
    IDataBaseManager dbm;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(KeywordCollection keywordCollection) {
        if (keywordCollection == null || StringUtils.isEmpty(keywordCollection.getId())) {
            return;
        }

        KeywordCollection oldKeywordCollection = dbm.get(KeywordCollection.class, keywordCollection.getId());

        if (oldKeywordCollection == null) {
            dbm.create(keywordCollection);
        } else {
            KeywordCollectionUpdater updater = new KeywordCollectionUpdater(keywordCollection.getId());

            updater.getPo().setSourceSiteCategoryName(keywordCollection.getSourceSiteCategoryName());

            dbm.update(updater);
        }
    }
}
