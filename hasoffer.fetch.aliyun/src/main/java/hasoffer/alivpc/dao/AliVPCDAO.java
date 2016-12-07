package hasoffer.alivpc.dao;

import hasoffer.alivpc.dmo.AliVPCDMO;
import hasoffer.aliyun.api.model.EipAddressModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AliVPCDAO {

    @Select({
            "<script>",
            "select `id`, `ecsInstance`, `privateIpAddress`, `eipId`, `eipIpAddress` from t_ali_vpc",
            "</script>"
    })
    List<AliVPCDMO> queryAllVPCList();

    @Update({
            "<script>",
            "update t_ali_vpc set eipId=#{eip.allocationId, jdbcType=VARCHAR},eipIpAddress=#{eip.eipAddress,jdbcType=VARCHAR} where ecsInstance=#{ecsId} ",
            "</script>"
    })
    void updateEipInfo(@Param("ecsId") String ecsInstance, @Param("eip") EipAddressModel eipAddressModel);
}
