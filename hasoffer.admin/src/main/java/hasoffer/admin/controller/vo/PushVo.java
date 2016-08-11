package hasoffer.admin.controller.vo;

/**
 * Created by hs on 2016年08月11日.
 * Time 16:13
 */
public class PushVo {
    private String title;
    private String content;
    private String outline;
    private String[] apps;
    private String version;
    private String[] marketChannel;
    private String messageType;
    private String id;
    private int number;

    public String[] getMarketChannel() {
        return marketChannel;
    }

    public void setMarketChannel(String[] marketChannel) {
        this.marketChannel = marketChannel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOutline() {
        return outline;
    }

    public void setOutline(String outline) {
        this.outline = outline;
    }

    public String[] getApps() {
        return apps;
    }

    public void setApps(String[] apps) {
        this.apps = apps;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
