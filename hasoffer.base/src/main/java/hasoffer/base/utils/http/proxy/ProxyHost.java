package hasoffer.base.utils.http.proxy;

/**
 * Created on 2016/4/20.
 */
public class ProxyHost {

    private String ip;
    private String port;

    public ProxyHost(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }}
