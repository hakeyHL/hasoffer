package hasoffer.spider.service.dubbo;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.dubbo.spider.task.api.IProductTaskDubboService;
import hasoffer.spider.context.SpiderConfigInitContext;
import hasoffer.spider.enums.PageType;
import hasoffer.spider.model.SpiderProductTask;
import hasoffer.spring.context.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductTaskDubboServiceImpl implements IProductTaskDubboService {
    private final Logger logger = LoggerFactory.getLogger(ProductTaskDubboServiceImpl.class);
    private IRedisListService<String> redisListService;

    public ProductTaskDubboServiceImpl() {
        this.redisListService = (IRedisListService<String>) SpringContextHolder.getBean(RedisListServiceImpl.class);
    }

    @Override
    public void sendTask(SpiderProductTask product) {
        String redisKeyName = SpiderConfigInitContext.getRedisListName(product.getWebsite(), PageType.LIST);
        if (redisKeyName == null || "".equals(redisKeyName)) {
            return;
        }
        logger.info("ProductTaskDubboServiceImpl.sendTask(): product.url={}", product.getUrl());
        redisListService.push(redisKeyName, JSONUtil.toJSON(product));
    }
}
