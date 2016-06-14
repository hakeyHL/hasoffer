package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * 商品类别信息表
 * Created by glx on 2014/7/25.
 */
@Entity
public class PtmCategory implements Identifiable<Long> {
	@Id
	@Column(unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private long parentId;
	private int level;

	private String name;
	private String imageUrl;

	private String keyword;

	public PtmCategory() {}

	public PtmCategory(long parentId, String name,
	                   String imageUrl) {
		this.parentId = parentId;
		this.name = name;
		this.imageUrl = imageUrl;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PtmCategory category = (PtmCategory) o;

		if (parentId != category.parentId) return false;
		if (level != category.level) return false;
		if (id != null ? !id.equals(category.id) : category.id != null) return false;
		if (name != null ? !name.equals(category.name) : category.name != null) return false;
		if (imageUrl != null ? !imageUrl.equals(category.imageUrl) : category.imageUrl != null) return false;
		return !(keyword != null ? !keyword.equals(category.keyword) : category.keyword != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (parentId ^ (parentId >>> 32));
		result = 31 * result + level;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
		result = 31 * result + (keyword != null ? keyword.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "PtmCategory{" +
				"id=" + id +
				", parentId=" + parentId +
				", level=" + level +
				", name='" + name + '\'' +
				", imageUrl='" + imageUrl + '\'' +
				", keyword='" + keyword + '\'' +
				'}';
	}
}
