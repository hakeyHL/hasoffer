package hasoffer.proxy.dmo;

import java.util.Date;

public class ProxyIPDMO {

    private Integer id;

    private String ip;

    private Integer port;

    private String status;

    private Long reqNum;

    private Long finishNum;

    private Long exceptionNum;

    private Date startDate;

    private Date stopDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    @Override
    public String toString() {
        return "ProxyIPDMO{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", status='" + status + '\'' +
                ", reqNum=" + reqNum +
                ", finishNum=" + finishNum +
                ", exceptionNum=" + exceptionNum +
                ", startDate=" + startDate +
                ", stopDate=" + stopDate +
                '}';
    }
}
