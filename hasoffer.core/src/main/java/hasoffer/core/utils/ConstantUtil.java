package hasoffer.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2016年11月30日D
 * Time 17:18
 */
public class ConstantUtil {
    //变量名定义规则
    //哪个服务的,操作者,哪个对象.操作类型
    public static final long API_ONE_BILLION_NUMBER = 1000000000;
    public static final String API_DEALS_ = "API_DEALS_";
    public static final String API_SOLR_PTMSTDSKU_CATEGORY_SEARCH = "API_SOLR_PTMSTDSKU_CATEGORY_SEARCH_";
    //    public static final List<String> API_CATEGORY_PARAMS = new ArrayList<>();
    public static final Map<String, String> API_CATEGORY_FILTER_PARAMS_MAP = new HashMap<>();

    static {
        API_CATEGORY_FILTER_PARAMS_MAP.put("brand", "brand");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Network", "Network");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Network3G", "Network");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Network4G", "Network");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Screen_Resolution", "Screen Resolution");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Operating_System", "Operating System");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryRam", "Ram");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryScreenSize", "Screen Size");
        API_CATEGORY_FILTER_PARAMS_MAP.put("querySecondaryCamera", "Secondary Camera");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryBatteryCapacity", "Battery Capacity");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryPrimaryCamera", "Primary Camera");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryInternalMemory", "Internal Memory");
    }
}
