package hasoffer.core.app.impl;

import hasoffer.core.app.AppCacheService;
import hasoffer.core.redis.ICacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Created by hs on 2016年12月13日.
 * Time 11:49
 */
@Service
public class AppCacheServiceImpl<T> implements AppCacheService<T> {
    @Autowired
    ICacheService iCacheService;

    @Override
    public String getCacheValueByKey(String key) {
        return iCacheService.get(key, 0);
    }

    @Override
    public String getCacheValueByKey(String key, long seconds) {
        return iCacheService.get(key, seconds);
    }

    @Override
    public boolean addStringCache(String key, String value, long seconds) {
        return iCacheService.add(key, value, seconds);
    }

    @Override
    public boolean addObjectCache(String key, Object value, long seconds) {
        return iCacheService.add(key, value, seconds);
    }

    @Override
    public List<T> getObjectListFromCache(String key, long seconds) {
        Class clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

        return null;
    }

    @Override
    public boolean addObjectListToCache(String key, String value, long seconds) {

        return true;
    }

}
