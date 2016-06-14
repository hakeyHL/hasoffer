package hasoffer.core.persistence.po.thd;

import hasoffer.base.model.TaskStatus;
import hasoffer.base.model.Website;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;
import java.util.Date;

/**
 * Date : 2016/2/23
 * Function :
 */
@Entity
public class ThdFetchTask implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    private String urlTemplate;

    private long ptmCateId;

    @Enumerated(EnumType.STRING)
    private Website website;

    private Date lastProcessTime;

    private int start;
    private int size;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus = TaskStatus.STOPPED;

    private int priority = 0;// 优先级

    public ThdFetchTask(){}

    public ThdFetchTask(String urlTemplate, long ptmCateId, Website website, Date lastProcessTime, int start, int size) {
        this.urlTemplate = urlTemplate;
        this.ptmCateId = ptmCateId;
        this.website = website;
        this.lastProcessTime = lastProcessTime;
        this.start = start;
        this.size = size;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public long getPtmCateId() {
        return ptmCateId;
    }

    public void setPtmCateId(long ptmCateId) {
        this.ptmCateId = ptmCateId;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public Date getLastProcessTime() {
        return lastProcessTime;
    }

    public void setLastProcessTime(Date lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThdFetchTask that = (ThdFetchTask) o;

        if (ptmCateId != that.ptmCateId) return false;
        if (start != that.start) return false;
        if (size != that.size) return false;
        if (priority != that.priority) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (urlTemplate != null ? !urlTemplate.equals(that.urlTemplate) : that.urlTemplate != null) return false;
        if (website != that.website) return false;
        if (lastProcessTime != null ? !lastProcessTime.equals(that.lastProcessTime) : that.lastProcessTime != null)
            return false;
        return taskStatus == that.taskStatus;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (urlTemplate != null ? urlTemplate.hashCode() : 0);
        result = 31 * result + (int) (ptmCateId ^ (ptmCateId >>> 32));
        result = 31 * result + (website != null ? website.hashCode() : 0);
        result = 31 * result + (lastProcessTime != null ? lastProcessTime.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + size;
        result = 31 * result + (taskStatus != null ? taskStatus.hashCode() : 0);
        result = 31 * result + priority;
        return result;
    }
}
