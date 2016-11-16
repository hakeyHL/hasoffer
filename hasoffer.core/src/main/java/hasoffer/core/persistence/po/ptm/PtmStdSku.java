package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

@Entity
public class PtmStdSku implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long stdProId; // PtmStdProduct # id

    private String title;// (可能)带商品的color，size属性的
    private String brand; // 品牌
    private String model; // 型号 (品牌+型号不允许有重复)

    private long categoryId;
    private float refPrice; // 参考价格

    private Date createTime;//该条sku记录的创建时间

    @Column(unique = true, nullable = false)
    private String sourceId; // sourceId
    private String sourceUrl; // source url

    private PtmStdSku() {
        this.createTime = TimeUtils.nowDate();
        this.stdProId = 0;
    }

    public PtmStdSku(String title, String brand, String model,
                     long categoryId, float refPrice,
                     String sourceId, String sourceUrl) {
        this();
        this.title = title;
        this.brand = brand;
        this.model = model;

        this.categoryId = categoryId;
        this.refPrice = refPrice;

        this.sourceId = sourceId;
        this.sourceUrl = sourceUrl;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getStdProId() {
        return stdProId;
    }

    public void setStdProId(long stdProId) {
        this.stdProId = stdProId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getRefPrice() {
        return refPrice;
    }

    public void setRefPrice(float refPrice) {
        this.refPrice = refPrice;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "PtmStdSku{" +
                "brand='" + brand + '\'' +
                ", id=" + id +
                ", stdProId=" + stdProId +
                ", title='" + title + '\'' +
                ", model='" + model + '\'' +
                ", categoryId=" + categoryId +
                ", refPrice=" + refPrice +
                ", createTime=" + createTime +
                ", sourceId='" + sourceId + '\'' +
                ", sourceUrl='" + sourceUrl + '\'' +
                '}';
    }
}
