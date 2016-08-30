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
    private Long startPoint;//最低点 ,起始数据点
    private Long endPoint;//终止数据点

    public PriceCurveVo(List<String> showX, List<Long> showY, Map<String, Float> priceXY, Long startPoint, Long endPoint) {
        this.showX = showX;
        this.showY = showY;
        this.priceXY = priceXY;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public PriceCurveVo() {
    }

    public Long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Long startPoint) {
        this.startPoint = startPoint;
    }

    public Long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Long endPoint) {
        this.endPoint = endPoint;
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
