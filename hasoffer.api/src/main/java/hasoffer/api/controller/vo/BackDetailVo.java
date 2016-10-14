package hasoffer.api.controller.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016/6/16.
 * 查看返利vo
 */
public class BackDetailVo {
    //冻结coin
    private BigDecimal PendingCoins;
    //可使用coin
    private BigDecimal verifiedCoins;
    //订单记录
    private List<OrderVo> transcations = new ArrayList<OrderVo>();

    //本次签到可获得的奖励
    private Integer thisTimeCoin = 0;

    //下次签到可获得的奖励
    private Integer nextTimeCoin = 0;

    //当前最大连续签到数
    private Integer maxConSignNum;

    //今日是否已经签到
    private boolean hasSign = false;

    //是否从未签到过,默认是
    private boolean everSign = true;
    private boolean auxiliaryCheck = false;

    public boolean isAuxiliaryCheck() {
        return auxiliaryCheck;
    }

    public void setAuxiliaryCheck(boolean auxiliaryCheck) {
        this.auxiliaryCheck = auxiliaryCheck;
    }

    public boolean isHasSign() {
        return hasSign;
    }

    public void setHasSign(boolean hasSign) {
        this.hasSign = hasSign;
    }

    public boolean isEverSign() {
        return everSign;
    }

    public void setEverSign(boolean everSign) {
        this.everSign = everSign;
    }

    public BigDecimal getPendingCoins() {
        return PendingCoins;
    }

    public void setPendingCoins(BigDecimal pendingCoins) {
        PendingCoins = pendingCoins;
    }

    public List<OrderVo> getTranscations() {
        return transcations;
    }

    public void setTranscations(List<OrderVo> transcations) {
        this.transcations = transcations;
    }

    public Integer getNextTimeCoin() {
        return nextTimeCoin;
    }

    public void setNextTimeCoin(Integer nextTimeCoin) {
        this.nextTimeCoin = nextTimeCoin;
    }

    public BigDecimal getVerifiedCoins() {
        return verifiedCoins;
    }

    public void setVerifiedCoins(BigDecimal verifiedCoins) {
        this.verifiedCoins = verifiedCoins;
    }

    public Integer getThisTimeCoin() {
        return thisTimeCoin;
    }

    public void setThisTimeCoin(Integer thisTimeCoin) {
        this.thisTimeCoin = thisTimeCoin;
    }

    public Integer getMaxConSignNum() {
        return maxConSignNum;
    }

    public void setMaxConSignNum(Integer maxConSignNum) {
        this.maxConSignNum = maxConSignNum;
    }
}
