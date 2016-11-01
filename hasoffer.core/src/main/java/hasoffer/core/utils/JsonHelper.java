package hasoffer.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyFilter;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by hs on 2016年07月29日.
 * Time 17:19
 */
public class JsonHelper {
    public static PropertyFilter filterProperty(final String[] args) {
        PropertyFilter propertyFilter = new PropertyFilter() {
            @Override
            public boolean apply(Object o, String s, Object o1) {
                Arrays.sort(args);
                int tt = Arrays.binarySearch(args, s);
                if (tt > 0) {
                    return false;
                }
                return true;
            }
        };
        return propertyFilter;
    }
    //    public List<T> convertMapList2OList(List<Map> oList, T t) throws IOException {
//        List<T> li = new ArrayList<>();
//        for (Map map : oList) {
//            String string = JSON.toJSONString(map);
//            T object = JSONUtil.toObject(string, T);
//        }
//        return new ArrayList<>();
//    }

    public static void transferJson2Object(List<LinkedHashMap> dataList, List desList) {
        Class sourceClass;
        if (desList != null && desList.size() > 0) {
            sourceClass = desList.get(0).getClass();
            desList.removeAll(desList);
            for (LinkedHashMap linkedHashMap1 : dataList) {
                String string = JSON.toJSONString(linkedHashMap1);
                if (!StringUtils.isEmpty(string)) {
                    try {
                        desList.add(JSONUtil.toObject(string, sourceClass));
                    } catch (IOException e) {
                        System.out.println(Thread.currentThread().getId() + " exception occur when transfer Json2ObjectList " + e.getMessage());
                        continue;
                    }
                } else {
                    continue;
                }
            }
        }
    }

    public static <T> List<T> toList(String jsonString, Class<T> targetClass) {
        String data = StringEscapeUtils.unescapeHtml(jsonString);
        return JSON.parseArray(data, targetClass);
    }
}
