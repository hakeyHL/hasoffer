package hasoffer.core.third;

/**
 * Created by hs on 2016/7/4.
 */

public interface ThirdService {
    String getDealsForIndia(int page, int pageSize, String... filterProperties);

    String getDealInfo(String id, String marketChannel, String deviceId, String... filterProperties);

    String getDealsForMexico(int page, int pageSize, String... filterProperties);

    String getDealsForGmobi(int page, int pageSize, String... filterProperties);
}
