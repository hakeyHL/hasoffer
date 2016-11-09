package hasoffer.admin.controller.vo;

/**
 * Created by chevy on 2016/11/9.
 */
public class SkuUpdateStatusVo {

    private String ymd_hh;

    private long logProCount;

    private long total;

    private long success;

    public SkuUpdateStatusVo(String ymd_hh, long logProCount, long total, long success) {
        this.ymd_hh = ymd_hh;
        this.logProCount = logProCount;
        this.total = total;
        this.success = success;
    }

    public String getYmd_hh() {
        return ymd_hh;
    }

    public void setYmd_hh(String ymd_hh) {
        this.ymd_hh = ymd_hh;
    }

    public long getLogProCount() {
        return logProCount;
    }

    public void setLogProCount(long logProCount) {
        this.logProCount = logProCount;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }
}
