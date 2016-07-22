package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/16
 */
@Entity
public class PtmBrand implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(AppBanner.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private Date createTime = TimeUtils.nowDate();
	private String name;
	private String logoPath;
	private int rank;
	private boolean topBrand;
	private String accordingTo;

	public PtmBrand() {}

	public PtmBrand(String name) {
		this.name = name;
	}

	public PtmBrand(String name, String logoPath, int rank, boolean topBrand) {
		this.name = name;
		this.logoPath = logoPath;
		this.rank = rank;
		this.topBrand = topBrand;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getAccordingTo() {
		return accordingTo;
	}

	public void setAccordingTo(String accordingTo) {
		this.accordingTo = accordingTo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogoPath() {
		return logoPath;
	}

	public void setLogoPath(String logoPath) {
		this.logoPath = logoPath;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isTopBrand() {
		return topBrand;
	}

	public void setTopBrand(boolean topBrand) {
		this.topBrand = topBrand;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "PtmBrand{" +
		       "id=" + id +
		       ", name='" + name + '\'' +
		       ", logoPath='" + logoPath + '\'' +
		       '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmBrand brand = (PtmBrand) o;

		if (rank != brand.rank) { return false; }
		if (topBrand != brand.topBrand) { return false; }
		if (id != null ? !id.equals(brand.id) : brand.id != null) { return false; }
		if (createTime != null ? !createTime.equals(brand.createTime) : brand.createTime != null) { return false; }
		if (name != null ? !name.equals(brand.name) : brand.name != null) { return false; }
		if (logoPath != null ? !logoPath.equals(brand.logoPath) : brand.logoPath != null) { return false; }
		return !(accordingTo != null ? !accordingTo.equals(brand.accordingTo) : brand.accordingTo != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (logoPath != null ? logoPath.hashCode() : 0);
		result = 31 * result + rank;
		result = 31 * result + (topBrand ? 1 : 0);
		result = 31 * result + (accordingTo != null ? accordingTo.hashCode() : 0);
		return result;
	}
}
