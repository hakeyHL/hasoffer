package hasoffer.admin.dao;

import hasoffer.admin.po.SysAdmin;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface AdminDAO {
    @Select({
            "<script>",
            " select id,uname,ukey,`password`,valid,email from sysadmin ",
            " WHERE uname = #{uname}",
            "</script>"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "uname", property = "uname", jdbcType = JdbcType.VARCHAR),
            @Result(column = "ukey", property = "ukey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
            @Result(column = "valid", property = "valid", jdbcType = JdbcType.VARCHAR),
            @Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
    })
    SysAdmin querySingle(@Param("uname") String uname);

    @Select({
            "<script>",
            " select id,uname,ukey,`password`,valid,email from sysadmin ",
            " WHERE ukey = #{ukey}",
            "</script>"
    })
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT),
            @Result(column = "uname", property = "uname", jdbcType = JdbcType.VARCHAR),
            @Result(column = "ukey", property = "ukey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
            @Result(column = "valid", property = "valid", jdbcType = JdbcType.VARCHAR),
            @Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
    })
    SysAdmin querySingleByUKey(@Param("ukey") String ukey);

    @Update({
            "<script>",
            " update sysadmin set ukey=#{admin.ukey,jdbcType=VARCHAR},lastLoginTime=#{admin.lastLoginTime,jdbcType=TIMESTAMP}",
            " WHERE uname=#{admin.uname,jdbcType=VARCHAR}",
            "</script>"
    })
    void updateUkey(@Param("admin") SysAdmin sysadmin);
}