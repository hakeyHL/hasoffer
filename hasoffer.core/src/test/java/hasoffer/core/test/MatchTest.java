package hasoffer.core.test;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.mongo.SrmAutoSearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by chevy on 2016/7/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class MatchTest {

    @Resource
    IMongoDbManager mdm;

    private Logger logger = LoggerFactory.getLogger(MatchTest.class);

    @Test
    public void f() {
        String searchLogId = "bfeb39e0a69dbe345ab0cbdac406658d";

        SrmAutoSearchResult srmAutoSearchResult = mdm.queryOne(SrmAutoSearchResult.class, searchLogId);

        logger.debug(srmAutoSearchResult.toString());
    }

}
