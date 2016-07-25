package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by glx on 2015/5/19.
 */
@Entity
public class PtmSku implements Identifiable<Long> {
	private static final IdWorker idWorker = IdWorker.getInstance(PtmSku.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private long productId;
	private Date createTime = TimeUtils.nowDate();
	private BigDecimal price;
	private String title;//商品名称
	private String masterImagePath; // 图片

	private long brandId;// 品牌
	private long modelId;// 型号

//	@Lob
//	@Basic(fetch = FetchType.LAZY)
	@Column(columnDefinition = "longtext")
	private String description;//描述
	private long soldQty;

	public PtmSku() {
	}

	public PtmSku(long productId, BigDecimal price, String title,
	              String masterImagePath, String description, long brandId,
	              long modelId, long soldQty) {
		this.productId = productId;
		this.price = price;
		this.title = title;
		this.masterImagePath = masterImagePath;
		this.description = description;
		this.brandId = brandId;
		this.modelId = modelId;
		this.soldQty = soldQty;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMasterImagePath() {
		return masterImagePath;
	}

	public void setMasterImagePath(String masterImagePath) {
		this.masterImagePath = masterImagePath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public long getSoldQty() {
		return soldQty;
	}

	public void setSoldQty(long soldQty) {
		this.soldQty = soldQty;
	}


	public long getBrandId() {
		return brandId;
	}

	public void setBrandId(long brandId) {
		this.brandId = brandId;
	}

	public long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmSku ptmSku = (PtmSku) o;

		if (productId != ptmSku.productId) { return false; }
		if (brandId != ptmSku.brandId) { return false; }
		if (modelId != ptmSku.modelId) { return false; }
		if (soldQty != ptmSku.soldQty) { return false; }
		if (id != null ? !id.equals(ptmSku.id) : ptmSku.id != null) { return false; }
		if (createTime != null ? !createTime.equals(ptmSku.createTime) : ptmSku.createTime != null) { return false; }
		if (price != null ? !price.equals(ptmSku.price) : ptmSku.price != null) { return false; }
		if (title != null ? !title.equals(ptmSku.title) : ptmSku.title != null) { return false; }
		if (masterImagePath != null ? !masterImagePath.equals(ptmSku.masterImagePath) : ptmSku.masterImagePath != null) { return false; }
		return !(description != null ? !description.equals(ptmSku.description) : ptmSku.description != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (productId ^ (productId >>> 32));
		result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
		result = 31 * result + (price != null ? price.hashCode() : 0);
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (masterImagePath != null ? masterImagePath.hashCode() : 0);
		result = 31 * result + (int) (brandId ^ (brandId >>> 32));
		result = 31 * result + (int) (modelId ^ (modelId >>> 32));
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (int) (soldQty ^ (soldQty >>> 32));
		return result;
	}
}
