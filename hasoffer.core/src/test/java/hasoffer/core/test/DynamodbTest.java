package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.mongo.AWSMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.AwsSummaryProduct;
import hasoffer.core.persistence.po.ptm.PtmCmpSku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Date : 2016/6/15
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class DynamodbTest {

    AWSMongoDbManager awsMongoDbManager = new AWSMongoDbManager();

    @Resource
    IDataBaseManager dbm;

    @Test
    public void testCount() {
        String queryStr = "1 = 1";

        List params = new ArrayList();

        long count = awsMongoDbManager.count(AwsSummaryProduct.class, queryStr, params);

        System.out.println(count);
    }

    @Test
    public void testQuery() {

        String queryStr = " id = :val1 and price > :val2 ";

        List params = new ArrayList();
        params.add(20);
        params.add(500);

        long count = awsMongoDbManager.count(AwsSummaryProduct.class, queryStr, params);
        System.out.println(count);

        PageableResult<AwsSummaryProduct> pageableResult = awsMongoDbManager.scanPage(AwsSummaryProduct.class, queryStr, params, 1, 1);

        System.out.println(pageableResult.getNumFund());
        for (AwsSummaryProduct asp : pageableResult.getData()) {
            System.out.println(asp.getId() + "\t" + asp.getWebsite() + "\t" + asp.getlCreateTime());
        }
    }

    @Test
    public void testQuery1() {

        String queryStr = "id > :val1 and website = :val2";

        List params = new ArrayList();
        params.add(20);
        params.add(Website.FLIPKART.name());

        PageableResult<AwsSummaryProduct> pageableResult = awsMongoDbManager.queryPage(AwsSummaryProduct.class, queryStr, params, 1, 1);

        System.out.println(pageableResult.getNumFund());
        for (AwsSummaryProduct asp : pageableResult.getData()) {
            System.out.println(asp.getId() + "\t" + asp.getWebsite() + "\t" + asp.getlCreateTime());
        }
    }

    @Test
    public void testDel() {
        awsMongoDbManager.deleteTable(AwsSummaryProduct.class);
    }

    @Test
    public void testUpdate() {
        awsMongoDbManager.updateTable(AwsSummaryProduct.class, 10, 10);
    }

    @Test
    public void testCreate() {
        awsMongoDbManager.createTable(AwsSummaryProduct.class);
//
//        System.out.println(awsMongoDbManager.descTable(AwsSummaryProduct.class));

        System.out.println("show tables...");
        List<String> tableNames = awsMongoDbManager.listTables();
        for (String tname : tableNames) {
            System.out.println(tname);
        }
    }

    @Test
    public void testLoad() {
        AwsSummaryProduct asp = awsMongoDbManager.queryOne(AwsSummaryProduct.class, 4L);
        System.out.println(asp.toString());
    }

    @Test
    public void test2() {
        List<String> tableNames = awsMongoDbManager.listTables();
        for (String tname : tableNames) {
            System.out.println(tname);
        }
    }

    @Test
    public void init_datas() {
        String sql = "select t from PtmCmpSku t";

        List<PtmCmpSku> cmpskus = dbm.query(sql, 1, 2000);

        List<AwsSummaryProduct> awsSummaryProducts = new ArrayList<AwsSummaryProduct>();

        for (PtmCmpSku cmpSku : cmpskus) {
            System.out.println(cmpSku.toString());
            AwsSummaryProduct awsSummaryProduct = new AwsSummaryProduct(cmpSku);
            awsSummaryProducts.add(awsSummaryProduct);

            awsMongoDbManager.save(awsSummaryProduct);
        }

//        awsMongoDbManager.save(awsSummaryProducts.toArray(new AwsSummaryProduct[0]));
    }
}
