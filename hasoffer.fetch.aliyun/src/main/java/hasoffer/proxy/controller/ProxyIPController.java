package hasoffer.proxy.controller;

import hasoffer.aliyun.enums.Group;
import hasoffer.proxy.dmo.ProxyIPDMO;
import hasoffer.proxy.service.ProxyIPService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/proxyIP")
public class ProxyIPController {

    @Resource
    private ProxyIPService proxyIPService;


    @RequestMapping(value = "/initGroupSelect", method = RequestMethod.POST)
    @ResponseBody
    public Group[] initGroupSelect() {
        return Group.values();
    }

    @RequestMapping(value = "/selectList", method = RequestMethod.POST)
    @ResponseBody
    public List<ProxyIPDMO> selectList(@RequestBody ProxyIPDMO proxyIPDMO) {
        return proxyIPService.select(proxyIPDMO);
    }


    @RequestMapping(value = "/insertProxyIP", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> insertProxyIP(@RequestBody ProxyIPDMO proxyIPDMO) {
        Map<String, String> ipMap = new HashMap<>();
        proxyIPDMO.setCreateTime(new Date());
        proxyIPDMO.setStatus("Y");
        proxyIPDMO.setDeleteFlag("N");
        List<ProxyIPDMO> proxyIPDMOList = proxyIPService.selectByIP(proxyIPDMO.getIp());
        if (proxyIPDMOList == null || proxyIPDMOList.size() == 0) {
            proxyIPService.insertProxyIP(proxyIPDMO);
            ipMap.put("msg", "新增成功.");
        } else {
            ipMap.put("msg", "IP 已存在.");
        }
        return ipMap;

    }

    @RequestMapping(value = "/stopProxyIP", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> stopProxyIP(@RequestBody ProxyIPDMO proxyIPDMO) {
        Map<String, String> ipMap = new HashMap<>();
        proxyIPDMO.setDeleteTime(new Date());
        proxyIPDMO.setDeleteFlag("Y");
        proxyIPService.updateProxyIP(proxyIPDMO);
        ipMap.put("msg", proxyIPDMO.getIp() + ":已停用");
        return ipMap;

    }


}
