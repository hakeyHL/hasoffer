package hasoffer.api.controller.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年08月18日.
 * Time 11:15
 * APP下载引导
 */
public class DownloadConfigVo {
    //是否开启下载以引导
    private boolean isBoot;
    //配置客户端检测哪些包
    private List<String> checkPackages = new ArrayList<String>();
    //优先下载渠道
    private String priorDownloadChannel = "9APP";
    //下载渠道及deeplink
    private Map<String, String> channels = new HashMap<String, String>();
    //APP在不同下载渠道的deeplink、logo、简介、评分值、评论数、下载数(googleplay的下载链接需添加联盟id)

    private HashMap<String, List<ThirdAppVo>> apps = new HashMap<String, List<ThirdAppVo>>();

    public DownloadConfigVo() {
    }

    public DownloadConfigVo(boolean isBoot, List<String> checkPackages, String priorDownloadChannel, Map<String, String> channels, HashMap<String, List<ThirdAppVo>> apps) {
        this.isBoot = isBoot;
        this.checkPackages = checkPackages;
        this.priorDownloadChannel = priorDownloadChannel;
        this.channels = channels;
        this.apps = apps;
    }

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

    public HashMap<String, List<ThirdAppVo>> getApps() {
        return apps;
    }

    public void setApps(HashMap<String, List<ThirdAppVo>> apps) {
        this.apps = apps;
    }

    public Map<String, String> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, String> channels) {
        this.channels = channels;
    }
}
