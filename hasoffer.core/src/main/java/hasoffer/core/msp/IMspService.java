package hasoffer.core.msp;

import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.msp.MspProductJob;

import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.persistence.po.thd.msp.ThdMspProduct;
import hasoffer.fetch.model.ProductJob;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceProduct;

import java.util.Date;
import java.util.Set;

public interface IMspService {

    void saveCategory(MspCategory mspCategory);

    MspCategory saveCategory(long parentId, String name, String url, String imageUrl, String groupName);

    void saveProductJobs(Set<ProductJob> productJobs);

    void updateProductByMspProduct(long ptmProductId, MySmartPriceProduct mspp);

    void saveProduct(long jobId, long categoryId, MySmartPriceProduct mySmartPriceProduct);

    void saveUncmpProduct(ThdMspProduct productJob);

    void updateCategory(long id, int proCount);

    void relateCategory(Long id, PtmCategory ptmCategory);

    void updateJobProcessTime(long jobId, Date time);

    MspProductJob findJobByPtmProductId(long ptmProductId);
}
