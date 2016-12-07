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
            " INSERT INTO t_update_url_stats (taskTarget, updateDate, pushNum, finishNum, exceptionNum, stopNum, logTime) ",
            " VALUES (#{dmo.taskTarget,jdbcType=VARCHAR}, #{dmo.updateDate,jdbcType=CHAR},  #{dmo.pushNum,jdbcType=INTEGER},  #{dmo.finishNum,jdbcType=INTEGER},  #{dmo.exceptionNum,jdbcType=INTEGER},  #{dmo.stopNum,jdbcType=INTEGER}, #{dmo.logTime,jdbcType=TIMESTAMP} )"
    })
    void insert(@Param("dmo") UpdateStateDMO dmo);

    @Select({
            "<script>",
            "select taskTarget, updateDate, pushNum, finishNum, exceptionNum, stopNum from t_update_url_stats where updateDate=#{queryDate,jdbcType=CHAR} and taskTarget=#{taskTarget,jdbcType=VARCHAR}",
            "</script>"
    })
    List<UpdateStateDMO> selectByTaskTargetDate(@Param("taskTarget") String taskTarget, @Param("queryDate") String date);

    @Update({
            "<script>",
            " UPDATE t_update_url_stats SET pushNum=#{dmo.pushNum,jdbcType=INTEGER}, finishNum=#{dmo.finishNum,jdbcType=INTEGER}, exceptionNum=#{dmo.exceptionNum,jdbcType=INTEGER}, stopNum= #{dmo.stopNum,jdbcType=INTEGER}, logTime=#{dmo.logTime,jdbcType=TIMESTAMP} ",
            " WHERE taskTarget=#{dmo.taskTarget,jdbcType=VARCHAR} and updateDate=#{dmo.updateDate,jdbcType=CHAR} ",
            "</script>"
    })
    void update(@Param("dmo") UpdateStateDMO dmo);

    @Select({
            "<script>",
            "select taskTarget, updateDate, pushNum, finishNum, exceptionNum, stopNum from t_update_url_stats  where updateDate=#{queryDate,jdbcType=CHAR}",
            "</script>"
    })
    List<UpdateStateDMO> selectByDate(@Param("queryDate") String date);


    @Select({
            "<script>",
            "select updateDate, sum(pushNum) as pushNum, sum(finishNum) as finishNum, sum(exceptionNum) as exceptionNum, sum(stopNum) as stopNum from t_update_url_stats where updateDate &gt;=#{queryDate,jdbcType=CHAR} and updateDate &lt;=#{queryDate,jdbcType=CHAR} GROUP BY updateDate",
            "</script>"
    })
    List<Map<String, Integer>> selectGroupByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
