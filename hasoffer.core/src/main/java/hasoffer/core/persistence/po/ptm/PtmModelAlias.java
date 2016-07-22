package hasoffer.core.persistence.po.ptm;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/11/25
 */
@Entity
public class PtmModelAlias implements Identifiable<Long> {

	private static final IdWorker idWorker = IdWorker.getInstance(AppBanner.class);

	@Id
	@Column(unique = true, nullable = false)
	private Long id = idWorker.nextLong();

	private long modelId;

	private String name;

	public PtmModelAlias() {
	}

	public PtmModelAlias(long modelId, String name) {
		this.modelId = modelId;
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }

		PtmModelAlias that = (PtmModelAlias) o;

		if (modelId != that.modelId) { return false; }
		if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
		return !(name != null ? !name.equals(that.name) : that.name != null);

	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (int) (modelId ^ (modelId >>> 32));
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
