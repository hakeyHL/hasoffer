package hasoffer.core.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;

/**
 * Created on 2016/12/26.
 * 品牌card
 */
@Document(collection = "PtmStdBrandCard")
public class PtmStdBrandCard {

    @Id
    @Column(unique = true, nullable = false)
    private String id;//MD5(brandName.upperCase())

    private String brandName;

    private String brandCardString;

    public PtmStdBrandCard() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrandCardString() {
        return brandCardString;
    }

    public void setBrandCardString(String brandCardString) {
        this.brandCardString = brandCardString;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PtmStdBrandCard that = (PtmStdBrandCard) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (brandName != null ? !brandName.equals(that.brandName) : that.brandName != null) return false;
        return !(brandCardString != null ? !brandCardString.equals(that.brandCardString) : that.brandCardString != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (brandName != null ? brandName.hashCode() : 0);
        result = 31 * result + (brandCardString != null ? brandCardString.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PtmStdBrandCard{" +
                "brandCardString='" + brandCardString + '\'' +
                ", id='" + id + '\'' +
                ", brandName='" + brandName + '\'' +
                '}';
    }
}
