package hasoffer.core.app.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chevy on 2016/7/4.
 */
public class ResultVo {

    private String errorCode = "00000";

    private String msg = "ok";
    private Map data = new HashMap();

    public ResultVo() {
    }

    public ResultVo(String errorCode, String msg) {
        this.errorCode = errorCode;
        this.msg = msg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

}
