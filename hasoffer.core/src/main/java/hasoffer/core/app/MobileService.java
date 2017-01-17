package hasoffer.core.app;

import hasoffer.core.app.vo.CmpProductListVo;
import hasoffer.core.app.vo.mobile.KeyWordsVo;
import hasoffer.core.persistence.po.h5.KeywordCollection;

import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2017年01月05日.
 * Time 17:18
 */
public interface MobileService {
    List<KeyWordsVo> getKeyWordsListFromRepo(KeyWordsVo keyWordsVo, int page, int pageSize);

    List<CmpProductListVo> searchFromSolrByKeyWordVo(KeyWordsVo keyWordsVo, int page, int pageSize);

    Map<String, List<CmpProductListVo>> getSimilarCategorys(KeyWordsVo keyWordsVo, int size);

    void updateKeyResultCount(KeywordCollection keywordCollection);
}
