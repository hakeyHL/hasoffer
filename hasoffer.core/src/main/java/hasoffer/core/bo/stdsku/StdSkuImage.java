package hasoffer.core.bo.stdsku;

import hasoffer.core.persistence.po.ptm.PtmStdImage;

/**
 * Created by chevy on 2016/11/16.
 */
public class StdSkuImage {

    private Long id;

    private long stdProId; // ptm_std_product # 一定不为 0
    private long stdSkuId; // ptm_std_sku # 如果不能判定是哪个sku的，则为0

    private String oriImageUrl;// 原图片url

    private String oriImagePath; // 下载后的图片路径
    private String smallImagePath;
    private String bigImagePath;

    public StdSkuImage(Long id, long stdProId, long stdSkuId, String oriImageUrl, String oriImagePath, String smallImagePath, String bigImagePath) {
        this.id = id;
        this.stdProId = stdProId;
        this.stdSkuId = stdSkuId;
        this.oriImageUrl = oriImageUrl;
        this.oriImagePath = oriImagePath;
        this.smallImagePath = smallImagePath;
        this.bigImagePath = bigImagePath;
    }

    public StdSkuImage(PtmStdImage stdImage) {
        this(stdImage.getId(), stdImage.getStdProId(), stdImage.getStdSkuId(), stdImage.getOriImageUrl(), stdImage.getOriImagePath(), stdImage.getSmallImagePath(), stdImage.getBigImagePath());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getStdProId() {
        return stdProId;
    }

    public void setStdProId(long stdProId) {
        this.stdProId = stdProId;
    }

    public long getStdSkuId() {
        return stdSkuId;
    }

    public void setStdSkuId(long stdSkuId) {
        this.stdSkuId = stdSkuId;
    }

    public String getOriImageUrl() {
        return oriImageUrl;
    }

    public void setOriImageUrl(String oriImageUrl) {
        this.oriImageUrl = oriImageUrl;
    }

    public String getOriImagePath() {
        return oriImagePath;
    }

    public void setOriImagePath(String oriImagePath) {
        this.oriImagePath = oriImagePath;
    }

    public String getSmallImagePath() {
        return smallImagePath;
    }

    public void setSmallImagePath(String smallImagePath) {
        this.smallImagePath = smallImagePath;
    }

    public String getBigImagePath() {
        return bigImagePath;
    }

    public void setBigImagePath(String bigImagePath) {
        this.bigImagePath = bigImagePath;
    }
}
