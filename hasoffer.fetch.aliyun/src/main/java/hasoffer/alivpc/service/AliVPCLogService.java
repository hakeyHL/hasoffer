package hasoffer.alivpc.service;

import hasoffer.alivpc.dao.AliVPCLogDAO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class AliVPCLogService {

    @Resource
    private AliVPCLogDAO aliVPCLogDAO;


    public void updateEndTimeLog(Date endTime, String eipId, String reqId) {
        aliVPCLogDAO.updateEndTimeLog(endTime, eipId, reqId);
    }

    public void insertLog(Date date, String eipId, String eipIp, String reqId) {
        aliVPCLogDAO.insertTimeLog(date, eipId, eipIp, reqId);
    }
}
