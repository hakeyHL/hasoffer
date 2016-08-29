package hasoffer.api.controller.vo;

import java.util.List;
import java.util.Map;

/**
 * Created by hs on 2016年08月29日.
 * Time 18:16
 * 价格曲线vo对象
 */
public class PriceCurveVo {
    List<Long> showY;
    private List<String> showX;
    private Map<String, Float> priceXY;
    private Long minPoint;//最低点 ,起始数据点
    private Long maxPoint;//终止数据点

    public PriceCurveVo(List<String> showX, List<Long> showY, Map<String, Float> priceXY, Long minPoint, Long maxPoint) {
        this.showX = showX;
        this.showY = showY;
        this.priceXY = priceXY;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public PriceCurveVo() {
    }

    public Long getMinPoint() {
        return minPoint;
    }

    public void setMinPoint(Long minPoint) {
        this.minPoint = minPoint;
    }

    public Long getMaxPoint() {
        return maxPoint;
    }

    public void setMaxPoint(Long maxPoint) {
        this.maxPoint = maxPoint;
    }

    public List<Long> getShowY() {
        return showY;
    }

    public void setShowY(List<Long> showY) {
        this.showY = showY;
    }

    public List<String> getShowX() {
        return showX;
    }

    public void setShowX(List<String> showX) {
        this.showX = showX;
    }

    public Map<String, Float> getPriceXY() {
        return priceXY;
    }

    public void setPriceXY(Map<String, Float> priceXY) {
        this.priceXY = priceXY;
    }
}
