package hasoffer.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2016年11月30日D
 * Time 17:18
 */
public class ConstantUtil {
    public static final long API_ONE_BILLION_NUMBER = 1000000000;
    public static final String API_DEALS_ = "API_DEALS_";
    //    public static final List<String> API_CATEGORY_PARAMS = new ArrayList<>();
    public static final Map<String, String> API_CATEGORY_FILTER_PARAMS_MAP = new HashMap<>();

    static {
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

       /* API_CATEGORY_PARAMS.add("Brand");
        API_CATEGORY_PARAMS.add("Network");
        API_CATEGORY_PARAMS.add("Ram");
        API_CATEGORY_PARAMS.add("Screen Size");
        API_CATEGORY_PARAMS.add("Screen Resolution");
        API_CATEGORY_PARAMS.add("Secondary Camera");
        API_CATEGORY_PARAMS.add("Battery Capacity");
        API_CATEGORY_PARAMS.add("Operating System");
        API_CATEGORY_PARAMS.add("Primary Camera");
        API_CATEGORY_PARAMS.add("Internal Memory");
        API_CATEGORY_PARAMS.add("Expandable Memory");*/
    }

    public enum queryRam {
        _1GB_2GB,
        _2GB_3FGB,
        _3GB_4GB,
        _4GB_WMore,
        _Less_than_512MB,
        _512_1GB
    }

    public enum queryScreenSize {
        _3_3D5_inch,
        Less_than_3inch,
        _3D5_4_inch,
        _4_4D5_inch,
        _4D5_5_inch,
        _5_5D5_inch,
        _5D5_inchWMore
    }

    public enum querySecondaryCamera {
        _0_1D9MP,
        _2_2D9MP,
        _3_4D9MP,
        _5_7D9MP,
        _8MPWAbove
    }

    public enum queryPrimaryCamera {
        _0_1D9MP,
        _2_2D9MP,
        _3_4D9MP,
        _5_7D9MP,
        _8MPWAbove
    }

    public enum NewWork {
        _2G,
        _3G,
        _4G
    }
}
