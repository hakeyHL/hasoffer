package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.mongo.AWSMongoDbManager;
import hasoffer.core.persistence.mongo.AwsSummaryProduct;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/6/15
 * Function :
 */
public class DynamodbTest {

    AWSMongoDbManager awsMongoDbManager = new AWSMongoDbManager();

    @Test
    public void testQuery() {

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
    public void testSave() throws Exception {
        int len = Website.values().length;
        for (int i = 0; i < 100000; i++) {
            AwsSummaryProduct asp = new AwsSummaryProduct();
            asp.setId(i);
            asp.setWebsite(Website.values()[i % len]);

            asp.setCreateTime(TimeUtils.nowDate());

            awsMongoDbManager.save(asp);

            System.out.println(i);
            TimeUnit.MILLISECONDS.sleep(10);
        }
    }

    @Test
    public void testLoad() {
        AwsSummaryProduct asp = awsMongoDbManager.queryOne(AwsSummaryProduct.class, 4L);
        System.out.println(asp.getWebsite());
    }

    @Test
    public void test2() {
        List<String> tableNames = awsMongoDbManager.listTables();
        for (String tname : tableNames) {
            System.out.println(tname);
        }
    }

}
