package hasoffer.core.persistence.po.ptm;

import hasoffer.base.utils.HexDigestUtil;
import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2015/12/7.
 */
@Entity
public class PtmImage2 implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String proUrlId;

    private String proUrl;

    private String imageUrlL;
    private String imageUrlM;
    private String imageUrlS;

    public PtmImage2(String proUrl, String imageUrlL, String imageUrlM, String imageUrlS) {
        this.proUrlId = HexDigestUtil.md5(proUrl);
        this.proUrl = proUrl;
        this.imageUrlL = imageUrlL;
        this.imageUrlM = imageUrlM;
        this.imageUrlS = imageUrlS;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getProUrlId() {
        return proUrlId;
    }

    public void setProUrlId(String proUrlId) {
        this.proUrlId = proUrlId;
    }

    public String getProUrl() {
        return proUrl;
    }

    public void setProUrl(String proUrl) {
        this.proUrl = proUrl;
    }

    public String getImageUrlL() {
        return imageUrlL;
    }

    public void setImageUrlL(String imageUrlL) {
        this.imageUrlL = imageUrlL;
    }

    public String getImageUrlM() {
        return imageUrlM;
    }

    public void setImageUrlM(String imageUrlM) {
        this.imageUrlM = imageUrlM;
    }

    public String getImageUrlS() {
        return imageUrlS;
    }

    public void setImageUrlS(String imageUrlS) {
        this.imageUrlS = imageUrlS;
    }
}
