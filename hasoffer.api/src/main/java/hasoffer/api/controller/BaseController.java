package hasoffer.api.controller;

import com.alibaba.fastjson.JSONObject;
import hasoffer.core.utils.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hs on 2017年02月14日.
 * Time 15:44
 */

public abstract class BaseController {
    public Logger logger = LoggerFactory.getLogger(getClass());
    JSONObject dataJsonObj = new JSONObject();
    Map dataMap = new HashMap<>();
    ModelAndView modelAndView = new ModelAndView();
    JSONObject resultJsonObj = new JSONObject();

    public BaseController() {
        resultJsonObj.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultJsonObj.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        resultJsonObj.put(ConstantUtil.API_NAME_DATA, dataJsonObj);
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_DATA, dataMap);
    }

    Map getDataMap() {
        Object o = modelAndView.getModel().get(ConstantUtil.API_NAME_DATA);
        if (!(o instanceof Map)) {
            modelAndView.addObject(ConstantUtil.API_NAME_DATA, dataMap);
        } else {
            dataMap = (Map) o;
        }
        return dataMap;
    }

    JSONObject getJsonDataObj() {
        return (JSONObject) resultJsonObj.get(ConstantUtil.API_NAME_DATA);
    }

    void initErrorCodeAndMsgSuccess() {
        resultJsonObj.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultJsonObj.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
    }

    void initErrorCodeAndMsgFailed() {
        resultJsonObj.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        resultJsonObj.put(ConstantUtil.API_NAME_DATA, null);
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        modelAndView.addObject(ConstantUtil.API_NAME_DATA, null);
    }
}
