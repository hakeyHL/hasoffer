package hasoffer.api.helper;

import com.alibaba.fastjson.serializer.PropertyFilter;

/**
 * Created by hs on 2016年07月29日.
 * Time 17:19
 */
public class JsonHelper {
    private String[] args;

    public JsonHelper(String[] args) {
        this.args = args;
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
