package hasoffer.core.persistence.po.match;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.utils.IdWorker;
import hasoffer.nlp.core.model.HasTag;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Date : 2016/6/16
 * Function :
 */
@Entity
public class TagCategory extends HasTag implements Identifiable<Long> {

    private static final IdWorker idWorker = IdWorker.getInstance(TagCategory.class);

    @Id
    @Column(unique = true, nullable = false)
    private Long id = idWorker.nextLong();

    @Column(unique = true, nullable = true)
    private String tag; // tag

    private String alias; // 别名

    private int score; // 打分

    public TagCategory() {
    }

    public TagCategory(String tag, String alias, int score) {
        this.tag = tag;
        this.alias = alias;
        this.score = score;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagCategory that = (TagCategory) o;

        if (score != that.score) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        return !(alias != null ? !alias.equals(that.alias) : that.alias != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + score;
        return result;
    }
}
