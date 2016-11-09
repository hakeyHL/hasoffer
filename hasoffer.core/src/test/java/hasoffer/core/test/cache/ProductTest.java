package hasoffer.core.test.cache;

import hasoffer.core.bo.product.SkuUpdateResult2;
import hasoffer.core.cache.ProductCacheManager;
import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.manager.SkuUpdateStatManager;
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
public class ProductTest {

    @Resource
    SearchLogCacheManager searchLogCacheManager;
    @Resource
    ProductCacheManager productCacheManager;
    @Resource
    SkuUpdateStatManager skuUpdateStatManager;


    @Test
    public void testtest() {
        for (int i = 91992; i < 92000; i++) {
            searchLogCacheManager.countSearchedProductByHour(i);
        }

        productCacheManager.put2UpdateQueue(100L);

        productCacheManager.put2UpdateProcessedSet(1000L);
        productCacheManager.put2UpdateQueue(1000L);

        skuUpdateStatManager.statUpdateResultByHour("20161109_11");
    }

    @Test
    public void test1() {
        List<SkuUpdateResult2> datas = searchLogCacheManager.getStatResults("20161109");

        for (SkuUpdateResult2 dat : datas) {
            System.out.println(dat.getAllTotal());
        }
    }

}
