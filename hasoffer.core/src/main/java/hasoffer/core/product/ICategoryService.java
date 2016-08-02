package hasoffer.core.product;

import hasoffer.core.persistence.po.ptm.PtmCategory;

import java.util.List;
import java.util.Queue;

public interface ICategoryService {

	PtmCategory createCategory(long parentId, String name, String imageUrl);

	void findRouterCategories(Queue<PtmCategory> categoryQueue, long cateId);

	List<PtmCategory> getRouterCategoryList(long categoryId);

	List<PtmCategory> listSubCategories(Long parentId);

	void reimportCategoryIndex();

	void updateCategoryIndex(long cateId, String keyword);

	void updateCategoryLevel(long cateId, int level);

	void updateCategoryName(long id, String categoryName);

	void updateCategoryKeyword(long cateId, String key);

	PtmCategory getCategory(long cateId);

	void deleteCategory(long id);

//	PtmCategory2 createAppCategory(PtmCategory2 category);

	//使用完成记得删除
	void tempDeleteCategoryForCategoryUpdate(long id);
}
