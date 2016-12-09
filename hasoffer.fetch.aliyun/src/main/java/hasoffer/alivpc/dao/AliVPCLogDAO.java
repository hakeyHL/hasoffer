package hasoffer.alivpc.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;

public interface AliVPCLogDAO {


    @Insert({
            "INSERT INTO t_ali_vpc_log (eipId, eipIp, startTime, startReqId, status) VALUES (#{eipId,jdbcType=VARCHAR},#{eipIp,jdbcType=VARCHAR}, #{startTime, jdbcType=TIMESTAMP},#{reqId,jdbcType=VARCHAR},'Y')"
    })
    void insertTimeLog(@Param("startTime") Date startTime, @Param("eipId") String eipId, @Param("eipIp") String eipIp, @Param("reqId") String reqId);

    @Update({
            "<script>",
            "update t_ali_vpc_log set endTime=#{endTime, jdbcType=TIMESTAMP}, endReqId=#{reqId,jdbcType=VARCHAR}, status='N' where eipId = #{eipId,jdbcType=VARCHAR}",
            "</script>"
    })
    void updateEndTimeLog(@Param("endTime") Date endTime, @Param("eipId") String eipId, @Param("reqId") String reqId);
}
