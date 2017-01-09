package hasoffer.state.dao;

import hasoffer.state.dmo.MatchStateDMO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MatchStateDAO {


    @Insert({
            "INSERT INTO t_product_match_stats (updateDate, webSite, pushNum, finishNum, exceptionNum) ",
            " VALUES (#{dmo.updateDate,jdbcType=CHAR}, #{dmo.webSite,jdbcType=VARCHAR},  #{dmo.pushNum,jdbcType=INTEGER},  #{dmo.finishNum,jdbcType=INTEGER},  #{dmo.exceptionNum,jdbcType=INTEGER}, #{dmo.logTime,jdbcType=TIMESTAMP} )"
    })
    void insert(@Param("dmo") MatchStateDMO dmo);

    @Update({
            "<script>",
            " UPDATE t_product_match_stats SET pushNum=#{dmo.pushNum,jdbcType=INTEGER}, finishNum=#{dmo.finishNum,jdbcType=INTEGER}, exceptionNum=#{dmo.exceptionNum,jdbcType=INTEGER}, logTime=#{dmo.logTime,jdbcType=TIMESTAMP} ",
            " WHERE webSite=#{dmo.webSite,jdbcType=VARCHAR} and updateDate=#{dmo.updateDate,jdbcType=CHAR} ",
            "</script>"
    })
    void update(@Param("dmo") MatchStateDMO dmo);

    @Select({
            "<script>",
            "select updateDate,webSite,pushNum,finishNum,exceptionNum,logTime from t_product_match_stats where 1=1 ",
            "<if test=\"queryDay!=null \">",
            " and updateDate=#{queryDay} ",
            "</if>",
            "<if test=\"webSite!=null and webSite!='' \">",
            " and webSite=#{webSite}",
            "</if>",
            "</script>"
    })
    List<MatchStateDMO> selectStats(@Param("queryDay") String queryDay, @Param("webSite") String webSite);
}
