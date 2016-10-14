package hasoffer.spider.service.dubbo;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.dubbo.spider.task.api.ISkuTaskDubboService;
import hasoffer.spider.context.SpiderConfigInitContext;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderSkuTask;
import hasoffer.spring.context.SpringContextHolder;

public class SkuTaskDubboServiceImpl implements ISkuTaskDubboService {

    private IRedisListService<String> redisListService;

    public SkuTaskDubboServiceImpl() {
        this.redisListService = (IRedisListService<String>) SpringContextHolder.getBean(RedisListServiceImpl.class);
    }

    @Override
    public void sendTask(SpiderSkuTask skuTask) {

        String redisKeyName = SpiderConfigInitContext.getRedisListName(skuTask.getWebsite(), PageType.DETAIL);
        if (redisKeyName == null || "".equals(redisKeyName)) {
            return;
        }
        redisListService.push(redisKeyName, JSONUtil.toJSON(skuTask));
    }

}
