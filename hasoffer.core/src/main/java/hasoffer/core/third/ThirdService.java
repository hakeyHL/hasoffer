package hasoffer.core.third;

import com.alibaba.fastjson.JSONObject;
import hasoffer.base.enums.MarketChannel;

import java.util.Date;
import java.util.List;

/**
 * Created by hs on 2016/7/4.
 */

public interface ThirdService {
    String listDealsForIndia(int page, int pageSize, String... filterProperties);

    String getDealInfo(String id, String marketChannel, String deviceId, String... filterProperties);

    String listDealsForInveno(int page, int pageSize, String... filterProperties);

    String listDealsForGmobi(int page, int pageSize, String... filterProperties);

    String getOfferOrderInfo(Date dateStart, Date dateEnd, MarketChannel marketChannel);

    String listTopSkusForNineApps(String page, String pageSize, Date updateTime, int thumbNumber, String[] affs);

    List listBannerForNineApp();

    JSONObject getPtmStdPriceInfo(long stdPriceId);
}
