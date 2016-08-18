package hasoffer.api.controller.vo;

import hasoffer.base.model.Website;

/**
 * Created by hs on 2016年08月18日.
 * Time 11:06
 * app下载引导中引导下载的APP的信息
 */
public class ThirdAppVo {

    //deeplink、logo、简介、评分值、评论数、下载数(googleplay的下载链接需添加联盟id)
    private Website website;   //site
    private String downloadLink;//下载地址
    private String logoUrl;//logo图片地址
    private String introduction;//简介
    private float ratins;//评分值
    private Long comments;//评论数
    private String downloads;//下载量

    public ThirdAppVo() {
    }

    public ThirdAppVo(Website website, String downloadLink, String logoUrl, String introduction, float ratins, Long comments, String downloads) {
        this.website = website;
        this.downloadLink = downloadLink;
        this.logoUrl = logoUrl;
        this.introduction = introduction;
        this.ratins = ratins;
        this.comments = comments;
        this.downloads = downloads;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public float getRatins() {
        return ratins;
    }

    public void setRatins(float ratins) {
        this.ratins = ratins;
    }

    public Long getComments() {
        return comments;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

    public String getDownloads() {
        return downloads;
    }

    public void setDownloads(String downloads) {
        this.downloads = downloads;
    }
}
