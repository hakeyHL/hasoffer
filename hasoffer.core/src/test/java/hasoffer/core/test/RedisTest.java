package hasoffer.core.test;

import hasoffer.core.cache.SearchLogCacheManager;
import hasoffer.core.persistence.dbm.nosql.IMongoDbManager;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.mongo.UrmDeviceRequestLog;
import hasoffer.core.persistence.po.search.SrmSearchLog;
import hasoffer.core.redis.impl.CacheServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * Date : 2016/2/18
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class RedisTest {

    @Resource
    IMongoDbManager mdm;
    @Resource
    IDataBaseManager dbm;
    @Resource
    SearchLogCacheManager logCacheManager;
    @Resource
    CacheServiceImpl cacheService;
    @Resource
    private RedisTemplate<Serializable, Serializable> redisTemplate;

    @Test
    public void exits() {
        boolean exists = cacheService.exists("LOG_COUNT_20160627");

        System.out.println(exists);
    }

    @Test
    public void delMap() {
        cacheService.del("LOG_COUNT_20160627");
    }

    @Test
    public void countTest() {
        List<SrmSearchLog> logs = dbm.query("select t from SrmSearchLog t", 1, 100);

        for (SrmSearchLog log : logs) {
            logCacheManager.countSrmSearchLog(log.getId());
        }

        cacheService.expire("LOG_COUNT_20160627", 200);
    }

    @Test
    public void set() {

        final UrmDeviceRequestLog log = mdm.queryOne(UrmDeviceRequestLog.class);

        System.out.println(log.getId() + "\t" + log.getDeviceId());

        redisTemplate.execute(new RedisCallback() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(redisTemplate.getStringSerializer().serialize(log.getId()),
                        redisTemplate.getStringSerializer().serialize(log.getDeviceId()));
                return 1L;
            }
        });
    }

    @Test
    public void get() {

        final String logId = "5698ec85e4b0abae708ef7d2";

        String deviceId = (String) redisTemplate.execute(new RedisCallback() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(logId);
                if (connection.exists(key)) {
                    byte[] value = connection.get(key);
                    String deviceId = redisTemplate.getStringSerializer().deserialize(value);
                    return deviceId;
                }
                return null;
            }
        });

        System.out.println(deviceId);
    }

    @Test
    public void del() {

        final String logId = "5698ec85e4b0abae708ef7d2";

        redisTemplate.execute(new RedisCallback() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] key = redisTemplate.getStringSerializer().serialize(logId);
                if (connection.exists(key)) {
                    byte[] value = connection.get(key);
                    connection.del(key);
                    return "1";
                }
                return "0";
            }
        });
    }

}
