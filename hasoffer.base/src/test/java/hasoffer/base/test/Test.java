package hasoffer.base.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Fenix on 2016/3/28.
 */
public class Test {

    public static void main(String[] strings) {

        Map<String, Integer> map = new HashMap<String, Integer>();

        Integer lon = 100;
        map.put("11", lon);

        map.put("11", ++lon);

        System.out.println(map.get("11"));

    }
}
