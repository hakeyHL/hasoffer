package hasoffer.core.push.impl;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import hasoffer.base.enums.MarketChannel;
import hasoffer.base.model.PageableResult;
import hasoffer.base.utils.JSONUtil;
import hasoffer.core.bo.push.AppPushBo;
import hasoffer.core.persistence.dbm.osql.IDataBaseManager;
import hasoffer.core.persistence.enums.PushSourceType;
import hasoffer.core.persistence.po.app.AppPush;
import hasoffer.core.persistence.po.urm.UrmDevice;
import hasoffer.core.push.IPushService;
import hasoffer.core.utils.Httphelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * Date : 2016/4/27
 * Function :
 */
@Service
public class PushServiceImpl implements IPushService {
    private static final String Q_URM_GET_GCMTOKENS =
            "SELECT t FROM UrmDevice t " +
                    " WHERE t.appVersion = ?0 ";
    private static final String Q_APPVERSION_GET_MARKETCHANNELS =
            "SELECT  DISTINCT t.marketChannel from AppVersion  t";
    private static final String Q_APPVERSION_GET_ALLVERSIONS =
            "SELECT DISTINCT t.version  from AppVersion t where t.appType='APP'";
    Logger logger = LoggerFactory.getLogger(PushServiceImpl.class);
    @Resource
    private IDataBaseManager dbm;

    @Override
    public String push(String to, AppPushBo pushBo) {

        Map<String, String> header = new HashMap<String, String>();
        Map<String, Object> body = new HashMap<String, Object>();

        Map<String, Object> datas = new HashMap<String, Object>();

        header.put("Content-Type", "application/json");
        header.put("Authorization", "key=AIzaSyCZrHjOkZ57j3Dvq_TpvYW8Mt38Ej1dzQA");

        datas.put("data", pushBo);
        datas.put("to", to);

        String datasJson = JSONUtil.toJSON(datas);
        String postResult = null;
        try {
            postResult = Httphelper.doPostJsonWithHeader("https://gcm-http.googleapis.com/gcm/send", datasJson, header);
        } catch (Exception e) {
            postResult = "{\n" +
                    "  \"success\": 0,\n" +
                    "  \"failure\": 1\n" +
                    "}";
            e.printStackTrace();
            System.out.println("exception Message :" + e.getMessage());
        }
        System.out.println("postResult :" + postResult);
        return postResult;
    }

    @Override
    public List<UrmDevice> getGcmTokens(String version) {
        return dbm.query(Q_URM_GET_GCMTOKENS, Arrays.asList(version));
    }

    @Override
    public void sendPush(int page, int size) {
    }

    @Override
    public List<MarketChannel> getAllMarketChannels() {

        return dbm.query(Q_APPVERSION_GET_MARKETCHANNELS);
    }

    @Override
    public List<String> getAllAppVersions() {

        return dbm.query(Q_APPVERSION_GET_ALLVERSIONS);
    }

    @Override
    public MulticastResult GroupPush(List<String> gcmTokens, AppPushBo pushBo) throws Exception {
        Sender sender = new Sender("AIzaSyCZrHjOkZ57j3Dvq_TpvYW8Mt38Ej1dzQA");
        String userMessage = JSONUtil.toJSON(pushBo.getMessage());
        logger.info("DealPush userMessage" + userMessage);
        Message message = new Message.Builder().timeToLive(30).delayWhileIdle(true).addData("message", userMessage).build();
        MulticastResult result = sender.send(message, gcmTokens, 1);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppPush createAppPush(AppPush appPush) {
        Long aLong = dbm.create(appPush);
        appPush.setId(aLong);
        return appPush;
    }

    @Override
    public PageableResult getPagedAppPush(PushSourceType pushSourceType, Date startDate, Date endDate, int curPage, int pageSize) {
        return dbm.queryPage("SELECT t FROM AppPush t WHERE t.pushSourceType = ?0 AND t.createTime > ?1 AND t.createTime < ?2  ORDER BY t.id ASC", curPage, pageSize, Arrays.asList(pushSourceType, startDate, endDate));
    }
}
