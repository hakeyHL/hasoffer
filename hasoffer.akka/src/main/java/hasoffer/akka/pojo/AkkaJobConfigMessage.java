package hasoffer.akka.pojo;

import java.io.Serializable;

public class AkkaJobConfigMessage implements Serializable {

    private int threadCount;
    private Class<?> clazz;

    public AkkaJobConfigMessage(Class<?> clazz, int threadCount) {
        this.clazz = clazz;
        this.threadCount = threadCount;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

}
