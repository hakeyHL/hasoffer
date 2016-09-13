package hasoffer.core.persistence.po.match;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.*;

/**
 * Date : 2016/6/16
 * Function :
 * spell check
 */
@Entity
public class TagSearch implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String tag; // tag

    public TagSearch() {
    }

    public TagSearch(String tag) {
        this();
        this.tag = tag;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagSearch tagSearch = (TagSearch) o;

        return tag.equals(tagSearch.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}
