package hasoffer.core.app.vo.mobile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2016年12月22日.
 * Time 16:01
 */
public class SiteMapKeyVo {
    //关键字名称
    private String name;
    //属性Map
    private Map pros = new HashMap();

    private String shortName;
    private int type = 0;

    public SiteMapKeyVo() {
    }

    public SiteMapKeyVo(String name, int type) {
        this.name = name;
        this.type = type;
    }

    //type  0 是把name 发回来
    // 1 是把shortName 发回来
    // 2 是把pros中的数据发回来
    // 3 是按照name去调用搜索接口
    public SiteMapKeyVo builderProMap(String key, String value) {
        this.pros.put(key, value);
        return this;
    }

    public SiteMapKeyVo buildeShortName(String shortName) {
        this.shortName = shortName;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map getPros() {
        return pros;
    }

    public void setPros(Map pros) {
        this.pros = pros;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
