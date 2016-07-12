package hasoffer.admin.worker;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmCategory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 2016/7/12.
 */
public class CategoryListWorker implements Runnable {

    private String queryString;
    private IDataBaseManager dbm;
    private ConcurrentLinkedQueue<PtmCategory> categoryQueue;

    public CategoryListWorker(String queryString, IDataBaseManager dbm, ConcurrentLinkedQueue<PtmCategory> categoryQueue) {
        this.queryString = queryString;
        this.dbm = dbm;
        this.categoryQueue = categoryQueue;
    }


    @Override
    public void run() {
        while (true) {

            List<PtmCategory> categoryList = dbm.query(queryString);

            for (PtmCategory category : categoryList) {

                //如果是二级类目，查询是否由子类目，如果有跳过，没有没有加入队列
                if (category.getLevel() == 2) {

                    List<PtmCategory> childCategoryList = dbm.query("SELECT t FROM PtmCategory t WHERE t.parentId = ?0 ", Arrays.asList(category.getId()));

                    if (childCategoryList == null || childCategoryList.size() == 0) {
                        categoryQueue.add(category);
                    } else {
                        continue;
                    }

                }

                if (category.getLevel() == 3) {
                    categoryQueue.add(category);
                }
            }
        }
    }
}
