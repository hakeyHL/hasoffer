package hasoffer.proxy.dmo;

import java.util.Date;

public class ProxyIPDMO {

    private Integer id;

    private String xGroup;

    private String ip;

    private Integer port;

    private String status;

    private Long reqNum;

    private Long finishNum;

    private Long exceptionNum;

    private Date createTime;

    private Date deleteTime;

    private String deleteFlag = "N";

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getxGroup() {
        return xGroup;
    }

    public void setxGroup(String xGroup) {
        this.xGroup = xGroup;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReqNum() {
        return reqNum;
    }

    public void setReqNum(Long reqNum) {
        this.reqNum = reqNum;
    }

    public Long getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(Long finishNum) {
        this.finishNum = finishNum;
    }

    public Long getExceptionNum() {
        return exceptionNum;
    }

    public void setExceptionNum(Long exceptionNum) {
        this.exceptionNum = exceptionNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Date deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    public String toString() {
        return "ProxyIPDMO{" +
                "id=" + id +
                ", xGroup='" + xGroup + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", status='" + status + '\'' +
                ", reqNum=" + reqNum +
                ", finishNum=" + finishNum +
                ", exceptionNum=" + exceptionNum +
                ", createTime=" + createTime +
                ", deleteTime=" + deleteTime +
                ", deleteFlag='" + deleteFlag + '\'' +
                '}';
    }
}
