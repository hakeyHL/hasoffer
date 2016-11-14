package hasoffer.joe.test;

import hasoffer.base.enums.TaskLevel;
import hasoffer.base.model.Website;
import hasoffer.core.admin.IDealService;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.service.IFetchDubboService;
import hasoffer.spider.constants.RedisKeysUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created on 2016/3/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:dubbo-spring.xml", "classpath:spring-beans.xml"})
public class DealFetchTest {

    @Resource
    IFetchDubboService fetchDubboService;
    @Resource
    IRedisListService redisListService;
    @Resource
    IDealService dealService;
    @Resource
    IDataBaseManager dbm;

    @Test
    public void testRedisPop() {

        Object pop = redisListService.pop(RedisKeysUtils.getWaitDealList(TaskLevel.LEVEL_2, Website.DESIDIME));

        System.out.println((String) pop);

    }

}
