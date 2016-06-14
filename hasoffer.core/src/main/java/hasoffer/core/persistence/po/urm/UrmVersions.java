package hasoffer.core.persistence.po.urm;

import hasoffer.core.persistence.dbm.osql.Identifiable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by freeman on 16/4/13.
 */
@Entity
@Deprecated
public class UrmVersions implements Identifiable<String> {

    @Id
    @Column(unique = true, nullable = false)
    private String id;
    private String name;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name= name;
    }

}
