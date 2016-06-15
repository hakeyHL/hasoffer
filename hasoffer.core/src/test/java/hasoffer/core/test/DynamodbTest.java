package hasoffer.core.test;

import hasoffer.base.model.Website;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.mongo.AWSMongoDbManager;
import hasoffer.core.persistence.mongo.AwsSummaryProduct;
import hasoffer.core.persistence.mongo.SummaryProduct;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date : 2016/6/15
 * Function :
 */
public class DynamodbTest {

    AWSMongoDbManager awsMongoDbManager = new AWSMongoDbManager();

    @Test
    public void testDel() {
        awsMongoDbManager.deleteTable(SummaryProduct.class);
    }

    @Test
    public void test1() {
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
        for (int i = 0; i < 100; i++) {
            AwsSummaryProduct asp = new AwsSummaryProduct();
            asp.setId(i);
            asp.setWebsite(Website.values()[i % len].name());

            asp.setCreateTime(TimeUtils.nowDate());

            awsMongoDbManager.save(asp);

            System.out.println(i);
            TimeUnit.MILLISECONDS.sleep(500);
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
