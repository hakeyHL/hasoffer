package hasoffer.core.persistence.po.app;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.enums.PushSourceType;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/9/14.
 */
@Entity
public class AppPush implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private PushSourceType pushSourceType;//push来源类型
    private Date createTime;//push创建时间
    private String sourceId;//来源id，官方定义:配置参数
    private String title;//推送文案标题
    @Column(columnDefinition = "text")
    private String content;//推送文案详情
    private Long pushExpectDeviceNumber;//预计推送设备数
    private Long receiveSuccessNumber;//成功接收
    private Long clickNumber;//用户点击次数
    private String pushImageUrl;//推送的图片地址

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long aLong) {
        this.id = aLong;
    }

    public Long getClickNumber() {
        return clickNumber;
    }

    public void setClickNumber(Long clickNumber) {
        this.clickNumber = clickNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getPushExpectDeviceNumber() {
        return pushExpectDeviceNumber;
    }

    public void setPushExpectDeviceNumber(Long pushExpectDeviceNumber) {
        this.pushExpectDeviceNumber = pushExpectDeviceNumber;
    }

    public PushSourceType getPushSourceType() {
        return pushSourceType;
    }

    public void setPushSourceType(PushSourceType pushSourceType) {
        this.pushSourceType = pushSourceType;
    }

    public Long getReceiveSuccessNumber() {
        return receiveSuccessNumber;
    }

    public void setReceiveSuccessNumber(Long receiveSuccessNumber) {
        this.receiveSuccessNumber = receiveSuccessNumber;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPushImageUrl() {
        return pushImageUrl;
    }

    public void setPushImageUrl(String pushImageUrl) {
        this.pushImageUrl = pushImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppPush appPush = (AppPush) o;

        if (id != null ? !id.equals(appPush.id) : appPush.id != null) return false;
        if (pushSourceType != appPush.pushSourceType) return false;
        if (createTime != null ? !createTime.equals(appPush.createTime) : appPush.createTime != null) return false;
        if (sourceId != null ? !sourceId.equals(appPush.sourceId) : appPush.sourceId != null) return false;
        if (title != null ? !title.equals(appPush.title) : appPush.title != null) return false;
        if (content != null ? !content.equals(appPush.content) : appPush.content != null) return false;
        if (pushExpectDeviceNumber != null ? !pushExpectDeviceNumber.equals(appPush.pushExpectDeviceNumber) : appPush.pushExpectDeviceNumber != null)
            return false;
        if (receiveSuccessNumber != null ? !receiveSuccessNumber.equals(appPush.receiveSuccessNumber) : appPush.receiveSuccessNumber != null)
            return false;
        if (clickNumber != null ? !clickNumber.equals(appPush.clickNumber) : appPush.clickNumber != null) return false;
        return !(pushImageUrl != null ? !pushImageUrl.equals(appPush.pushImageUrl) : appPush.pushImageUrl != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (pushSourceType != null ? pushSourceType.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (sourceId != null ? sourceId.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (pushExpectDeviceNumber != null ? pushExpectDeviceNumber.hashCode() : 0);
        result = 31 * result + (receiveSuccessNumber != null ? receiveSuccessNumber.hashCode() : 0);
        result = 31 * result + (clickNumber != null ? clickNumber.hashCode() : 0);
        result = 31 * result + (pushImageUrl != null ? pushImageUrl.hashCode() : 0);
        return result;
    }
}
