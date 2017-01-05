package hasoffer.core.app;

import java.util.List;

/**
 * Created by hs on 2016年12月13日.
 * Time 11:48
 */
public interface AppCacheService<T> {
    String getCacheValueByKey(String key);

    String getCacheValueByKey(String key, long seconds);

    boolean addStringCache(String key, String value, long seconds);

    boolean addObjectCache(String key, Object object, long seconds);

    List<T> getObjectListFromCache(String key, long seconds);

    boolean addObjectListToCache(String key, String value, long seconds);
}
