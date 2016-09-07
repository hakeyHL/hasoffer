package hasoffer.core.utils;

import com.alibaba.fastjson.serializer.PropertyFilter;

import java.util.Arrays;

/**
 * Created by hs on 2016年07月29日.
 * Time 17:19
 */
public class JsonHelper<T> {
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
}
