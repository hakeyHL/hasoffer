package hasoffer.core.test;

import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.BeanUtil;
import hasoffer.core.admin.IAdminCountService;
import hasoffer.core.persistence.dbm.HibernateDao;
import hasoffer.core.persistence.mongo.StatDayAlive;
import hasoffer.core.persistence.po.admin.StsAliveResult;
import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created on 2016/4/11.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
@Transactional
@TransactionConfiguration(transactionManager = "defaultTransactionManager", defaultRollback = true)
public class StsAliveTest {

    private final String INSERT_PRE = "insert into " ;
    private final String TABLE = "stsalive" ;
    private final String INSERT_BODY = " (deviceId,wakeupTime,ratioTime,osVersion,deviceName,eCommerce,assistIsActive,assistIsFirst,showIcon,clickIcon,clickShop,marketChannel,Campaign,ADset) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)" ;
    @Resource
    HibernateDao dao;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private IAdminCountService adminCountService;
    private org.jboss.logging.Logger logger = LoggerFactory.logger(StsAliveTest.class);

    @Test
    public void batch(){
        String sql = INSERT_PRE + TABLE + "201603" + INSERT_BODY;
        List<Object[]> data = new ArrayList<Object[]>();
        Object[] obj = new Object[14];
        obj[0] = "de";
        obj[1] = new Date();
        obj[2] = new Date();
        obj[3] = "de";
        obj[4] = "de";
        obj[5] = "de";
        obj[6] = 0;
        obj[7] = 1;
        obj[8] = 1;
        obj[9] = 1;
        obj[10] = 0;
        obj[11] = "de";
        obj[12] = "de";
        obj[13] = "de";
        data.add(obj);

        System.out.print(sql);
        jdbcTemplate.batchUpdate(sql, data);
    }

    @Test
    public void update(){
        String fieldName = "clickShop";
        String sql = "update stsalive" + "201603" +  " SET "  + fieldName + " = ? WHERE deviceId=?";
        logger.info(sql);
        int i = dao.updateBySql(sql, 1, "e645199e3dc60d8c0f7fb6c2347e993a");
        Assert.assertEquals(i, 1);

    }

    @Test
    public void findStsAlive(){
        Map<String, String> param = new HashMap<String, String>();
        param.put("baseDate", "2016-03-01");
        param.put("deviceNum", "100");
        param.put("ratioNum", "100");

        PageableResult<Map<String, Object>> page =  adminCountService.findStsAlive(param, 1, 20);
        logger.info("size......" + page.getData().size());
    }

    @Test
    public void count(){
        String sql = "select id from stsalive201603 WHERE wakeupTime='2016-03-01 00:00:00' and clickIcon='0'";
        int n = dao.countBySql(sql);
        logger.info(n);
    }

    @Test
    public void findBysql(){
       List<StatDayAlive> list =  dao.findBySql("select * from StsDayAlive order by date desc limit 1");
        for(StatDayAlive ss : list){
        logger.info(ss.getYmd());
        }
        logger.info(list.size());
    }


    @Test
    public void findBysql2(){
        List<String> list =  dao.findBySql("SHOW TABLES LIKE 'stsalive20%';");
        for(String ss : list){
            logger.info(ss);
        }
        logger.info(list.size());
    }

    @Test
    public void totalAliveDevice(){
       int  aa = adminCountService.totalAliveDevice("2016-04-05");
        System.out.print(aa);
    }

    @Test
    public void page() throws InvocationTargetException, IllegalAccessException {
        String sql = "select date_format(u.createTime, '%Y-%m-%d') as downloadDate, count(s.wakeupTime) as alives, count(s.wakeupTime) as alivesPercent, " +
                " count(s.ratioTime) as ratios, count(s.ratioTime) as ratioPercent" +
                " from stsalive201603 s, urmdevice u where s.deviceId = u.id and s.wakeupTime = '2016-03-01' and  s.eCommerce != ''" +
                " group by downloadDate order by downloadDate";

        logger.info(sql);
        PageableResult<Map<String,Object>> pageData =  dao.findPageOfMapBySql(sql, 1, 20);
        for(Map<String, Object> re : pageData.getData()){
            StsAliveResult sr = new StsAliveResult();
            BeanUtil.mapToObj(re, sr);
            System.out.println(sr.getDownloadDate());
        }
        logger.info(pageData.getData().size());
    }
}
