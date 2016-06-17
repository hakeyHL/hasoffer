package hasoffer.api.controller.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hs on 2016/6/16.
 * ²é¿´·µÀûvo
 */
public class BackDetailVo {
    private Long  PendingCoins;
    private  Long   verifiedCoins;
    private List<OrderVo> transcations=new ArrayList<OrderVo>();

    public Long getPendingCoins() {
        return PendingCoins;
    }

    public void setPendingCoins(Long pendingCoins) {
        PendingCoins = pendingCoins;
    }

    public Long getVericiedCoins() {
        return verifiedCoins;
    }

    public void setVericiedCoins(Long vericiedCoins) {
        this.verifiedCoins = vericiedCoins;
    }

    public List<OrderVo> getTranscations() {
        return transcations;
    }

    public void setTranscations(List<OrderVo> transcations) {
        this.transcations = transcations;
    }
}
