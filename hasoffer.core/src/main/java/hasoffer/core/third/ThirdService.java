package hasoffer.core.third;

/**
 * Created by hs on 2016/7/4.
 */

public interface ThirdService {
    String getDeals(String acceptjson);

    String getDealsForIndia(int page, int pageSize);

    String getDealInfoForIndia(String id);
}
