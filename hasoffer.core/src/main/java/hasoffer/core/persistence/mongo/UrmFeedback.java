package hasoffer.core.persistence.mongo;

import hasoffer.base.utils.TimeUtils;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

/**
 * Date : 2016/3/31
 * Function :
 */
@Document(collection = "UrmFeedback")
public class UrmFeedback {

    @Id
    private String id;
    private Date createTime = TimeUtils.nowDate();

    private String deviceId;

    private String content;

    private int type;


}
