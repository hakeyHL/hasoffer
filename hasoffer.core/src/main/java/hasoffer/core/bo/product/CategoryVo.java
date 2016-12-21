package hasoffer.core.bo.product;

import hasoffer.core.persistence.po.ptm.PtmCategory;

import java.util.List;

/**
 * 商品类目VO
 * Created by hs on 2016/6/20.
 */
public class CategoryVo {

    List<CategoryVo> categorys;
    private String name;
    private String image;
    private Long id;
    private int hasChildren;
    private Long parentId;
    private int rank;
    private int level;

    public CategoryVo() {
    }

    public CategoryVo(Long id, String name, String image, int hasChildren, Long parentId, int rank, int level) {
        this.name = name;
        this.image = image;
        this.id = id;
        this.hasChildren = hasChildren;
        this.parentId = parentId;
        this.rank = rank;
        this.level = level;
    }

    public CategoryVo(PtmCategory ptmCategory) {
        this.name = ptmCategory.getName();
        this.image = ptmCategory.getImageUrl();
        this.id = ptmCategory.getId();
        this.parentId = ptmCategory.getParentId();
        this.rank = ptmCategory.getRank();
        this.level = ptmCategory.getLevel();
        this.hasChildren = 1;
    }

    public List<CategoryVo> getCategorys() {
        return categorys;
    }

    public void setCategorys(List<CategoryVo> categorys) {
        this.categorys = categorys;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(int hasChildren) {
        this.hasChildren = hasChildren;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
