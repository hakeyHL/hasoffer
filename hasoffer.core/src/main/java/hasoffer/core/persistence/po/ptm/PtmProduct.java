package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;


@Entity
public class PtmProduct implements Identifiable<Long> {

    private static final IdWorker idWorker = IdWorker.getInstance(PtmProduct.class);

    @Id
    @Column(unique = true, nullable = false)
    private Long id = idWorker.nextLong();

    private Date createTime = TimeUtils.nowDate();
    private Date updateTime;

    private long categoryId;
    private String title;// 标题
    private String tag;
    private float price;

    private String color;
    private String size;

    @Column(columnDefinition = "longtext")
    private String description;
    private int rating;

    private String sourceSite;
    private String sourceUrl;
    private String sourceId;

    public PtmProduct() {
    }

    public PtmProduct(Long id) {
        this.id = id;
    }

    public PtmProduct(long categoryId, String title, float price,
                      String sourceSite, String sourceUrl, String sourceId) {
        this.categoryId = categoryId;
        this.title = title;
        this.price = price;
        this.sourceId = sourceId;
        this.sourceSite = sourceSite;
        this.sourceUrl = sourceUrl;
    }

    @Deprecated
    public PtmProduct(long categoryId, String title, float price,
                      String description, String color, String size,
                      int rating, String sourceSite, String sourceId) {
        this.categoryId = categoryId;
        this.title = title;
        this.price = price;
        this.description = description;
        this.color = color;
        this.size = size;
        this.rating = rating;
        this.sourceId = sourceId;
        this.sourceSite = sourceSite;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getSourceSite() {
        return sourceSite;
    }

    public void setSourceSite(String sourceSite) {
        this.sourceSite = sourceSite;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmProduct that = (PtmProduct) o;

        if (categoryId != that.categoryId) return false;
        if (Double.compare(that.price, price) != 0) return false;
        if (rating != that.rating) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (color != null ? !color.equals(that.color) : that.color != null) return false;
        if (size != null ? !size.equals(that.size) : that.size != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (sourceSite != null ? !sourceSite.equals(that.sourceSite) : that.sourceSite != null) return false;
        if (sourceUrl != null ? !sourceUrl.equals(that.sourceUrl) : that.sourceUrl != null) return false;
        return !(sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
        result = 31 * result + (int) (categoryId ^ (categoryId >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + rating;
        result = 31 * result + (sourceSite != null ? sourceSite.hashCode() : 0);
        result = 31 * result + (sourceUrl != null ? sourceUrl.hashCode() : 0);
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
        return result;
    }
}
