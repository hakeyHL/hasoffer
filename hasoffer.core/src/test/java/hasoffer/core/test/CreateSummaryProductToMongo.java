package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.SummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created on 2016/5/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class CreateSummaryProductToMongo {

    private static final String Q_FLIPKART_CMPSKU = "SELECT t FROM PtmCmpSku t WHERE t.website = 'FLIPKART' ORDER BY t.id ";

    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;

    @Test
    public void testCreateSummaryProduct() {

        int curPage = 1;
        int pageSize = 1000;

        PageableResult<PtmCmpSku> pageableResult = dbm.queryPage(Q_FLIPKART_CMPSKU, curPage, pageSize);

        long totalPage = pageableResult.getTotalPage();

        while (curPage <= totalPage) {

            pageableResult = dbm.queryPage(Q_FLIPKART_CMPSKU, curPage, pageSize);

            List<PtmCmpSku> skus = pageableResult.getData();

            for (PtmCmpSku sku : skus) {

                SummaryProduct summaryProduct = new SummaryProduct();
                summaryProduct.setId(sku.getId());
                summaryProduct.setUrl(sku.getOriUrl());

                mdm.save(summaryProduct);
                System.out.println("save success for [" + sku.getId() + "]");
            }

            curPage++;
        }

    }

}
