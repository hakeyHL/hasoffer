package hasoffer.core.app.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.core.app.MobileService;
import hasoffer.core.app.vo.CmpProductListVo;
import hasoffer.core.app.vo.mobile.KeyWordsVo;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.h5.KeywordCollection;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuModel;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.api.ApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by hs on 2017年01月05日.
 * Time 17:18
 */
@Service
public class MobileServiceImpl implements MobileService {
    private static final String API_KEYWORDCOLLECTION_GETBY_KEYWORDSVO = "select t from KeywordCollection t where 1=1 ";
    private static final String API_KEYWORDCOLLECTION_GET_SIMILAR_CATEGORYKEYS = "select t from KeywordCollection t where t.sourceSiteCategoryName=?0";
    @Autowired
    ICacheService iCacheService;
    @Resource
    IDataBaseManager dbm;
    @Resource
    PtmStdSkuIndexServiceImpl ptmStdSkuIndexService;
    @Resource
    ApiUtils apiUtils;

    @Override
    public List<KeyWordsVo> getKeyWordsListFromRepo(KeyWordsVo keyWordsVo, int page, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append(API_KEYWORDCOLLECTION_GETBY_KEYWORDSVO);
        List<KeyWordsVo> keyWordsVoList = new ArrayList<>();
        if (keyWordsVo == null) {
            return keyWordsVoList;
        }
        if (StringUtils.isNotEmpty(keyWordsVo.getName())) {
            sql.append(" and  t.keyword=" + keyWordsVo.getName());
        }

        if (keyWordsVo.getCategoryId() != null && keyWordsVo.getCategoryId() > 0) {
            sql.append(" and  t.categoryid=" + keyWordsVo.getCategoryId());
        }

//        if (keyWordsVo.getId() > 0) {
//            sql.append(" and  t.id=" + keyWordsVo.getId());
//        }

        if (keyWordsVo.getSource() != null) {
            sql.append(" and  t.sourceSiteCategoryName=" + keyWordsVo.getSource().name());
        }

        if (keyWordsVo.getResultCount() != null && keyWordsVo.getResultCount() > 0) {
            sql.append(" order by   t.keywordResult desc ");
        } else {
            sql.append(" order by   t.keywordResult asc ");
        }

        if (keyWordsVo.getWeight() != null && keyWordsVo.getWeight() > 0) {
            sql.append(",t.weight desc ");
        } else {
            sql.append(",t.weight asc ");
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
        //1. 按照关键词从ptmStdSku solr搜索商品列表  暂时只有,后期要优化的话再从ptmProduct中搜索
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setKeyword(keyWordsVo.getName());
        searchCriteria.setPage(page);
        searchCriteria.setPageSize(pageSize);
        PageableResult<PtmStdSkuModel> ptmStdSkuModels = ptmStdSkuIndexService.filterStdSkuOnCategoryByCriteria(searchCriteria);
        apiUtils.addProductVo2List(cmpProductListVoList, ptmStdSkuModels.getData());
        return cmpProductListVoList;
    }

    /**
     * 获取相关关键词列表
     *
     * @param keyWordsVo
     * @return
     */
    @Override
    public Map<String, List<CmpProductListVo>> getSimilarCategorys(KeyWordsVo keyWordsVo) {
        Map<String, List<CmpProductListVo>> similarKeyAndPros = new HashMap<>();
        List<CmpProductListVo> similarProducts = new ArrayList<>();
        if (keyWordsVo == null || StringUtils.isEmpty(keyWordsVo.getCategoryName())) {
            return similarKeyAndPros;
        }
        List<KeywordCollection> keywordCollections = dbm.query(API_KEYWORDCOLLECTION_GET_SIMILAR_CATEGORYKEYS, Arrays.asList(keyWordsVo.getCategoryName()));
        for (KeywordCollection keywordCollection : keywordCollections) {
            if (keywordCollection.getSourceSiteCategoryName().equals(keyWordsVo.getCategoryName()) && similarKeyAndPros.size() < 2) {
                List<CmpProductListVo> cmpProductListVoList = searchFromSolrByKeyWordVo(new KeyWordsVo(keywordCollection), 0, 20);
                similarKeyAndPros.put(keywordCollection.getKeywordKey(), cmpProductListVoList);
            }
        }
        return similarKeyAndPros;
    }
}
