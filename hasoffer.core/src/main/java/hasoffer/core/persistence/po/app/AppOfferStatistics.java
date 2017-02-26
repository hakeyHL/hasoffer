package hasoffer.core.persistence.po.app;

import hasoffer.base.enums.MarketChannel;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by hs on 2017年02月09日.
 * Time 19:16
 */
@Entity
public class AppOfferStatistics implements Identifiable<Long> {
    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "bigint default 0")
    private Long offerScanCount = 0l;//offer返回的次数

    @Enumerated(EnumType.STRING)
    private MarketChannel marketChannel;//哪个渠道
    @Column(columnDefinition = "bigint default 0")
    private Long offerClickCount = 0l;//offer总点击次数

    private String ymd = TimeUtils.parse(new Date(), "yyyyMMdd");//当前日期--年月日
    @Column(columnDefinition = "bigint default 0")
    private Long scanUV;//返回列表的UV

    @Column(columnDefinition = "bigint default 0")
    private Long clickUV;//点击的UV

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getOfferScanCount() {
        return offerScanCount;
    }

    public void setOfferScanCount(Long offerScanCount) {
        this.offerScanCount = offerScanCount;
    }

    public MarketChannel getMarketChannel() {
        return marketChannel;
    }

    public void setMarketChannel(MarketChannel marketChannel) {
        this.marketChannel = marketChannel;
    }

    public Long getOfferClickCount() {
        return offerClickCount;
    }

    public void setOfferClickCount(Long offerClickCount) {
        this.offerClickCount = offerClickCount;
    }

    public String getYmd() {
        return ymd;
    }

    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    public Long getScanUV() {
        return scanUV;
    }

    public void setScanUV(Long scanUV) {
        this.scanUV = scanUV;
    }

    public Long getClickUV() {
        return clickUV;
    }

    public void setClickUV(Long clickUV) {
        this.clickUV = clickUV;
    }
}
