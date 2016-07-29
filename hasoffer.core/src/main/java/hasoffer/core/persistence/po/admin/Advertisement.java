package hasoffer.core.persistence.po.admin;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hs on 2016年07月26日.
 * Time 12:02
 */
@Entity
public class Advertisement implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    private Long id;
    private Date startTime;
    private Date endTime;
    private int count;
    private String aderLogoUrl;
    private String aderName;
    private String adMinmage;
    private String adMaxmage;
    private String adSlogan;
    private String adLink;
    private String adBtnContent;
    private String aderSiteUrl;
    private int adLocation;
    //为适配客户端显示的格式化的时间字符串--开始时间
    @Transient
    private String sTime;
    //为适配客户端显示的格式化的时间字符串---结束时间
    @Transient
    private String eTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getsTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.startTime);
    }

    public String geteTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.endTime);
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAderLogoUrl() {
        return aderLogoUrl;
    }

    public void setAderLogoUrl(String aderLogoUrl) {
        this.aderLogoUrl = aderLogoUrl;
    }

    public String getAderName() {
        return aderName;
    }

    public void setAderName(String aderName) {
        this.aderName = aderName;
    }

    public String getAdMinmage() {
        return adMinmage;
    }

    public void setAdMinmage(String adMinmage) {
        this.adMinmage = adMinmage;
    }

    public String getAdMaxmage() {
        return adMaxmage;
    }

    public void setAdMaxmage(String adMaxmage) {
        this.adMaxmage = adMaxmage;
    }

    public String getAdSlogan() {
        return adSlogan;
    }

    public void setAdSlogan(String adSlogan) {
        this.adSlogan = adSlogan;
    }

    public String getAdLink() {
        return adLink;
    }

    public void setAdLink(String adLink) {
        this.adLink = adLink;
    }

    public String getAdBtnContent() {
        return adBtnContent;
    }

    public void setAdBtnContent(String adBtnContent) {
        this.adBtnContent = adBtnContent;
    }

    public String getAderSiteUrl() {
        return aderSiteUrl;
    }

    public void setAderSiteUrl(String aderSiteUrl) {
        this.aderSiteUrl = aderSiteUrl;
    }

    public int getAdLocation() {
        return adLocation;
    }

    public void setAdLocation(int adLocation) {
        this.adLocation = adLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Advertisement that = (Advertisement) o;

        if (count != that.count) return false;
        if (adLocation != that.adLocation) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        if (aderLogoUrl != null ? !aderLogoUrl.equals(that.aderLogoUrl) : that.aderLogoUrl != null) return false;
        if (aderName != null ? !aderName.equals(that.aderName) : that.aderName != null) return false;
        if (adMinmage != null ? !adMinmage.equals(that.adMinmage) : that.adMinmage != null) return false;
        if (adMaxmage != null ? !adMaxmage.equals(that.adMaxmage) : that.adMaxmage != null) return false;
        if (adSlogan != null ? !adSlogan.equals(that.adSlogan) : that.adSlogan != null) return false;
        if (adLink != null ? !adLink.equals(that.adLink) : that.adLink != null) return false;
        if (adBtnContent != null ? !adBtnContent.equals(that.adBtnContent) : that.adBtnContent != null) return false;
        return !(aderSiteUrl != null ? !aderSiteUrl.equals(that.aderSiteUrl) : that.aderSiteUrl != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + count;
        result = 31 * result + (aderLogoUrl != null ? aderLogoUrl.hashCode() : 0);
        result = 31 * result + (aderName != null ? aderName.hashCode() : 0);
        result = 31 * result + (adMinmage != null ? adMinmage.hashCode() : 0);
        result = 31 * result + (adMaxmage != null ? adMaxmage.hashCode() : 0);
        result = 31 * result + (adSlogan != null ? adSlogan.hashCode() : 0);
        result = 31 * result + (adLink != null ? adLink.hashCode() : 0);
        result = 31 * result + (adBtnContent != null ? adBtnContent.hashCode() : 0);
        result = 31 * result + (aderSiteUrl != null ? aderSiteUrl.hashCode() : 0);
        result = 31 * result + adLocation;
        return result;
    }
}
