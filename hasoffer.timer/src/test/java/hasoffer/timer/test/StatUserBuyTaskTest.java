package hasoffer.timer.test;

import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.user.IBuyService;
import hasoffer.timer.userbuy.StatUserBuyTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by wing on 2016/4/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class StatUserBuyTaskTest {

    @Resource
    IBuyService service;
    @Resource
    IDataBaseManager dbm;
    @Resource
    IMongoDbManager mdm;


    @Test
    public void testStatUserBuyTask() {

//        StatUserBuyTask task = new StatUserBuyTask(service, dbm, mdm);
//
//        task.parseStatUserBuy();


    }

}
