package hasoffer.api.controller;

import com.alibaba.fastjson.JSONObject;
import hasoffer.core.utils.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hs on 2017年02月14日.
 * Time 15:44
 */

public abstract class BaseController {
    public Logger logger = LoggerFactory.getLogger(getClass());
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    JSONObject dataJsonObj = new JSONObject();
    Map dataMap = new ConcurrentHashMap();
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

    @ModelAttribute
    void initErrorCodeAndMsgSuccess() {
        resultJsonObj.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        resultJsonObj.put(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_SUCCESS);
        modelAndView.addObject(ConstantUtil.API_NAME_MSG, ConstantUtil.API_NAME_MSG_SUCCESS);
        dataJsonObj.clear();
        dataMap.clear();
    }

    @ModelAttribute
    protected void initRequestResponse(HttpServletRequest request, HttpServletResponse response
    ) {
        this.request = request;
        this.response = response;
    }

    void initErrorCodeAndMsgFail() {
        resultJsonObj.put(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        modelAndView.addObject(ConstantUtil.API_NAME_ERRORCODE, ConstantUtil.API_ERRORCODE_FAILED_LOGIC);
        dataJsonObj.clear();
        dataMap.clear();
    }
}
