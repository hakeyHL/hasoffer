package hasoffer.core.persistence.po.app;

import hasoffer.core.persistence.dbm.osql.Identifiable;
import hasoffer.core.persistence.enums.PushSourceType;

import javax.persistence.*;
import java.util.Date;

/**
 * Created on 2016/9/14.
 */
@Entity
public class AppPush implements Identifiable<Long> {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private PushSourceType pushSourceType;//push来源类型
    private Date createTime;//push创建时间
    private String sourceId;//来源id，官方定义:配置参数
    private String title;//推送文案标题
    private String content;//推送文案详情

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long aLong) {
        this.id = aLong;
    }

}
