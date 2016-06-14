package hasoffer.core.push.impl;

import hasoffer.base.model.HttpResponseModel;
import hasoffer.base.utils.JSONUtil;
import hasoffer.base.utils.http.HttpUtils;
import hasoffer.core.bo.push.AppPushBo;
import hasoffer.core.push.IPushService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Date : 2016/4/27
 * Function :
 */
@Service
public class PushServiceImpl implements IPushService {

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

        HttpResponseModel responseModel = HttpUtils.postByRaw("https://gcm-http.googleapis.com/gcm/send", datasJson, header);

        System.out.println(responseModel.getBodyString());
    }
}
