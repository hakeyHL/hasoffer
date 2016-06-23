package hasoffer.core.persistence.dbm.mongo;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import hasoffer.base.model.PageableResult;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/1/4.
 */
//@Component
public class AWSMongoDbManager implements IMongoDbManager {

//    static AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient().withEndpoint("http://60.205.57.57:8000");
//static AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient().withEndpoint("http://192.168.1.203:8000");

//    static AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(new ProfileCredentialsProvider());

    private static AmazonDynamoDBClient dynamoDBClient = getDynamoDBClient();

    private static AmazonDynamoDBClient getDynamoDBClient() {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials("AKIAI2KXGSAA6ML4ZSJQ", "vDUeGxdjPeH1ulHark/VhKlAkD4d9L/wVpBINxep");
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(basicAWSCredentials)
                .withRegion(Regions.AP_SOUTHEAST_1);

        return client;
    }

    private static DynamoDBMapper getMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }

    private <T> T getById(Class<T> clazz, Object id) throws Exception {
        return getMapper().load(clazz, id);
    }

    public <T> void createTable(Class<T> clazz) {
        CreateTableRequest request = getMapper().generateCreateTableRequest(clazz);

        request.withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(100L)
                        .withWriteCapacityUnits(10L)
        );

        CreateTableResult createTableResult = dynamoDBClient.createTable(request);

        System.out.println(createTableResult.getTableDescription().toString());
    }

    public <T> void updateTable(Class<T> clazz, long readUnits, long writeUnits) {
        String tName = getTableName(clazz);

        UpdateTableResult updateTableResult = dynamoDBClient.updateTable(tName, new ProvisionedThroughput().withReadCapacityUnits(readUnits).withWriteCapacityUnits(writeUnits));
    }

    public <T> void deleteTable(Class<T> clazz) {
        String tName = getTableName(clazz);

        DeleteTableResult deleteTableResult = dynamoDBClient.deleteTable(tName);
    }

    public List<String> listTables() {
        ListTablesResult tables = dynamoDBClient.listTables();
        return tables.getTableNames();
    }

    public <T> String descTable(Class<T> clazz) {
        String tName = getTableName(clazz);

        return dynamoDBClient.describeTable(tName).toString();
    }

    private <T> String getTableName(Class<T> clazz) {
        String className = clazz.getName();
        return className.substring(className.lastIndexOf(".") + 1);
    }

    @Override
    public <T> void save(T t) {
        getMapper().save(t);
    }

    public <T> void save(T... ts) {
        getMapper().save(ts);
    }

    public <T> PageableResult<T> queryPage(Class<T> clazz, String expressionStr, List<Object> params, int page, int size) {

        Map<String, AttributeValue> eav = getParamMap(params);

        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                .withFilterExpression(expressionStr)
                .withExpressionAttributeValues(eav);

        QueryResultPage<T> resultPage = getMapper().queryPage(clazz, queryExpression);

        return new PageableResult<T>(resultPage.getResults(), resultPage.getCount(), 1, 1);
    }

    public <T> PageableResult<T> scanPage(Class<T> clazz, String expressionStr, List<Object> params, int page, int size) {

        Map<String, AttributeValue> eav = getParamMap(params);

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression(expressionStr)
                .withExpressionAttributeValues(eav);

        ScanResultPage<T> resultPage = getMapper().scanPage(clazz, scanExpression);


        return new PageableResult<T>(resultPage.getResults(), resultPage.getCount(), 1, 1);
    }

    private Map<String, AttributeValue> getParamMap(List<Object> params) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();

        int index = 1;
        for (Object param : params) {
            if (param instanceof String) {
                eav.put(":val" + index, new AttributeValue().withS((String) param));
            } else if (param instanceof Integer || param instanceof Long) {
                eav.put(":val" + index, new AttributeValue().withN(param.toString()));
            }

            index++;
        }

        return eav;
    }

    public <T> long count(Class<T> clazz, String expressionStr, List<Object> params) {

        Map<String, AttributeValue> eav = getParamMap(params);

//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression(expressionStr)
//                .withExpressionAttributeValues(eav);

        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                .withFilterExpression(expressionStr)
                .withExpressionAttributeValues(eav);

        return getMapper().count(clazz, queryExpression);
    }

    @Override
    public <T> List<T> query(Class<T> clazz, Query query) {
        return null;
    }

    //    @Override
    public <T> List<T> query(Class<T> clazz, String queryStr, Map<String, Object> params) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(""));
        eav.put(":v2", new AttributeValue().withS(""));

        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                .withKeyConditionExpression(queryStr).withExpressionAttributeValues(eav);

        return getMapper().query(clazz, queryExpression);
    }

    @Override
    public <T> PageableResult<T> queryPage(Class<T> clazz, Query query, int page, int size) {

        return null;
    }

    @Override
    public <T> long count(Class<T> clazz, Query query) {
        return 0;
    }

    @Override
    public <T> List<T> query(Class<T> clazz, Query query, int page, int size) {
        return null;
    }

    @Override
    public <T> T queryOne(Class<T> clazz) {
        return null;
    }

    @Override
    public <T> int update(Class<T> clazz, Object id, Update update) {
        return 0;
    }

    @Override
    public <T> T queryOne(Class<T> clazz, Object id) {
        return getMapper().load(clazz, id);
    }

    @Override
    public <T> AggregationResults<T> aggregate(Class<T> clazz, TypedAggregation<T> agg) {
        return null;
    }
}
