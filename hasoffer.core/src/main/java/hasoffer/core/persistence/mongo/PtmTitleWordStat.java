package hasoffer.core.persistence.mongo;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

/**
 * Date : 2016/5/18
 * Function :
 */
@Document(collection = "PtmTitleWordStat")
public class PtmTitleWordStat {

    @Id
    private String id;

    private long count;

    public PtmTitleWordStat(String id, long count) {
        this.id = id;
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
