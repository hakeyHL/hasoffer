package hasoffer.core.thd;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.core.bo.enums.RelateStatus;
import hasoffer.core.bo.enums.RelateType;
import hasoffer.core.persistence.po.thd.ThdCategory;
import hasoffer.core.persistence.po.thd.ThdProduct;

import java.util.Date;
import java.util.List;

/**
 * Date : 2016/2/22
 * Function :
 */
public interface IThdService {

    PageableResult<ThdProduct> getProducts(Website website, long cateId, int page, int size);

    PageableResult<ThdProduct> getProducts(Website website, long category3, RelateType relateType, int page, int size);

    PageableResult<ThdProduct> getProducts(Website website, RelateType relateType, int page, int size);

    PageableResult<ThdProduct> getProducts(Website website, int page, int size);

    List<ThdProduct> getUnrelatedProducts(Website website, int page, int size);

    PageableResult<ThdProduct> getPagedUnrelatedProductsByCategory(Website website, long cateId, int page, int size);

    ThdProduct createProduct(ThdProduct product);

    ThdCategory createCategory(ThdCategory category);

    void updateRelateInfo(ThdProduct thd);

    // start - update status
    // end - update status and time
    void updateTask(long taskId, Date processTime, TaskStatus taskStatus);

    RelateStatus relate(ThdProduct thd);
}
