package hasoffer.core.persistence.po.sys;

import hasoffer.base.utils.TimeUtils;
import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.po.app.AppBanner;
import hasoffer.core.utils.IdWorker;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Date : 2016/3/4
 * Function :
 */
@Entity
public class SysTimerTaskLog implements Identifiable<Long> {

    private static final IdWorker idWorker = IdWorker.getInstance(AppBanner.class);

    @Id
    @Column(unique = true, nullable = false)
    private Long id = idWorker.nextLong();

    private String taskName;

    private Date createTime = TimeUtils.nowDate();

    private Date startTime = TimeUtils.nowDate();
    private Date endTime = TimeUtils.nowDate();

    private String result;

    public SysTimerTaskLog() {
    }

    public SysTimerTaskLog(String taskName) {
        this();
        this.taskName = taskName;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SysTimerTaskLog that = (SysTimerTaskLog) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (taskName != null ? !taskName.equals(that.taskName) : that.taskName != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        return !(result != null ? !result.equals(that.result) : that.result != null);

    }

    @Override
    public int hashCode() {
        int result1 = id != null ? id.hashCode() : 0;
        result1 = 31 * result1 + (taskName != null ? taskName.hashCode() : 0);
        result1 = 31 * result1 + (createTime != null ? createTime.hashCode() : 0);
        result1 = 31 * result1 + (startTime != null ? startTime.hashCode() : 0);
        result1 = 31 * result1 + (endTime != null ? endTime.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }
}
