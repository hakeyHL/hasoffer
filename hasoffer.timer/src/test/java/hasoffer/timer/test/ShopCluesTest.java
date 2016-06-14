package hasoffer.timer.test;

import hasoffer.base.exception.HttpFetchException;
import hasoffer.core.msp.IMspService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.msp.MspCategory;
import hasoffer.core.persistence.po.thd.shopclues.ThdCCategory;
import hasoffer.core.persistence.po.thd.snapdeal.updater.ThdACategoryUpdater;
import hasoffer.core.product.ICategoryService;
import hasoffer.fetch.sites.mysmartprice.MspList2Processor;
import hasoffer.fetch.sites.mysmartprice.model.MySmartPriceUncmpProduct;
import hasoffer.fetch.sites.shopclues.ShopCluesCategoryProcessor;
import hasoffer.fetch.sites.shopclues.model.ShopCluesFetchCategory;
import hasoffer.timer.msp.worker.SaveUncmpProductWorker;
import org.htmlcleaner.XPatherException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chevy on 2015/12/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ShopCluesTest {
    private final static String Q_CATEGORY_UNCMP_FIXCOUNT = "SELECT t FROM ThdCCategory t where t.proCount = 0 and t.depth = 2";
    private final static String Q_CATEGORY_UNCMPS = "SELECT t FROM MspCategory t where t.parentId > 0 and t.compared = 0";
    @Resource
    IMspService mspService;

    @Resource
    ICategoryService categoryService;
    @Resource
    IDataBaseManager dbm;
    private Logger logger = LoggerFactory.getLogger(ShopCluesTest.class);

    @Test
    public void fetchCategories() throws HttpFetchException, XPatherException {
        Set<ShopCluesFetchCategory> categories = ShopCluesCategoryProcessor.parseCategories();
        saveCategories(categories);
    }

    @Test
    public void fixProductCount() throws HttpFetchException, XPatherException {
        List<ThdCCategory> categories = dbm.query(Q_CATEGORY_UNCMP_FIXCOUNT);
        if (categories != null && categories.size() > 0){
            for (ThdCCategory cCategory : categories){
                String cateInfo = ShopCluesCategoryProcessor.getCateInfo(cCategory.getUrl());
                String[] infos = cateInfo.split(",");
                int count = Integer.parseInt(infos[0]);
                long id = Long.parseLong(infos[1]);
                ThdACategoryUpdater updater = new ThdACategoryUpdater(cCategory.getId());
                updater.getPo().setProCount(count);
                updater.getPo().setSourceId(id);
                dbm.update(updater);
            }
        }
    }

    @Test
    public void fetchProducts(){
        BlockingQueue<MySmartPriceUncmpProduct> queue = new ArrayBlockingQueue<MySmartPriceUncmpProduct>(1024);
        ExecutorService service = Executors.newFixedThreadPool(5);
        for (int i = 0; i <5; i++){
            service.execute(new SaveUncmpProductWorker(queue, mspService));
        }

        List<MspCategory> categories = dbm.query(Q_CATEGORY_UNCMPS);
        if (categories != null && categories.size() > 0){
            for (MspCategory category :categories){
                String[] subStrs = category.getUrl().trim().split("/");
                String cateIdentify = subStrs[subStrs.length - 1];
                MspList2Processor.fetchProductsByCate(category.getId(), cateIdentify, category.getProCount(), queue);
            }
        }

        SaveUncmpProductWorker.setIsFinished(true);
    }

    public void saveCategories(Set<ShopCluesFetchCategory> categories){
        for (ShopCluesFetchCategory category : categories){
            saveCategory(category, 0);
        }
    }

    void saveCategory(ShopCluesFetchCategory category, long parentId){
        ThdCCategory cate = new ThdCCategory(0, category.getName(), category.getUrl(), category.getImageUrl());
        this.dbm.create(cate);
        if (category.getSubCates() != null && category.getSubCates().size() > 0){
            for (ShopCluesFetchCategory subCate : category.getSubCates()){
                saveCategory(subCate, cate.getId());
            }
        }
    }
}
