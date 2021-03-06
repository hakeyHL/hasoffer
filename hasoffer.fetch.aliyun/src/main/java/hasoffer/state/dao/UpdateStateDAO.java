package hasoffer.state.dao;

import hasoffer.state.dmo.UpdateStateDMO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UpdateStateDAO {

    @Insert({
            " INSERT INTO t_update_url_stats (taskTarget, updateDate, webSite, pushNum, finishNum, exceptionNum, stopNum, logTime) ",
            " VALUES (#{dmo.taskTarget,jdbcType=VARCHAR}, #{dmo.updateDate,jdbcType=CHAR}, #{dmo.webSite,jdbcType=VARCHAR},  #{dmo.pushNum,jdbcType=INTEGER},  #{dmo.finishNum,jdbcType=INTEGER},  #{dmo.exceptionNum,jdbcType=INTEGER},  #{dmo.stopNum,jdbcType=INTEGER}, #{dmo.logTime,jdbcType=TIMESTAMP} )"
    })
    void insert(@Param("dmo") UpdateStateDMO dmo);

    @Select({
            "<script>",
            "select taskTarget, updateDate, webSite, pushNum, finishNum, exceptionNum, stopNum from t_update_url_stats where updateDate=#{queryDate,jdbcType=CHAR} and taskTarget=#{taskTarget,jdbcType=VARCHAR} and webSite= #{webSite,jdbcType=VARCHAR}",
            "</script>"
    })
    List<UpdateStateDMO> selectByTaskTargetDate(@Param("queryDate") String updateStr, @Param("taskTarget") String taskTarget, @Param("webSite") String webSite);

    @Update({
            "<script>",
            " UPDATE t_update_url_stats SET pushNum=#{dmo.pushNum,jdbcType=INTEGER}, finishNum=#{dmo.finishNum,jdbcType=INTEGER}, exceptionNum=#{dmo.exceptionNum,jdbcType=INTEGER}, stopNum= #{dmo.stopNum,jdbcType=INTEGER}, logTime=#{dmo.logTime,jdbcType=TIMESTAMP} ",
            " WHERE taskTarget=#{dmo.taskTarget,jdbcType=VARCHAR} and webSite=#{dmo.webSite,jdbcType=VARCHAR} and updateDate=#{dmo.updateDate,jdbcType=CHAR} ",
            "</script>"
    })
    void update(@Param("dmo") UpdateStateDMO dmo);

    @Select({
            "<script>",
            "select taskTarget, updateDate, sum(pushNum) as pushNum, sum(finishNum) as finishNum, sum(exceptionNum) as exceptionNum, sum(stopNum) stopNum from t_update_url_stats  where updateDate=#{queryDate,jdbcType=CHAR} GROUP BY taskTarget,updateDate",
            "</script>"
    })
    List<UpdateStateDMO> selectByDate(@Param("queryDate") String date);


    @Select({
            "<script>",
            "select updateDate, sum(pushNum) as pushNum, sum(finishNum) as finishNum, sum(exceptionNum) as exceptionNum, sum(stopNum) as stopNum from t_update_url_stats where updateDate &gt;=#{queryDate,jdbcType=CHAR} and updateDate &lt;=#{queryDate,jdbcType=CHAR} GROUP BY updateDate",
            "</script>"
    })
    List<Map<String, Integer>> selectGroupByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    @Select({
            "<script>",
            "select updateDate,taskTarget,webSite,pushNum,finishNum,exceptionNum,stopNum,logTime from t_update_url_stats where 1=1 ",
            "<if test=\"queryDay!=null \">",
            " and updateDate=#{queryDay} ",
            "</if>",
            "<if test=\"taskTarget!=null and taskTarget!='' \">",
            " and taskTarget=#{taskTarget}",
            "</if>",
            "<if test=\"webSite!=null and webSite!='' \">",
            " and webSite=#{webSite}",
            "</if>",
            "</script>"
    })
    List<UpdateStateDMO> selectStats(@Param("queryDay") String queryDay, @Param("taskTarget") String taskTarget, @Param("webSite") String webSite);
}
