package hasoffer.api.controller.vo;

import com.alibaba.fastjson.JSON;
import hasoffer.base.model.Website;

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
//    private List<DownLoadConfigChannle> channels = new ArrayList<DownLoadConfigChannle>();
    //APP在不同下载渠道的deeplink、logo、简介、评分值、评论数、下载数(googleplay的下载链接需添加联盟id)

    private List<Map<String, List<ThirdAppVo>>> apps = new ArrayList<Map<String, List<ThirdAppVo>>>();

    public DownloadConfigVo() {
    }

    public DownloadConfigVo(boolean isBoot, List<String> checkPackages, String priorDownloadChannel, List<Map<String, List<ThirdAppVo>>> apps) {
        this.isBoot = isBoot;
        this.checkPackages = checkPackages;
        this.priorDownloadChannel = priorDownloadChannel;
        this.apps = apps;
    }

    public static void main(String[] args) {
        Map<String, List<ThirdAppVo>> map = new HashMap<String, List<ThirdAppVo>>();
        Map<String, List<ThirdAppVo>> map2 = new HashMap<String, List<ThirdAppVo>>();
        List<Map<String, List<ThirdAppVo>>> apps = new ArrayList<Map<String, List<ThirdAppVo>>>();
        Map m = new HashMap();
        List<ThirdAppVo> li = new ArrayList<ThirdAppVo>();
        ThirdAppVo appVo = new ThirdAppVo();
        appVo.setLogoUrl("logoUrl");
        appVo.setComments("80,000");
        appVo.setDownloadLink("a");
        appVo.setDownloads("3000,000");
        appVo.setIntroduction(" yi ban ");
        appVo.setRatings(4.5f);
        appVo.setWebsite(Website.FLIPKART);
        li.add(appVo);


        ThirdAppVo appVo1 = new ThirdAppVo();
        appVo1.setLogoUrl("logoUrl1");
        appVo1.setComments("60,000");
        appVo1.setDownloadLink("b");
        appVo1.setDownloads("4000,000");
        appVo1.setIntroduction(" yi ban  a");
        appVo1.setRatings(4.9f);
        appVo1.setWebsite(Website.SNAPDEAL);
        li.add(appVo1);


        map.put("GOOGLEPLAY", li);


        List<ThirdAppVo> li2 = new ArrayList<ThirdAppVo>();
        ThirdAppVo appVo2 = new ThirdAppVo();
        appVo2.setLogoUrl("logoUrl");
        appVo2.setComments("80,000");
        appVo2.setDownloadLink("a");
        appVo2.setDownloads("3000,000");
        appVo2.setIntroduction(" yi ban ");
        appVo2.setRatings(4.5f);
        appVo2.setWebsite(Website.FLIPKART);
        li2.add(appVo2);


        ThirdAppVo appVo3 = new ThirdAppVo();
        appVo3.setLogoUrl("logoUrl1");
        appVo3.setComments("60,000");
        appVo3.setDownloadLink("b");
        appVo3.setDownloads("4000,000");
        appVo3.setIntroduction(" yi ban  a");
        appVo3.setRatings(4.9f);
        appVo3.setWebsite(Website.SNAPDEAL);
        li2.add(appVo3);

        apps.add(map);
        apps.add(map2);
        map.put("GOOGLEPLAY", li);
        map2.put("9APP", li2);
        m.put("apps", apps);
        String string = JSON.toJSONString(apps);
        System.out.println(string);
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

    public List<Map<String, List<ThirdAppVo>>> getApps() {
        return apps;
    }

    public void setApps(List<Map<String, List<ThirdAppVo>>> apps) {
        this.apps = apps;
    }
}
