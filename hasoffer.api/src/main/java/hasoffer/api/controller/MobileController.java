package hasoffer.api.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;

/**
 * Created by hs on 2016年12月20日.
 * 为H5服务
 * Time 16:38
 */
@Controller
public class MobileController {
    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a", "b");
        jsonObject.put("c", "d");
        jsonObject.writeJSONString(stringBuilder);
        System.out.println("1");
    }
}
