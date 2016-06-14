package hasoffer.timer.msp.vo;

import org.htmlcleaner.TagNode;

/**
 * Created on 2016/2/19.
 */
public class MspUnCmpModel {

    private String sourceId;
    private TagNode tagNode;
    private Long categoryId;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public TagNode getTagNode() {
        return tagNode;
    }

    public void setTagNode(TagNode tagNode) {
        this.tagNode = tagNode;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
