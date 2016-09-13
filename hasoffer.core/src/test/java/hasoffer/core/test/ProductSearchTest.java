package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.core.bo.system.SearchCriteria;
import hasoffer.core.product.solr.ProductIndex2ServiceImpl;
import hasoffer.core.product.solr.ProductModel2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by chevy on 2015/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class ProductSearchTest {


    @Resource
    ProductIndex2ServiceImpl productIndex2Service;

    @Test
    public void testSearchKeyword() {
        String keyword = "iphone 6s";

        SearchCriteria sc = new SearchCriteria();
        sc.setKeyword(keyword);

        PageableResult<ProductModel2> productModels = productIndex2Service.searchProducts(sc);

        List<ProductModel2> pms = productModels.getData();

        for (ProductModel2 pm : pms) {
            print(pm.getId() + "\t" + pm.getTitle());
        }
    }

    private void print(String str) {
        System.out.println(str);
    }
}
