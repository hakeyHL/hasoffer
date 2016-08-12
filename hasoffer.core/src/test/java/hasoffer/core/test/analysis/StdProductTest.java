package hasoffer.core.test.analysis;

import hasoffer.affiliate.affs.flipkart.FlipkartAffiliateProductProcessor;
import hasoffer.affiliate.model.AffiliateProduct;
import hasoffer.affiliate.model.FlipkartSkuInfo;
import hasoffer.base.utils.ArrayUtils;
import hasoffer.core.product.IStdProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.HashMap;
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

    @Test
    public void getFlipkartProduct() throws Exception {
        String keyword = "iphone 5s";

        FlipkartAffiliateProductProcessor fapp = new FlipkartAffiliateProductProcessor();
        List<AffiliateProduct> searchedPros = fapp.getAffiliateProductByKeyword(keyword, 5);

        if (ArrayUtils.isNullOrEmpty(searchedPros)) {
            System.out.println("no searched results.");
            return;
        }

        for (AffiliateProduct ap : searchedPros) {
            System.out.println(ap.getSourceId() + "\t" + ap.getTitle());
        }

        String sourceId = searchedPros.get(0).getSourceId();
        FlipkartSkuInfo skuInfo = fapp.getSkuInfo(sourceId);

        Map<String, FlipkartSkuInfo> skuInfoMap = new HashMap<>();

        String[] sourceIds = skuInfo.getProductFamily();
        skuInfoMap.put(sourceId, skuInfo);

        for (String sid : sourceIds) {
            try {
                FlipkartSkuInfo skuInfo1 = fapp.getSkuInfo(sid);
                skuInfoMap.put(skuInfo1.getProductId(), skuInfo1);

                System.out.println(skuInfo1.getProductBrand() + "|\t" + skuInfo1.getModelName() + "|\t" + skuInfo1.getAttributes());
            } catch (Exception e) {
                System.out.println("error");
            }
        }

        stdProductService.createStd(skuInfoMap);
    }

}
