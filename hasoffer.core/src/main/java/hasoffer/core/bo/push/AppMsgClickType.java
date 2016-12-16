package hasoffer.core.bo.push;

/**
 * Date : 2016/4/28
 * Function :
 */
public enum AppMsgClickType {

    ACCESS,
    MAIN,
    WEBVIEW,
    SIGN,
    SKU,

    GOOGLEPLAY,

    //--------跳转浏览器，忽略设备shopApp的问题
    DEEPLINK,

    //--------跳转hasoffer的app里面的页面
    PRODUCT,
    DEAL
}

