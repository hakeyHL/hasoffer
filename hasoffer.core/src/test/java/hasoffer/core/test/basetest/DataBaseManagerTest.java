package hasoffer.core.test.basetest;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2016/4/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class DataBaseManagerTest {
    //private Logger logger = LoggerFactory.getLogger(DataBaseManagerTest.class);

    @Resource
    IDataBaseManager dbm;

    @Test
    @Transactional
    public void testQueryBySql() {
        String sql = "SELECT count(*) as countNum FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = :ymd AND psc.count > :sum AND sku.website = 'AMAZON' AND sku.`status` <> 'OFFSALE'";
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("ymd","20160813");
        paramsMap.put("sum","5");
        List list = dbm.queryBySql(sql, paramsMap);
        for (Object obj : list) {
            if(obj != null){
                Map<String, Object> temp = (Map<String, Object>) obj;
                System.out.println(temp.get("countNum"));
            }
        }
        sql = "SELECT sku.url,sku.productId,sku.id FROM SrmProductSearchCount psc LEFT JOIN ptmcmpsku sku ON psc.productId = sku.productId WHERE psc.ymd = :ymd AND psc.count > :sum AND sku.website = 'AMAZON' AND sku.`status` <> 'OFFSALE' limit :begin, :end";
        paramsMap.put("begin",0);
        paramsMap.put("end",1000);
        list = dbm.queryBySql(sql, paramsMap);
        for (Object obj : list) {
            if(obj != null){
                Map<String, Object> temp = (Map<String, Object>) obj;
                System.out.println(temp.get("id"));
                System.out.println(temp.get("productId"));
                System.out.println(temp.get("url"));
            }
        }


    }


}
