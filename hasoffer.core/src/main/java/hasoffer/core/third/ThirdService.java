package hasoffer.core.third;

import java.util.Date;

/**
 * Created by hs on 2016/7/4.
 */

public interface ThirdService {
    String getDealsForIndia(int page, int pageSize, String... filterProperties);

    String getDealInfo(String id, String marketChannel, String deviceId, String... filterProperties);

    String getDealsForInveno(int page, int pageSize, String... filterProperties);

    String getDealsForGmobi(int page, int pageSize, String... filterProperties);

    String getOfferOrderInfo(Date dateStart, Date dateEnd);
}
