package hasoffer.api.controller.vo;

import hasoffer.base.model.Website;

/**
 * Created by hs on 2016年08月18日.
 * Time 11:06
 * app下载引导中引导下载的APP的信息
 */
public class ThirdAppVo {

    //deeplink、logo、简介、评分值、评论数、下载数(googleplay的下载链接需添加联盟id)
    private String packageName;
    private Website website;   //site
    private String downloadLink;//下载地址
    private String logoUrl;//logo图片地址
    private String introduction;//简介
    private float ratings;//评分值
    private String comments;//评论数
    private String downloads;//下载量
    private String packageSize;

    public ThirdAppVo() {
    }

    public ThirdAppVo(Website website, String packageName, String downloadLink, String logoUrl, String introduction, float ratings, String comments, String downloads, String pacpackageSize) {
        this.packageName = packageName;
        this.website = website;
        this.downloadLink = downloadLink;
        this.logoUrl = logoUrl;
        this.introduction = introduction;
        this.ratings = ratings;
        this.comments = comments;
        this.downloads = downloads;
        this.packageSize = pacpackageSize;
    }
}
