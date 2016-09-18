package hasoffer.api.helper;

import hasoffer.api.controller.vo.DeviceInfoVo;
import hasoffer.base.utils.HexDigestUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date : 2016/3/29
 * Function :
 */
public class ClientHelper {

    private final static String CLIENT_KEY = "13213213213132132132";

    public static String getRequestKey(DeviceInfoVo deviceInfoVo, Map<String, String> requestParams, String uri) {
        StringBuilder sBuilder = new StringBuilder();

        sBuilder.append(uri);
        sBuilder.append(sort4requestKey(requestParams));
        sBuilder.append(deviceInfoVo.getDeviceId());
        sBuilder.append(CLIENT_KEY);
        sBuilder.append(sBuilder.length());

        String keyBeforeMD5 = sBuilder.toString();
//        System.out.println(keyBeforeMD5);

        return HexDigestUtil.md5(keyBeforeMD5).toUpperCase();
    }

    private static String sort4requestKey(Map<String, String> param) {
        String[] keyArray = param.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : keyArray) {
            String value = param.get(key);
            if (value != null && !value.equals("")) {
                stringBuilder.append(key).append(param.get(key));
            }
        }
        return stringBuilder.toString();
    }

    public static int returnNumberBetween0And5(Long number) {
        //取得其余数
        Long tempNumber = number % 10;
        if (tempNumber > 0 && tempNumber <= 5) {
            number = (number / 10) * 10 + 5;
        } else if (tempNumber > 5) {
            number = (number / 10) * 10 + 10;
        }
        return number.intValue();
    }

    public static void main(String[] args) {
        int i = ClientHelper.returnNumberBetween0And5(Long.valueOf(0));
        System.out.println(i);
        String title = "samsung case";
        String keyword = "samsung";
        boolean b = FilterProducts(title, keyword);
        if (b) {
            System.out.println("搜索结果的title中有配件,关键词中没有的判断结果是 true, 要");
        } else {
            System.out.println("搜索结果的title中有配件,关键词中没有的判断结果是 true, 不要");
        }

        String title1 = "samsung case";
        String keyword1 = "samsungcase";
        boolean b1 = FilterProducts(title1, keyword1);
        if (b1) {
            System.out.println("搜索结果的title中有配件,关键词中有的判断结果是 true, 要");
        } else {
            System.out.println("搜索结果的title中有配件,关键词中有的判断结果是 false, 不要");
        }

        String title2 = "samsung";
        String keyword2 = "samsungcase";
        boolean b2 = FilterProducts(title2, keyword2);
        if (b2) {
            System.out.println("搜索结果的title中无配件,关键词中有的判断结果是 true, 要");
        } else {
            System.out.println("搜索结果的title中无配件,关键词中有的判断结果是 false, 不要");
        }

        String title3 = "samsung";
        String keyword3 = "samsung as";
        boolean b3 = FilterProducts(title3, keyword3);
        if (b3) {
            System.out.println("搜索结果的title中无配件,关键词中无的判断结果是 true, 要");
        } else {
            System.out.println("搜索结果的title中无配件,关键词中无的判断结果是 false, 不要");
        }
    }

    public static String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); //过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }

    public static boolean FilterProducts(String title, String keyword) {
        String[] filterWords = new String[]{"case", "cover", "glass", "battery", "for", "back", "phone", "guard", "cable"};
        for (String str : filterWords) {
            if (title.trim().contains(str)) {
                //如果搜索结果中包含配件名称,看关键词中有没有
                if (keyword.trim().contains(str)) {
                    //如果关键词中也有,ok
                    return true;
                } else {
                    //关键词中没有,filter
                    return false;
                }
            } else {
                //如果搜索结果中不包含配件名称,看关键词中有没有
                if (keyword.trim().contains(str)) {
                    //如果关键词中有,filter
                    return false;
                } else {
                    //关键词中没有,ok
                    return true;
                }
            }
        }
        //默认放行
        return true;
    }
}
