package hasoffer.proxy.service;

import hasoffer.aliyun.enums.Group;
import hasoffer.aliyun.enums.IPState;
import hasoffer.data.redis.IRedisListService;
import hasoffer.proxy.dao.ProxyIPDAO;
import hasoffer.proxy.dmo.ProxyIPDMO;
import hasoffer.spider.constants.RedisKeysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ProxyIPService {

    private Logger logger = LoggerFactory.getLogger(ProxyIPService.class);

    @Resource
    private IRedisListService<String> listService;

    @Resource
    private ProxyIPDAO proxyIPDAO;

    //public boolean vaildate(ProxyIPDMO proxyIPDMO) {
    //    return false;
    //}

    public List<ProxyIPDMO> select(ProxyIPDMO proxyIPDMO) {
        return proxyIPDAO.select(proxyIPDMO);
    }


    public void insertProxyIP(ProxyIPDMO proxyIPDMO) {
        proxyIPDAO.insert(proxyIPDMO);
        refreshRedis();
    }

    public List<ProxyIPDMO> selectByIP(String ip) {
        ProxyIPDMO temp = new ProxyIPDMO();
        temp.setIp(ip);
        return proxyIPDAO.select(temp);
    }

    public void updateProxyIP(ProxyIPDMO proxyIPDMO) {
        proxyIPDAO.update(proxyIPDMO);
        refreshRedis();
    }

    private void refreshRedis() {
        listService.deleteAll(RedisKeysUtils.SPIDER_PROXY_LIST);
        List<ProxyIPDMO> proxyIPDMOList = select(new ProxyIPDMO());
        for (ProxyIPDMO temp : proxyIPDMOList) {
            if (temp.getStatus() != null && "Y".equals(temp.getStatus())) {
                listService.push(RedisKeysUtils.SPIDER_PROXY_LIST, temp.getIp() + ":" + temp.getPort(), RedisKeysUtils.DEFAULT_EXPIRE_TIME);
                logger.info("refresh {}, ip:port= {}:{},", RedisKeysUtils.SPIDER_PROXY_LIST, temp.getIp(), temp.getPort());
            }
        }
    }

    public List<ProxyIPDMO> selectByGroup(Group group) {
        return null;
    }

    public String selectGroupName(IPState ipState) {
        return proxyIPDAO.selectGroupName(ipState.toString());
    }

    public void updateProxyStausByGroup(String group, IPState state) {
        proxyIPDAO.updateByGroup(group, state.toString());
        refreshRedis();

    }
}
