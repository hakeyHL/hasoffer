package hasoffer.proxy.dao;

import hasoffer.proxy.dmo.ProxyIPDMO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

public interface ProxyIPDAO {

    @Insert({
            " INSERT INTO t_proxy_ip (ip, xGroup, port, status, reqNum, finishNum, exceptionNum, createTime, deleteTime, deleteFlag) ",
            " VALUES (#{dmo.ip,jdbcType=VARCHAR}, #{dmo.xGroup,jdbcType=VARCHAR}, #{dmo.port,jdbcType=INTEGER}, #{dmo.status,jdbcType=CHAR},  #{dmo.reqNum,jdbcType=INTEGER},  #{dmo.finishNum,jdbcType=INTEGER},  #{dmo.exceptionNum,jdbcType=INTEGER},  #{dmo.createTime,jdbcType=TIMESTAMP}, #{dmo.deleteTime,jdbcType=TIMESTAMP}, #{dmo.deleteFlag,jdbcType=CHAR})"
    })
    void insert(@Param("dmo") ProxyIPDMO dmo);

    @Update({
            "<script>",
            " UPDATE t_proxy_ip SET id=#{dmo.id,jdbcType=INTEGER}",
            "<if test=\"dmo.ip!=null \">",
            " ,ip=#{dmo.ip,jdbcType=VARCHAR} ",
            "</if>",
            "<if test=\"dmo.port!=null \">",
            " ,port=#{dmo.port,jdbcType=INTEGER} ",
            "</if>",
            "<if test=\"dmo.status!=null \">",
            " ,status=#{dmo.status,jdbcType=CHAR} ",
            "</if>",
            "<if test=\"dmo.reqNum!=null \">",
            " ,reqNum=#{dmo.reqNum,jdbcType=INTEGER}",
            "</if>",
            "<if test=\"dmo.finishNum!=null \">",
            " ,finishNum= #{dmo.finishNum,jdbcType=INTEGER} ",
            "</if>",
            "<if test=\"dmo.exceptionNum!=null \">",
            " ,exceptionNum=#{dmo.exceptionNum,jdbcType=INTEGER}",
            "</if>",
            "<if test=\"dmo.createTime!=null \">",
            " ,createTime=#{dmo.createTime,jdbcType=TIMESTAMP} ",
            "</if>",
            "<if test=\"dmo.deleteFlag!=null \">",
            " ,deleteFlag=#{dmo.deleteFlag,jdbcType=CHAR} ",
            "</if>",
            "<if test=\"dmo.deleteTime!=null \">",
            " ,deleteTime=#{dmo.deleteTime,jdbcType=TIMESTAMP} ",
            "</if>",
            " WHERE id=#{dmo.id,jdbcType=VARCHAR}",
            "</script>"
    })
    void update(@Param("dmo") ProxyIPDMO dmo);

    @Select({
            "<script>",
            "select id, xGroup, ip, port, status, reqNum, finishNum, exceptionNum, createTime, deleteFlag, deleteTime from t_proxy_ip where 1=1 ",
            "<if test=\"req.id!=null \">",
            " and id=#{req.id} ",
            "</if>",
            "<if test=\"req.ip!=null and req.ip!='' \">",
            " and ip=#{req.ip}",
            "</if>",
            " and deleteFlag='N' ",
            " order by createTime desc",
            "</script>"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER),
            @Result(column = "xGroup", property = "xGroup", jdbcType = JdbcType.VARCHAR),
            @Result(column = "ip", property = "ip", jdbcType = JdbcType.VARCHAR),
            @Result(column = "port", property = "port", jdbcType = JdbcType.INTEGER),
            @Result(column = "status", property = "status", jdbcType = JdbcType.CHAR),
            @Result(column = "reqNum", property = "reqNum", jdbcType = JdbcType.INTEGER),
            @Result(column = "finishNum", property = "finishNum", jdbcType = JdbcType.INTEGER),
            @Result(column = "exceptionNum", property = "exceptionNum", jdbcType = JdbcType.INTEGER),
            @Result(column = "createTime", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "deleteFlag", property = "deleteFlag", jdbcType = JdbcType.CHAR),
            @Result(column = "deleteTime", property = "deleteTime", jdbcType = JdbcType.TIMESTAMP)
    })
    List<ProxyIPDMO> select(@Param("req") ProxyIPDMO proxyIPDMO);

    @Select({
            "<script>",
            "select DISTINCT xGroup from t_proxy_ip where status=#{ipState} and deleteFlag='N'",
            "</script>"
    })
    String selectGroupName(@Param("ipState") String ipState);

    @Update({
            "<script>",
            " UPDATE t_proxy_ip SET status=#{status,jdbcType=CHAR}",
            " WHERE xGroup=#{group,jdbcType=VARCHAR} and deleteFlag='N'",
            "</script>"
    })
    void updateByGroup(@Param("group") String group, @Param("status") String s);
}
