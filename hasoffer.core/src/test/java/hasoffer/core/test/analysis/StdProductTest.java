package hasoffer.core.test.analysis;

import hasoffer.affiliate.model.FlipkartSkuInfo;
import hasoffer.core.bo.enums.TopSellStatus;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.ptm.PtmProduct;
import hasoffer.core.persistence.po.ptm.PtmTopSelling;
import hasoffer.core.persistence.po.search.SrmProductSearchCount;
import hasoffer.core.product.IStdProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by chevy on 2016/8/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class StdProductTest {

    @Resource
    IStdProductService stdProductService;

    @Resource
    IDataBaseManager dbm;

    @Test
    public void buildStdResp() {
        final String Q_P = "SELECT t FROM SrmProductSearchCount t WHERE t.ymd=?0 ";

        List<SrmProductSearchCount> spscs = dbm.query(Q_P, Arrays.asList("20160720"));
        for (SrmProductSearchCount spsc : spscs) {
            PtmProduct product = dbm.get(PtmProduct.class, spsc.getProductId());
            if (product == null) {
//                System.out.println(String.format("%d not exists.", spsc.getProductId()));
                continue;
            }

            String keyword = product.getTitle();
            String keyword_2 = keyword.toLowerCase().trim();

            Map<String, FlipkartSkuInfo> skuInfoMap = stdProductService.searchSku(keyword_2);

            try {
                stdProductService.createStd(skuInfoMap);
            } catch (Exception e) {
                System.out.println(keyword + "...create error...");
            }
        }
    }

    @Test
    public void buildStdResp2() {
        final String Q_P = "SELECT t FROM PtmTopSelling t WHERE t.status=?0 ";

        List<PtmTopSelling> ptss = dbm.query(Q_P, Arrays.asList(TopSellStatus.ONLINE));
        for (PtmTopSelling pts : ptss) {
            PtmProduct product = dbm.get(PtmProduct.class, pts.getId());
            if (product == null) {
//                System.out.println(String.format("%d not exists.", spsc.getProductId()));
                continue;
            }

            String keyword = product.getTitle();
            String keyword_2 = keyword.toLowerCase().trim();

            Map<String, FlipkartSkuInfo> skuInfoMap = stdProductService.searchSku(keyword_2);

            try {
                stdProductService.createStd(skuInfoMap);
            } catch (Exception e) {
                System.out.println(keyword + "...create error...");
            }
        }
    }

    @Test
    public void getFlipkartProduct() throws Exception {
        String keyword = "samsung galaxy j2 2016 edition (8gb)";

        Map<String, FlipkartSkuInfo> skuInfoMap = stdProductService.searchSku(keyword);

        stdProductService.createStd(skuInfoMap);
    }

}
