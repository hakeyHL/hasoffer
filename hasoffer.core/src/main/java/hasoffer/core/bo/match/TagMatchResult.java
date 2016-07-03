package hasoffer.core.bo.match;

/**
 * Created by chevy on 2016/7/3.
 */
public class TagMatchResult {

    private String tag;

    private ITag iTag;

    public TagMatchResult(String tag, ITag iTag) {
        this.tag = tag;
        this.iTag = iTag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ITag getiTag() {
        return iTag;
    }

    public void setiTag(ITag iTag) {
        this.iTag = iTag;
    }
}
