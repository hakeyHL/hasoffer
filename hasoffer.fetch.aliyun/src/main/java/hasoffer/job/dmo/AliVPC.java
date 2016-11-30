package hasoffer.job.dmo;

public class AliVPC {

    private Integer id;
    private String ecsInstance;
    private String privateIpAddress;
    private String eipId;
    private String eipIpAddress;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEcsInstance() {
        return ecsInstance;
    }

    public void setEcsInstance(String ecsInstance) {
        this.ecsInstance = ecsInstance;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getEipId() {
        return eipId;
    }

    public void setEipId(String eipId) {
        this.eipId = eipId;
    }

    public String getEipIpAddress() {
        return eipIpAddress;
    }

    public void setEipIpAddress(String eipIpAddress) {
        this.eipIpAddress = eipIpAddress;
    }
}