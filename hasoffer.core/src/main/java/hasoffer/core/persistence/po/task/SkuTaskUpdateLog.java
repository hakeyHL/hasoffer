package hasoffer.core.persistence.po.task;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created on 2016/5/20.
 */
@Entity
public class SkuTaskUpdateLog implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    //采用当天时间和网站的MD5
    private String id;
    //sum----只查询当前有多少该网站的sku
    private int totalNum;
    //onsale
    private int onsaleNum;
    //offsale
    private int offsaleNum;
    //sold out
    private int outstackNum;
    //inQueue---任务启动时有多少sku被分页
    private int inQueue;
    //success---更新正常
    private int fetchSuccess;
    //error-----更新异常
    private int fetchFail;

    public int getFetchFail() {
        return fetchFail;
    }

    public void setFetchFail(int fetchFail) {
        this.fetchFail = fetchFail;
    }

    public int getFetchSuccess() {
        return fetchSuccess;
    }

    public void setFetchSuccess(int fetchSuccess) {
        this.fetchSuccess = fetchSuccess;
    }

    public int getInQueue() {
        return inQueue;
    }

    public void setInQueue(int inQueue) {
        this.inQueue = inQueue;
    }

    public int getOffsaleNum() {
        return offsaleNum;
    }

    public void setOffsaleNum(int offsaleNum) {
        this.offsaleNum = offsaleNum;
    }

    public int getOnsaleNum() {
        return onsaleNum;
    }

    public void setOnsaleNum(int onsaleNum) {
        this.onsaleNum = onsaleNum;
    }

    public int getOutstackNum() {
        return outstackNum;
    }

    public void setOutstackNum(int outstackNum) {
        this.outstackNum = outstackNum;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SkuTaskUpdateLog that = (SkuTaskUpdateLog) o;

        if (totalNum != that.totalNum) return false;
        if (onsaleNum != that.onsaleNum) return false;
        if (offsaleNum != that.offsaleNum) return false;
        if (outstackNum != that.outstackNum) return false;
        if (inQueue != that.inQueue) return false;
        if (fetchSuccess != that.fetchSuccess) return false;
        if (fetchFail != that.fetchFail) return false;
        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + totalNum;
        result = 31 * result + onsaleNum;
        result = 31 * result + offsaleNum;
        result = 31 * result + outstackNum;
        result = 31 * result + inQueue;
        result = 31 * result + fetchSuccess;
        result = 31 * result + fetchFail;
        return result;
    }
}
