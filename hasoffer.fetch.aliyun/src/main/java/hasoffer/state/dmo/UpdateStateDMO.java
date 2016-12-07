package hasoffer.state.dmo;

import java.util.Date;

public class UpdateStateDMO {

    private int id;
    private String taskTarget;
    private String updateDate;
    private int pushNum;
    private int finishNum;
    private int exceptionNum;
    private int stopNum;
    private Date logTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskTarget() {
        return taskTarget;
    }

    public void setTaskTarget(String taskTarget) {
        this.taskTarget = taskTarget;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public int getPushNum() {
        return pushNum;
    }

    public void setPushNum(int pushNum) {
        this.pushNum = pushNum;
    }

    public int getFinishNum() {
        return finishNum;
    }

    public void setFinishNum(int finishNum) {
        this.finishNum = finishNum;
    }

    public int getExceptionNum() {
        return exceptionNum;
    }

    public void setExceptionNum(int exceptionNum) {
        this.exceptionNum = exceptionNum;
    }

    public int getStopNum() {
        return stopNum;
    }

    public void setStopNum(int stopNum) {
        this.stopNum = stopNum;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    @Override
    public String toString() {
        return "UpdateStateDMO{" +
                "id=" + id +
                ", taskTarget='" + taskTarget + '\'' +
                ", updateDate=" + updateDate +
                ", pushNum=" + pushNum +
                ", finishNum=" + finishNum +
                ", exceptionNum=" + exceptionNum +
                ", stopNum=" + stopNum +
                ", logTime=" + logTime +
                '}';
    }
}
