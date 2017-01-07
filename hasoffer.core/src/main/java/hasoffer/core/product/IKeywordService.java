package hasoffer.core.product;

import hasoffer.core.persistence.po.h5.KeywordCollection;

/**
 * Created on 2017/1/4.
 */
public interface IKeywordService {

    void saveOrUpdate(KeywordCollection keywordCollection);

}


