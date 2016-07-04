package hasoffer.core.third.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hasoffer.core.persistence.dbm.Hibernate4DataBaseManager;
import hasoffer.core.third.ThirdService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hs on 2016/7/4.
 */
@Service
public class ThirdServiceImple implements ThirdService {
    private static String THIRD_GMOBI_DEALS = "SELECT t from Appdeal where and t.createTime <=?0 and  1=1 ";
    @Resource
    Hibernate4DataBaseManager hdm;

    @Override
    public String getDeals(String acceptjson) {
        StringBuilder sb = new StringBuilder();
        sb.append(THIRD_GMOBI_DEALS);
        JSONObject jsonObject = JSONObject.parseObject(acceptjson);
        String createTime = jsonObject.getString("createTime");
        if (StringUtils.isEmpty(createTime)) {
            createTime = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
        }
        JSONArray sites = jsonObject.getJSONArray("sites");
        String site = "";
        if (sites != null) {
            sb.append(" and t.website in ( ?1");
            for (int i = 0; i < sites.size(); i++) {
                site += (String) sites.get(i);
                if (i < sites.size() - 1) {
                    site += ",";
                }
            }
        }
        sb.append(" ) ");
        List li = new ArrayList();
        li.add(createTime);
        li.add(site);
        hdm.query(sb.toString(), li);
        return "hello";
    }
}
