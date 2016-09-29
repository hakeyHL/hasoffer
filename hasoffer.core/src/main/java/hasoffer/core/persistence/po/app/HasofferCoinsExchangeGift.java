package hasoffer.core.persistence.po.app;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Created on 2016/9/29.
 */
@Entity
public class HasofferCoinsExchangeGift implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;//礼品的title
    private Float rePrice;//Rs:1500
    private Float coinPrice;//1500coins;

    private String imagePath;//用来存放奖项的图片路径

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long aLong) {
        this.id = aLong;
    }

    public Float getCoinPrice() {
        return coinPrice;
    }

    public void setCoinPrice(Float coinPrice) {
        this.coinPrice = coinPrice;
    }

    public Float getRePrice() {
        return rePrice;
    }

    public void setRePrice(Float rePrice) {
        this.rePrice = rePrice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HasofferCoinsExchangeGift gift = (HasofferCoinsExchangeGift) o;

        if (id != null ? !id.equals(gift.id) : gift.id != null) return false;
        if (title != null ? !title.equals(gift.title) : gift.title != null) return false;
        if (rePrice != null ? !rePrice.equals(gift.rePrice) : gift.rePrice != null) return false;
        if (coinPrice != null ? !coinPrice.equals(gift.coinPrice) : gift.coinPrice != null) return false;
        return !(imagePath != null ? !imagePath.equals(gift.imagePath) : gift.imagePath != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (rePrice != null ? rePrice.hashCode() : 0);
        result = 31 * result + (coinPrice != null ? coinPrice.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        return result;
    }
}
