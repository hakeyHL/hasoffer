package hasoffer.api.controller.vo;

/**
 * Created by chevy on 2016/7/4.
 */
public class ResultVo {

    private String errorCode;

    private String msg;

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
}
