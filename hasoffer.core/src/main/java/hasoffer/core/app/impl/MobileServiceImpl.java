package hasoffer.core.app.impl;

import hasoffer.core.app.MobileService;
import hasoffer.core.app.vo.CmpProductListVo;
import hasoffer.core.app.vo.mobile.KeyWordsVo;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.h5.KeywordCollection;
import hasoffer.core.redis.ICacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2017年01月05日.
 * Time 17:18
 */
@Service
public class MobileServiceImpl implements MobileService {
    private static final String API_KEYWORDCOLLECTION_GETBY_KEYWORDSVO = "select t from KeywordCollection t where 1=1 ";
    @Autowired
    ICacheService iCacheService;
    @Resource
    IDataBaseManager dbm;

    @Override
    public List<KeyWordsVo> getKeyWordsListFromRepo(KeyWordsVo keyWordsVo, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(API_KEYWORDCOLLECTION_GETBY_KEYWORDSVO);
        List<KeyWordsVo> keyWordsVoList = new ArrayList<>();
        if (keyWordsVo == null) {
            return keyWordsVoList;
        }
        if (StringUtils.isNotEmpty(keyWordsVo.getName())) {
            sql.append(" and  t.keyword=" + keyWordsVo.getName());
        }

        if (keyWordsVo.getCategoryId() > 0) {
            sql.append(" and  t.categoryid=" + keyWordsVo.getCategoryId());
        }

//        if (keyWordsVo.getId() > 0) {
//            sql.append(" and  t.id=" + keyWordsVo.getId());
//        }

        if (keyWordsVo.getSource() != null) {
            sql.append(" and  t.sourceSiteCategoryName=" + keyWordsVo.getSource().name());
        }

        if (keyWordsVo.getResultCount() > 0) {
            sql.append(" order by   t.keywordResult desc ");
        } else {
            sql.append(" order by   t.keywordResult asc ");
        }

        if (keyWordsVo.getWeight() > 0) {
            sql.append(" order by   t.weight desc ");
        } else {
            sql.append(" order by   t.weight asc ");
        }
        List<KeywordCollection> keywordCollections = dbm.query(sql.toString());
        for (KeywordCollection keywordCollection : keywordCollections) {
            keyWordsVoList.add(new KeyWordsVo(keywordCollection));
        }
        return keyWordsVoList;
    }

    @Override
    public List<CmpProductListVo> searchFromSolrByKeyWordVo(KeyWordsVo keyWordsVo, int page, int pageSize) {
        List<CmpProductListVo> cmpProductListVoList = new ArrayList<>();
        return cmpProductListVoList;
    }
}
