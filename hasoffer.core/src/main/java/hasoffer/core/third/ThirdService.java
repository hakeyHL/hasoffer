package hasoffer.core.third;

import hasoffer.base.enums.MarketChannel;

import java.util.Date;

/**
 * Created by hs on 2016/7/4.
 */

public interface ThirdService {
    String getDealsForIndia(int page, int pageSize, String... filterProperties);

    String getDealInfo(String id, String marketChannel, String deviceId, String... filterProperties);

    String getDealsForInveno(int page, int pageSize, String... filterProperties);

    String getDealsForGmobi(int page, int pageSize, String... filterProperties);

    String getOfferOrderInfo(Date dateStart, Date dateEnd, String[] affIds, MarketChannel marketChannel);

    String getTopSkusForNineApps(String page, String pageSize, Date updateTime, int thumbNumber);
}
