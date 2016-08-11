package hasoffer.core.push.impl;

import hasoffer.base.utils.JSONUtil;
import hasoffer.core.bo.push.AppPushBo;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.push.IPushService;
import hasoffer.core.utils.Httphelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date : 2016/4/27
 * Function :
 */
@Service
public class PushServiceImpl implements IPushService {
    private static final String Q_URM_GET_GCMTOKENS =
            "SELECT t FROM UrmDevice t " +
                    " WHERE t.appVersion = ?0 ";
    @Resource
    private IDataBaseManager dbm;

    @Override
    public void push(String to, AppPushBo pushBo) {

        Map<String, String> header = new HashMap<String, String>();
        Map<String, Object> body = new HashMap<String, Object>();

        Map<String, Object> datas = new HashMap<String, Object>();

        header.put("Content-Type", "application/json");
        header.put("Authorization", "key=AIzaSyCZrHjOkZ57j3Dvq_TpvYW8Mt38Ej1dzQA");

        datas.put("data", pushBo);
        datas.put("to", to);

        String datasJson = JSONUtil.toJSON(datas);

//        body.put("", datasJson);
//        body.put("data", JSONUtil.toJSON(pushBo));

        //HttpResponseModel responseModel = HttpUtils.postByRaw("https://gcm-http.googleapis.com/gcm/send", datasJson, header);
        String postResult = "";
        try {
            postResult = Httphelper.doPostJsonWithHeader("https://gcm-http.googleapis.com/gcm/send", datasJson, header);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("exception Message :" + e.getMessage());
        }
        System.out.println("postResult :" + postResult);
    }

    @Override
    public List<UrmDevice> getGcmTokens(String version) {
        return dbm.query(Q_URM_GET_GCMTOKENS, Arrays.asList(version));
    }

    @Override
    public void sendPush(int page, int size) {
        
    }
}
