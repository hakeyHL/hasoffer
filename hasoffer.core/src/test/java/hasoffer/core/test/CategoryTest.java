package hasoffer.core.test;

import hasoffer.core.persistence.po.ptm.PtmCategory;
import hasoffer.core.product.ICategoryService;
import hasoffer.core.product.solr.CategoryIndexServiceImpl;
import hasoffer.core.product.solr.CategoryModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Date : 2016/1/18
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class CategoryTest {


    @Resource
    ICategoryService categoryService;
    @Resource
    CategoryIndexServiceImpl categoryIndexService;

    @Test
    public void testShow() {
        List<PtmCategory> categories = categoryService.listSubCategories(0L);

        for (PtmCategory category : categories) {
            System.out.println(category.getId() + " - " + category.getName());
            List<PtmCategory> categories2 = categoryService.listSubCategories(category.getId());

            for (PtmCategory category2 : categories2) {
                System.out.println("\t" + category2.getId() + " - " + category2.getName());

                List<PtmCategory> categories3 = categoryService.listSubCategories(category2.getId());

                for (PtmCategory category3 : categories3) {
                    System.out.println("\t\t" + category3.getId() + " - " + category3.getName());
                }
            }
        }

    }

    @Test
    public void testCache() {
        for (int i = 0; i < 5; i++) {
            testCache_1();
        }
    }

    public void testCache_1() {
        List<PtmCategory> categories = categoryService.getRouterCategoryList(202);

        for (PtmCategory cate : categories) {
            System.out.println(cate.toString());
        }
    }

    @Test
    public void update() {

        long cateId = 23;
        String keyword = "headphones";

        categoryService.getRouterCategoryList(cateId);
    }

    @Test
    public void updateIndex() {
        categoryService.reimportCategoryIndex();
    }

    @Test
    public void search() {
//        String title = "CS2 Sony Mh750 Wired Headset Sony Mh750 Wired Headset Wired Headphones"; N
//        String title = "Enfin Homes M4XPBS Barbeque Black ";// Barbeque Y
        String title = "Adidas SPRINGBLADE DRIVE 2 M Running Shoes ";

        List<CategoryModel> categories = categoryIndexService.simpleSearch(title);

        for (CategoryModel cate : categories) {
            System.out.println(cate);
        }
    }

}
