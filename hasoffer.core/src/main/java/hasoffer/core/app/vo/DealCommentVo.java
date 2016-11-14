package hasoffer.core.app.vo;

/**
 * Created by hs on 2016年11月14日.
 * Time 14:17
 */
public class DealCommentVo {
    private String comCreateTime; //创建时间
    private String cmerName;//用户名
    private String cmerAvatar;//用户头像
    private String comContent;//评论内容

    public DealCommentVo() {
    }

    public DealCommentVo(String comCreateTime, String cmerName, String cmerAvatar, String comContent) {
        this.comCreateTime = comCreateTime;
        this.cmerName = cmerName;
        this.cmerAvatar = cmerAvatar;
        this.comContent = comContent;
    }

    public String getComCreateTime() {
        return comCreateTime;
    }

    public void setComCreateTime(String comCreateTime) {
        this.comCreateTime = comCreateTime;
    }

    public String getCmerName() {
        return cmerName;
    }

    public void setCmerName(String cmerName) {
        this.cmerName = cmerName;
    }

    public String getCmerAvatar() {
        return cmerAvatar;
    }

    public void setCmerAvatar(String cmerAvatar) {
        this.cmerAvatar = cmerAvatar;
    }

    public String getComContent() {
        return comContent;
    }

    public void setComContent(String comContent) {
        this.comContent = comContent;
    }
}
