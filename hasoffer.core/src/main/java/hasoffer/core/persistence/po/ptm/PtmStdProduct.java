package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;


@Entity
public class PtmStdProduct implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date createTime = TimeUtils.nowDate();

    private long categoryId;

    private String title;// 标题
    private String brand; // 品牌
    private String model; // 型号 (品牌+型号不允许有重复)

    private PtmStdProduct() {
        //init default values
    }

    public PtmStdProduct(String title, String brand, String model) {
        this();
        this.title = title;
        this.brand = brand;
        this.model = model;
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
