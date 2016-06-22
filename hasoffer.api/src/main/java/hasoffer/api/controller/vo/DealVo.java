package hasoffer.api.controller.vo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hs on 2016/6/21.
 */
public class DealVo {
    private  Long id;
    private  String image;
    private  String title;
    private  String exp;
    private  Double extra;
    private  String link;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(Date time) {
        this.exp=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(time);
    }

    public Double getExtra() {
        return extra;
    }

    public void setExtra(Double extra) {
        this.extra = extra;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
