package hasoffer.core.test;

import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/1/21
 * Function :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-beans.xml")
public class DeviceTest2 {

    @Resource
    IDataBaseManager dbm;

    @Test
    public void expDevice() throws Exception {
        File file = new File("d:/TMP/deviceshopapp.txt");

        List<String> strs = FileUtils.readLines(file);

        Map<String, Long> countMap = new HashMap<String, Long>();

        for (String str : strs) {
            String[] data = str.split("\\|");

            String countStr = data[1].trim();
            String shopApps = data[2].trim();

            countMap.put(shopApps, Long.valueOf(countStr));
        }

        Map<String, Long> countMap2 = new HashMap<String, Long>();
        for (Map.Entry<String, Long> datas : countMap.entrySet()) {
            String[] apps = datas.getKey().split(",");

            long count = datas.getValue();

            for (String app : apps) {
                if (countMap2.containsKey(app)) {
                    countMap2.put(app, countMap2.get(app) + count);
                } else {
                    countMap2.put(app, count);
                }
            }
        }

        for (Map.Entry<String, Long> datas : countMap2.entrySet()) {
            System.out.println(datas.getKey() + "\t" + datas.getValue());
        }
    }

}
