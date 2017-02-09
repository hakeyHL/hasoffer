package hasoffer.core.persistence.po.app.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * Created by hs on 2017年02月09日.
 * Time 19:24
 */
@Document(collection = "AppOfferRecord")
public class AppOfferRecord {
    @Id
    private String id = UUID.randomUUID().toString();

    private String marketChannel;//渠道
    private long offerId;//哪个deal
    private long clickCount;//点击次数
    private long currentTime;//当前时间戳
    private String ymd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMarketChannel() {
        return marketChannel;
    }

    public void setMarketChannel(String marketChannel) {
        this.marketChannel = marketChannel;
    }

    public long getOfferId() {
        return offerId;
    }

    public void setOfferId(long offerId) {
        this.offerId = offerId;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }
}
