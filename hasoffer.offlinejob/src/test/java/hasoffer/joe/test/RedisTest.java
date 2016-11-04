package hasoffer.joe.test;

import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.IRedisSetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created on 2016/3/8.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-cache.xml"})
public class RedisTest {

    @Resource
    IRedisListService<String> listService;

    @Resource
    IRedisSetService redisSetService;

    @Test
    public void test() {
//        listService.push("PRICE_OFF_SKUID_QUEUE", "2");
        System.out.println(redisSetService.size("PRODUCT_UPDATE_PROCESSED_20161104"));
    }


}
