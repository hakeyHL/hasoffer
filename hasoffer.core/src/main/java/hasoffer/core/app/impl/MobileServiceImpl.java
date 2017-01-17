package hasoffer.core.app.impl;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.core.app.MobileService;
import hasoffer.core.app.vo.CmpProductListVo;
import hasoffer.core.app.vo.mobile.KeyWordsVo;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.h5.KeywordCollection;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.PtmStdSkuIndexServiceImpl;
import hasoffer.core.redis.ICacheService;
import hasoffer.core.utils.api.ApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Resource
    ProductIndex2ServiceImpl productIndex2Service;

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
        PageableResult pModels = ptmStdSkuIndexService.filterStdSkuOnCategoryByCriteria(searchCriteria);
        if (pModels == null || pModels.getData().size() < 1) {
            //从product中搜索
            pModels = productIndex2Service.searchProducts(searchCriteria);
        }
        apiUtils.addProductVo2List(cmpProductListVoList, pModels.getData());
        ///keyword+keywordSourceSite的md5
        //HexDigestUtil.md5(keyword.toUpperCase() + Website.SNAPDEAL)
        String keyId = HexDigestUtil.md5(keyWordsVo.getName().toUpperCase() + keyWordsVo.getSource());
        KeywordCollection keywordCollection = dbm.get(KeywordCollection.class, keyId);
        if (keywordCollection != null) {
            if (pModels.getNumFund() > 0 && keywordCollection.getKeywordResult() != pModels.getNumFund()) {
                keywordCollection.setKeywordResult(pModels.getNumFund());
                updateKeyResultCount(keywordCollection);
            }
        }
        return cmpProductListVoList;
    }

    /**
     * 获取相关关键词列表
     *
     * @param keyWordsVo
     * @return
     */
    @Override
    public Map<String, List<CmpProductListVo>> getSimilarCategorys(KeyWordsVo keyWordsVo, int size) {
        if (size < 1) {
            size = 2;
        }
        Map<String, List<CmpProductListVo>> similarKeyAndPros = new HashMap<>();
        if (keyWordsVo == null || StringUtils.isEmpty(keyWordsVo.getCategoryName())) {
            return similarKeyAndPros;
        }
        List<KeywordCollection> keywordCollections = dbm.query(API_KEYWORDCOLLECTION_GET_SIMILAR_CATEGORYKEYS, Arrays.asList(keyWordsVo.getCategoryName()));
        if (keywordCollections == null || keywordCollections.size() < 1) {
            return similarKeyAndPros;
        }
        //排序
        Collections.sort(keywordCollections, new Comparator<KeywordCollection>() {
            @Override
            public int compare(KeywordCollection o1, KeywordCollection o2) {
                if (o1.getKeyword().compareToIgnoreCase(o2.getKeyword()) < 0) {
                    return -1;
                }
                if (o1.getKeyword().compareToIgnoreCase(o2.getKeyword()) > 0) {
                    return 1;
                }
                return 0;
            }
        });
        setSimilarCategory(similarKeyAndPros, keywordCollections, size, keyWordsVo.getCategoryName(), keyWordsVo.getName());
        return similarKeyAndPros;
    }

    @Transactional
    @Override
    public void updateKeyResultCount(KeywordCollection keywordCollection) {
        dbm.update(keywordCollection);
    }

    /**
     * 循环将指定size的推荐列表放入到map中
     *
     * @param desMap
     * @param keywordCollections
     * @param size
     * @param categoryName
     */
    private void setSimilarCategory(Map desMap, List<KeywordCollection> keywordCollections, int size, String categoryName, String keyword) {
        KeywordCollection keywordCollection;
        while (desMap.size() != size) {
            int nextCollectionIndex = getNextCollectionIndex(keywordCollections, categoryName, keyword);
            keywordCollection = keywordCollections.get(nextCollectionIndex);
            categoryName = keywordCollection.getSourceSiteCategoryName();
            keyword = keywordCollection.getKeyword();
            List<CmpProductListVo> cmpProductListVoList = searchFromSolrByKeyWordVo(new KeyWordsVo(keywordCollection), 0, 20);
            desMap.put(keywordCollection.getKeyword(), cmpProductListVoList);
        }
    }

    private int getNextCollectionIndex(List<KeywordCollection> keywordCollections, String categoryName, String keyword) {
        //1. 获得开始填入的位置+1
        int index = 0;
        for (KeywordCollection keywordCollection : keywordCollections) {
            if (keywordCollection.getSourceSiteCategoryName().equals(categoryName)) {
                if (keyword.equals(keywordCollection.getKeyword())) {
                    //找到这个
                    index = keywordCollections.indexOf(keywordCollection) + 1;
                    if (index >= keywordCollections.size()) {
                        index = 0;
                    }
                }
            }
        }
        return index;
    }
}
