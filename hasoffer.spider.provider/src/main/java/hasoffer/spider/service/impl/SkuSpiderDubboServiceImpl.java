package hasoffer.spider.service.impl;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.dubbo.spider.task.api.ISkuTaskDubboService;
import hasoffer.spider.context.SpiderConfigInitContext;
import hasoffer.spider.model.SpiderSkuTask;
import hasoffer.spring.context.SpringContextHolder;

public class SkuSpiderDubboServiceImpl implements ISkuTaskDubboService {

    private IRedisListService<String> redisListService;

    public SkuSpiderDubboServiceImpl() {
        this.redisListService = (IRedisListService<String>) SpringContextHolder.getBean(RedisListServiceImpl.class);
    }

    @Override
    public void sendTaskUrl(SpiderSkuTask skuTask) {

        String redisKeyName = SpiderConfigInitContext.getRedisListName(skuTask.getWebsite());
        if (redisKeyName == null || "".equals(redisKeyName)) {
            return;
        }
        System.out.println(redisKeyName);
        redisListService.push(redisKeyName, JSONUtil.toJSON(skuTask));
    }

}
