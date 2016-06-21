package hasoffer.core.bo.common;

/**
 * Created by chevy on 2016/6/21.
 */
public class ImagePath {

    private String originalPath;

    private String smallPath;

    private String bigPath;

    public ImagePath(String originalPath, String smallPath, String bigPath) {
        this.originalPath = originalPath;
        this.smallPath = smallPath;
        this.bigPath = bigPath;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getSmallPath() {
        return smallPath;
    }

    public void setSmallPath(String smallPath) {
        this.smallPath = smallPath;
    }

    public String getBigPath() {
        return bigPath;
    }

    public void setBigPath(String bigPath) {
        this.bigPath = bigPath;
    }

    @Override
    public String toString() {
        return "ImagePath{" +
                "originalPath='" + originalPath + '\'' +
                ", smallPath='" + smallPath + '\'' +
                ", bigPath='" + bigPath + '\'' +
                '}';
    }
}
