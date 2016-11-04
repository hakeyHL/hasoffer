package hasoffer.third.task;

import hasoffer.base.utils.JSONUtil;
import hasoffer.data.redis.IRedisListService;
import hasoffer.data.redis.impl.RedisListServiceImpl;
import hasoffer.spider.model.ProxyIP;
import hasoffer.spring.context.SpringContextHolder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public class ProxyIPTimerTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger("hasoffer.proxy.ip.task");


    @Override
    public void run() {
        // 1,访问西刺代理 http://tpv.daxiangdaili.com/ip/?tid=557861101684355&num=10&foreign=only&show_area=true
        List<ProxyIP> list = getIPStr();
        logger.info("size is {}", list.size());
        // 2,将结果缓存到redis中。
        IRedisListService<String> redisListService = SpringContextHolder.getBean(RedisListServiceImpl.class);
        for (ProxyIP obj : list) {
            redisListService.push("PROXY_IP_LIST", JSONUtil.toJSON(obj));
        }

    }

    private List<ProxyIP> getIPStr() {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        String url = "http://tpv.daxiangdaili.com/ip/?tid=559474704659304&num=10&foreign=only&show_area=true&filter=on&delay=5&format=json";
        HttpGet httpget = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
            return JSONUtil.toObject(result, List.class);

        } catch (Exception e) {
            logger.error("Get IP Fail.", e);
        } finally {
            //关闭链接,释放资源
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}