package hasoffer.core.persistence.mongo;

import hasoffer.base.model.Website;
import hasoffer.base.utils.HexDigestUtil;
import hasoffer.base.utils.TimeUtils;
import hasoffer.core.bo.enums.IndexSearchLogStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

/**
 * Date : 2016/5/11
 * Function :
 */
@Document(collection = "PtmCmpSkuIndexSearchLog")
public class PtmCmpSkuIndexSearchLog {

    @Id
    private String id;

    private Website website;

    private String sourceId;

    private String cliQ;

    private boolean hasExcept;

    private long skuId;

    private String errorMsg;

    private Date createTime;
    private long lCreateTime;

    @Enumerated(EnumType.STRING)
    private IndexSearchLogStatus status;

    public PtmCmpSkuIndexSearchLog(Website website, String sourceId, String cliQ) {
        this.id = HexDigestUtil.md5(website.name() + sourceId + cliQ);
        this.website = website;
        this.sourceId = sourceId;
        this.cliQ = cliQ;
        this.hasExcept = false;
        this.createTime = TimeUtils.nowDate();
        this.lCreateTime = createTime.getTime();
        this.status = IndexSearchLogStatus.NEW;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getCliQ() {
        return cliQ;
    }

    public void setCliQ(String cliQ) {
        this.cliQ = cliQ;
    }

    public boolean isHasExcept() {
        return hasExcept;
    }

    public void setHasExcept(boolean hasExcept) {
        this.hasExcept = hasExcept;
    }

    public long getSkuId() {
        return skuId;
    }

    public void setSkuId(long skuId) {
        this.skuId = skuId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.hasExcept = true;
        this.errorMsg = errorMsg;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getlCreateTime() {
        return lCreateTime;
    }

    public void setlCreateTime(long lCreateTime) {
        this.lCreateTime = lCreateTime;
    }

    public IndexSearchLogStatus getStatus() {
        return status;
    }

    public void setStatus(IndexSearchLogStatus status) {
        this.status = status;
    }
}
