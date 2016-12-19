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
    public static final String API_FILTER_PARAMS_ = "API_FILTER_PARAMS_";
    public static final String API_SOLR_PTMSTDSKU_CATEGORY_SEARCH = "API_SOLR_PTMSTDSKU_CATEGORY_SEARCH_";
    public static final Map<String, String> API_CATEGORY_FILTER_PARAMS_MAP = new HashMap<>();
    public static final Map<String, String> API_PTMSTDSKU_PARAM_MEAN_MAP = new HashMap<>();

    static {
        API_CATEGORY_FILTER_PARAMS_MAP.put("brand", "brand");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Network", "Network");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Screen_Resolution", "Screen Resolution");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Operating_System", "Operating System");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryRam", "Ram");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryScreenSize", "Screen Size");
        API_CATEGORY_FILTER_PARAMS_MAP.put("querySecondaryCamera", "Secondary Camera");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryBatteryCapacity", "Battery Capacity");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryPrimaryCamera", "Primary Camera");
        API_CATEGORY_FILTER_PARAMS_MAP.put("queryInternalMemory", "Internal Memory");
        API_CATEGORY_FILTER_PARAMS_MAP.put("Expandable_Memory", "Expandable Memory");


        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Screen Resolution", "In phone of the same screen size,higher  resolution indicates a sharper display.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Screen Size", "");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Primary Camera", "A camera of higher resolution takes more detailed photos, which will look sharp on a large screen as well.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Expandable Memory", "An external microSD card supplements the internal storage and can be used for saving songs, images, videos, etc.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Secondary Camera", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Brand", "");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Internal Memory", "More internal storage allows installation of more apps as well as ability to save songs, images, videos, etc.");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Battery Capacity", "Higher battery capacity generally means longer battery life, but it also depends upon other factors like phone`s screen, processor, OS, optimisations by the brand and personal usage. capacity gen");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Network", "4G connection provides very fast internet access up to 100 Mbps. Along with voice calls and messages, 3G enables fast internet access of up to 42.2 Mbps, allowing live TV, multiplayer gaming, high speed downloads, etc. With 2G, you can get all cellular features, but basic internet capabilities.");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Model", "");
//        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Operating System", "");
        API_PTMSTDSKU_PARAM_MEAN_MAP.put("Ram", "More RAM results in faster loading and smoother running of multiple apps simultaneously.");

    }

}
