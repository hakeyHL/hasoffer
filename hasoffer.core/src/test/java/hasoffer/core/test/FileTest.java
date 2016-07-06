package hasoffer.core.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hasoffer.base.config.AppConfig;
import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.bo.match.AnalysisResult;
import jodd.io.FileUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2016/6/30.
 */
public class FileTest {

    @Test
    public void ts2() {
        Map<String, Object> formMap = new HashMap<String, Object>();
        formMap.put("title", "MapmyIndia ICENAV 301 IN-Dash AVN-Universal GPS Navigation Device");

        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Accept", "application/json, text/javascript, */*; q=0.01");

        try {
            String url = AppConfig.get(AppConfig.ANALYSIS_TITLE_URL);
            HttpResponseModel responseModel = HttpUtils.post(url, formMap, headerMap);

            AnalysisResult ar = JSONUtil.toObject(responseModel.getBodyString(), AnalysisResult.class);

            System.out.println(ar.getTitle());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
    }

    @Test
    public void ts() throws Exception {
        String json = "{\"title\":\"MapmyIndia ICENAV 301 IN-Dash AVN-Universal GPS Navigation Device\",\"tagMap\":{\"MODEL\":[{\"tag\":\"301\",\"hasTag\":{\"id\":27522,\"tag\":\"301\",\"alias\":\"\",\"score\":0}},{\"tag\":\"-\",\"hasTag\":{\"id\":25,\"tag\":\"-\",\"alias\":\"\",\"score\":0}},{\"tag\":\"-\",\"hasTag\":{\"id\":25,\"tag\":\"-\",\"alias\":\"\",\"score\":0}}],\"BRAND\":[{\"tag\":\"IN\",\"hasTag\":{\"id\":4477,\"tag\":\"in\",\"alias\":\"\",\"score\":0}},{\"tag\":\"Dash\",\"hasTag\":{\"id\":447,\"tag\":\"dash\",\"alias\":\"\",\"score\":4}},{\"tag\":\"Universal\",\"hasTag\":{\"id\":6253,\"tag\":\"universal\",\"alias\":\"\",\"score\":0}},{\"tag\":\"GPS\",\"hasTag\":{\"id\":5051,\"tag\":\"gps\",\"alias\":\"\",\"score\":0}}]}}";

        JSONObject jsonObj = JSON.parseObject(json);

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
