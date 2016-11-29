package hasoffer.core.product.solr;

import hasoffer.data.solr.IIdentifiable;

/**
 * Created by hs on 2016年11月28日.
 * Time 18:36
 */
public class PtmStdSkuModel implements IIdentifiable<Long> {
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
