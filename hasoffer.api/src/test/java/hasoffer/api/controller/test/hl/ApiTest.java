package hasoffer.api.controller.test.hl;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016年10月31日.
 * Time 14:50
 */
public class ApiTest {
    @Test
    public void redisTest() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        String idsValue = jedis.get("idsKey");
        if (StringUtils.isEmpty(idsValue)) {
            List idList = new ArrayList();
            idList.add("hasoffer");
            idList.add("ShunFeng Liu");
            jedis.append("idsKey", JSONArray.toJSONString(idList));
        } else {
            JSONArray jsonArray = JSONArray.parseArray(idsValue);
            String[] strings = jsonArray.toArray(new String[]{});
            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];
                System.out.println(string);
            }
        }

    }
}
