package hasoffer.akka.pojo;

import java.io.Serializable;

public class AkkaJobMessage implements Serializable {

    private Class<?> clazz;
    private Object object;

    public AkkaJobMessage(Class<?> clazz, Object object) {
        this.clazz = clazz;
        this.object = object;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
