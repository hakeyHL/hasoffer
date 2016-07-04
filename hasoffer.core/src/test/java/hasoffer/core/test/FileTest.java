package hasoffer.core.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.utils.JSONUtil;
import hasoffer.core.bo.match.AnalysisResult;
import jodd.io.FileUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created on 2016/6/30.
 */
public class FileTest {

    @Test
    public void ts() throws Exception {
        String json = "{\"title\":\"MapmyIndia ICENAV 301 IN-Dash AVN-Universal GPS Navigation Device\",\"tagMap\":{\"MODEL\":[{\"tag\":\"301\",\"iTag\":{\"id\":27522,\"tag\":\"301\",\"alias\":\"\",\"score\":0}},{\"tag\":\"-\",\"iTag\":{\"id\":25,\"tag\":\"-\",\"alias\":\"\",\"score\":0}},{\"tag\":\"-\",\"iTag\":{\"id\":25,\"tag\":\"-\",\"alias\":\"\",\"score\":0}}],\"BRAND\":[{\"tag\":\"IN\",\"iTag\":{\"id\":4477,\"tag\":\"in\",\"alias\":\"\",\"score\":0}},{\"tag\":\"Dash\",\"iTag\":{\"id\":447,\"tag\":\"dash\",\"alias\":\"\",\"score\":4}},{\"tag\":\"Universal\",\"iTag\":{\"id\":6253,\"tag\":\"universal\",\"alias\":\"\",\"score\":0}},{\"tag\":\"GPS\",\"iTag\":{\"id\":5051,\"tag\":\"gps\",\"alias\":\"\",\"score\":0}}]}}";

        JSONObject jsonObj = JSON.parseObject(json);

        System.out.println(jsonObj.get("title"));
        Object obj = jsonObj.get("tagMap");
        System.out.println(obj);

        AnalysisResult ar = JSONUtil.toObject(json, AnalysisResult.class);
        System.out.println(ar.getTitle());
    }

    @Test
    public void test() throws IOException {

        String[] strings = FileUtil.readLines(new File("C:/Users/wing/Desktop/wing.txt"));

        for (String str : strings) {
            System.out.println(str);
        }

    }
}
