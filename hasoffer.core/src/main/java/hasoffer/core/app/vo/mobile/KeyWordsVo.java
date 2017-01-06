package hasoffer.core.app.vo.mobile;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.po.h5.KeywordCollection;

/**
 * Created by hs on 2017年01月05日.
 * Time 17:21
 */
public class KeyWordsVo {
    private String id;
    private String name;
    private Website source;
    private Long categoryId;
    private Float weight;
    private Long resultCount;

    public KeyWordsVo() {
    }

    public KeyWordsVo(KeywordCollection keywordCollection) {
        this.id = keywordCollection.getId();
        this.name = keywordCollection.getKeyword();
        this.source = keywordCollection.getKeywordSourceSite();
        this.categoryId = keywordCollection.getCategoryid();
        this.weight = keywordCollection.getWeight();
        this.resultCount = keywordCollection.getKeywordResult();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Website getSource() {
        return source;
    }

    public void setSource(Website source) {
        this.source = source;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Long getResultCount() {
        return resultCount;
    }

    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }
}
