package hasoffer.core.persistence.po.h5;

import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created on 2017/1/4.
 */
@Entity
public class KeywordCollection implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
//    keywordRepository.setId(HexDigestUtil.md5(keyword.toUpperCase() + Website.FLIPKART));
    public String id;//keyword+keywordSourceSite的md5
    public String keyword;//关键字
    public String keywordKey;//关键字md5
    public Website keywordSourceSite;//关键字来源
    public String sourceSiteCategoryName;//来源网站类目名称
    public float weight = 0.6f;//权重   0-1 ex:0.6
    public long categoryid;//hasoffer category id
    public long keywordResult;//关键字结果数量

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(long categoryid) {
        this.categoryid = categoryid;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getKeywordResult() {
        return keywordResult;
    }

    public void setKeywordResult(long keywordResult) {
        this.keywordResult = keywordResult;
    }

    public Website getKeywordSourceSite() {
        return keywordSourceSite;
    }

    public void setKeywordSourceSite(Website keywordSourceSite) {
        this.keywordSourceSite = keywordSourceSite;
    }

    public String getSourceSiteCategoryName() {
        return sourceSiteCategoryName;
    }

    public void setSourceSiteCategoryName(String sourceSiteCategoryName) {
        this.sourceSiteCategoryName = sourceSiteCategoryName;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getKeywordKey() {
        return keywordKey;
    }

    public void setKeywordKey(String keywordKey) {
        this.keywordKey = keywordKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeywordCollection that = (KeywordCollection) o;

        if (Float.compare(that.weight, weight) != 0) return false;
        if (categoryid != that.categoryid) return false;
        if (keywordResult != that.keywordResult) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (keyword != null ? !keyword.equals(that.keyword) : that.keyword != null) return false;
        if (keywordKey != null ? !keywordKey.equals(that.keywordKey) : that.keywordKey != null) return false;
        if (keywordSourceSite != that.keywordSourceSite) return false;
        return !(sourceSiteCategoryName != null ? !sourceSiteCategoryName.equals(that.sourceSiteCategoryName) : that.sourceSiteCategoryName != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (keyword != null ? keyword.hashCode() : 0);
        result = 31 * result + (keywordKey != null ? keywordKey.hashCode() : 0);
        result = 31 * result + (keywordSourceSite != null ? keywordSourceSite.hashCode() : 0);
        result = 31 * result + (sourceSiteCategoryName != null ? sourceSiteCategoryName.hashCode() : 0);
        result = 31 * result + (weight != +0.0f ? Float.floatToIntBits(weight) : 0);
        result = 31 * result + (int) (categoryid ^ (categoryid >>> 32));
        result = 31 * result + (int) (keywordResult ^ (keywordResult >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "KeywordCollection{" +
                "categoryid=" + categoryid +
                ", id='" + id + '\'' +
                ", keyword='" + keyword + '\'' +
                ", keywordKey='" + keywordKey + '\'' +
                ", keywordSourceSite=" + keywordSourceSite +
                ", sourceSiteCategoryName='" + sourceSiteCategoryName + '\'' +
                ", weight=" + weight +
                ", keywordResult=" + keywordResult +
                '}';
    }
}
