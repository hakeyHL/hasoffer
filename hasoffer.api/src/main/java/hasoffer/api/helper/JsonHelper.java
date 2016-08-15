package hasoffer.api.helper;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by hs on 2016年07月29日.
 * Time 17:19
 */
public class JsonHelper {
    private String[] args;

    public JsonHelper(String[] args) {
        this.args = args;
    }

    public static Map getJsonMap(String jsonString) {
        if (jsonString != null) {
            Map param = new HashMap();
            JSONObject jsonObject1 = JSONObject.parseObject(jsonString);
            Set<Map.Entry<String, Object>> entries = jsonObject1.entrySet();
            Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                param.put(next.getKey(), next.getValue());
            }
            return param;
        } else {
            return null;
        }
    }

    public void J() {
        PropertyFilter propertyFilter = new PropertyFilter() {
            @Override
            public boolean apply(Object o, String s, Object o1) {

                return false;
            }
        };
    }
}
