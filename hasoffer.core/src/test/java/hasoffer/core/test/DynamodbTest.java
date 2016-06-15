package hasoffer.core.test;

import hasoffer.core.persistence.dbm.mongo.AWSMongoDbManager;
import hasoffer.core.persistence.mongo.AwsSummaryProduct;
import org.junit.Test;

import java.util.List;

/**
 * Date : 2016/6/15
 * Function :
 */
public class DynamodbTest {

    AWSMongoDbManager awsMongoDbManager = new AWSMongoDbManager();

    @Test
    public void test1() {
//        awsMongoDbManager.createTable(AwsSummaryProduct.class);

        awsMongoDbManager.updateTable(AwsSummaryProduct.class, 2000, 100);

//        AwsSummaryProduct asp = new AwsSummaryProduct();
//        asp.setId(0);
//        asp.setWebsite(Website.AMAZON.name());
//
//        awsMongoDbManager.save(asp);
        System.out.println("show tables...");
        List<String> tableNames = awsMongoDbManager.listTables();
        for (String tname : tableNames) {
            System.out.println(tname);
        }
    }

    @Test
    public void test2() {
        List<String> tableNames = awsMongoDbManager.listTables();
        for (String tname : tableNames) {
            System.out.println(tname);
        }
    }

}
