package hasoffer.core.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016年11月30日.
 * Time 17:18
 */
public class ConstantUtil {
    public static final long API_ONE_BILLION_NUMBER = 1000000000;
    public static final String API_DEALS_ = "API_DEALS_";
    public static final List<String> API_CATEGORY_PARAMS = new ArrayList<>();

    static {
        API_CATEGORY_PARAMS.add("Brand");
        API_CATEGORY_PARAMS.add("Network");
        API_CATEGORY_PARAMS.add("Ram");
        API_CATEGORY_PARAMS.add("Screen Size");
        API_CATEGORY_PARAMS.add("Screen Resolution");
        API_CATEGORY_PARAMS.add("Secondary Camera");
        API_CATEGORY_PARAMS.add("Battery Capacity");
        API_CATEGORY_PARAMS.add("Operating System");
        API_CATEGORY_PARAMS.add("Primary Camera");
        API_CATEGORY_PARAMS.add("Internal Memory");
        API_CATEGORY_PARAMS.add("Expandable Memory");
    }
}
