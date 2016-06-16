package hasoffer.dubbo.api.fetch.service.impl;

import hasoffer.data.redis.IRedisListService;
import hasoffer.dubbo.api.fetch.common.StringConstant;
import hasoffer.dubbo.api.fetch.service.IKeywordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(value = "keywordService")
public class KeywordServiceImpl implements IKeywordService {

    private String listKey = StringConstant.WAIT_KEY_LIST;

    @Resource
    private IRedisListService<String> redisListService;

    @Override
    public String popKeyword() {
        return redisListService.pop(listKey, true);
    }

    @Override
    public void saveKeyword(String keyword){
        redisListService.save(listKey, keyword);
    }
}
