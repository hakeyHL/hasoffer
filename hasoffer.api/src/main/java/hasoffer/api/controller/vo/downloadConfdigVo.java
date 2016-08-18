package hasoffer.api.controller.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年08月18日.
 * Time 10:32
 * APP 下载引导
 */
public class DownloadConfdigVo {

    //是否开启下载以引导
    private boolean isBoot;
    //配置客户端检测哪些包
    private List<String> checkPackages = new ArrayList<String>();
    //优先下载渠道
    private String priorDownloadChannel = "9APP";
    //下载渠道及deeplink
    private Map<String, String> map = new HashMap<String, String>();
    //APP在不同下载渠道的deeplink、logo、简介、评分值、评论数、下载数(googleplay的下载链接需添加联盟id)
    private Map<String, ThirdAppVo> apps = new HashMap<String, ThirdAppVo>();

    public boolean isBoot() {
        return isBoot;
    }

    public void setIsBoot(boolean isBoot) {
        this.isBoot = isBoot;
    }

    public List<String> getCheckPackages() {
        return checkPackages;
    }

    public void setCheckPackages(List<String> checkPackages) {
        this.checkPackages = checkPackages;
    }

    public String getPriorDownloadChannel() {
        return priorDownloadChannel;
    }

    public void setPriorDownloadChannel(String priorDownloadChannel) {
        this.priorDownloadChannel = priorDownloadChannel;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public Map<String, ThirdAppVo> getApps() {
        return apps;
    }

    public void setApps(Map<String, ThirdAppVo> apps) {
        this.apps = apps;
    }
}
