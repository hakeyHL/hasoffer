package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by hs on 2017年03月02日.
 * Time 12:05
 */
@Entity
public class PtmMStdProduct implements Identifiable<Long> {
    //id,sourceId,sourceUrl,review,ratings,model,
    // brand,sourceUpdateTime,title,categoryName,subCategoryName,categoryId
    //refPrice,imageUrl,description,params,sourceListTitle,createTime,updateTime

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceId;
    private String brand;
    private String model;
    private String sourceUrl;
    private Integer review = 0;
    private Integer ratings = 0;
    private Integer stores = 0;
    private String categoryName;
    private String subCategoryName;
    private Integer categoryId = -1;
    private Float refPrice = 0f;
    private String imageUrl;
    //    private String description;
    //这两个应该放到mongo中
//    private String params;
    private String sourceListTitle;
    private String title;
    private Date createTime;
    private Date updateTime;
    private Long mspUpdateTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Integer getReview() {
        return review;
    }

    public void setReview(Integer review) {
        this.review = review;
    }

    public Integer getRatings() {
        return ratings;
    }

    public void setRatings(Integer ratings) {
        this.ratings = ratings;
    }

    public Integer getStores() {
        return stores;
    }

    public void setStores(Integer stores) {
        this.stores = stores;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Float getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(Float refPrice) {
        this.refPrice = refPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSourceListTitle() {
        return sourceListTitle;
    }

    public void setSourceListTitle(String sourceListTitle) {
        this.sourceListTitle = sourceListTitle;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getMspUpdateTime() {
        return mspUpdateTime;
    }

    public void setMspUpdateTime(Long mspUpdateTime) {
        this.mspUpdateTime = mspUpdateTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
