package hasoffer.base.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Author : CHENGWEI ZHANG
 * Date : 2015/10/19
 */
public class StringUtils {

    public static final String EMPTY_STRING = "";

    public static String arrayToString(String[] strs, String split) {
        if (strs == null || strs.length == 0) {
            return EMPTY_STRING;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = strs.length; i < len; i++) {
            sb.append(strs[i]);
            if (i != len - 1) {
                sb.append(split);
            }
        }
        return sb.toString();
    }

    public static String arrayToString(List<String> strs, String split) {
        if (ArrayUtils.isNullOrEmpty(strs)) {
            return "";
        }
        String[] strs2 = strs.toArray(new String[0]);
        return arrayToString(strs2, split);
    }

    public static String arrayToString(List<String> strs) {
        return arrayToString(strs, ",");
    }

    public static String unescapeHtml(String input) {
        return StringEscapeUtils.unescapeHtml(input);
    }

    public static String urlDecode(String input) {
        return URLDecoder.decode(input);
    }

    public static String urlEncode(String input) {
        return URLEncoder.encode(input);
    }

    public static String notNullTrim(String theString) {
        if (theString == null) {
            return EMPTY_STRING;
        }

        return theString.trim();
    }

    /**
     * 将异常的 stacktrace 转换为一个字符串
     *
     * @param e
     * @return
     */
    public static String stackTraceAsString(Throwable e) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(output);
        e.printStackTrace(ps);
        ps.flush();
        return new String(output.toByteArray());
    }

    public static boolean isEmpty(String t) {
        return org.apache.commons.lang3.StringUtils.isEmpty(t);
    }

    public static String getNewCode(int size) {
        String code = "";
        Random ran = new Random();
        for (int i = 0; i < size; i++) {
            char c = (char) (97 + ran.nextInt(26));
            code += c;
        }
        return code;
    }

    public static String[] getCleanWords(String sourceStr) {

        if (isEmpty(sourceStr)) {
            return new String[]{};
        }

        List<String> filters = Arrays.asList("[", "]", ";", "%", "$", "@", "#", "(", ")", ",");

        if (!ArrayUtils.isNullOrEmpty(filters)) {
            for (String f : filters) {
                while (sourceStr.contains(f)) {
                    sourceStr = sourceStr.replace(f, " ");
                }
            }
        }

        sourceStr = sourceStr.trim();
        return sourceStr.split("\\s+");
    }

    public static String getCleanWordString(String sourceStr) {
        String[] ss = getCleanWords(sourceStr);

        StringBuffer sb = new StringBuffer();

        for (String s : ss) {
            sb.append(s).append(" ");
        }

        return sb.toString().trim();
    }

    public static String filterAndTrim(String sourceStr, List<String> filters) {
        if (isEmpty(sourceStr)) {
            return EMPTY_STRING;
        }

        if (!ArrayUtils.isNullOrEmpty(filters)) {
            for (String f : filters) {
                while (sourceStr.contains(f)) {
                    sourceStr = sourceStr.replace(f, "");
                }
            }
        }

        return sourceStr.trim();
    }

    public static String[] splitAndTrim(String str, String splitBy) {
        String[] props = str.split(splitBy);
        for (int i = 0, len = props.length; i < len; i++) {
            props[i] = props[i].trim();
        }
        return props;
    }

    public static int getInt(String reviewStr) {

        if (isEmpty(reviewStr)) {
            return 0;
        }

        if (NumberUtils.isDigits(reviewStr)) {
            return Integer.parseInt(reviewStr);
        }

        return 0;
    }

    public static boolean isEqual(Object s1, Object s2) {
        if (s1 == s2) {
            return true;
        }

        if (s1 != null && s1.equals(s2)) {
            return true;
        } else {
            return false;
        }
    }

    public static int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }

    /**
     * 返回对第一个字符串的匹配度
     * 匹配度 = 共同的单词数 / s1单词数
     * 范围: 0~1
     *
     * @param s1
     * @param s2
     */
    public static float wordMatchD(String s1, String s2) {
        String[] ss1 = s1.toLowerCase().trim().split(" ");
        String[] ss2 = s2.toLowerCase().trim().split(" ");

        return wordsMatchD(ss1, ss2);
    }

    public static float wordsMatchD(String[] ss1, String[] ss2) {
        int ls1 = ss1.length;
        int ls2 = ss2.length;

        if (ls1 <= 0 || ls2 <= 0) {
            return 0;
        }

        String[] ss = null;
        List<String> wlist = null;
        if (ls1 <= ls2) {
            ss = ss1;
            wlist = Arrays.asList(ss2);
        } else {
            ss = ss2;
            wlist = Arrays.asList(ss1);
        }

        int mc = 0;
        for (String s : ss) {
            if (wlist.contains(s)) {
                mc++;
            }
        }

        return (Float.valueOf(mc) / ls1 + Float.valueOf(mc) / ls2) / 2;
    }

    public static String getCleanChars(String title) {
        if (StringUtils.isEmpty(title)) {
            return EMPTY_STRING;
        }
        int len = title.length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char c = title.charAt(i);
            if ((c > 'a' && c < 'z') || (c > 'A' && c < 'Z') || (c > '0' && c < '9')) {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase();
    }

    public static String captureTitle(String title) {
        title = title.replaceAll("\\s+", " ");
        String[] words = title.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(captureName(w)).append(" ");
        }
        return sb.toString().trim();
    }

    // 首字母大写
    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        if (cs[0] < 'a' || cs[0] > 'z') {
            return String.valueOf(cs);
        }
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 判断俩个字符串的首单词是否相同
     *
     * @param s1
     * @param s2
     * @return
     */
    public static boolean matchedFirstWord(String s1, String s2) {

        if (s1 == null || s2 == null) {
            return false;
        }

        if (s1 == s2) {
            return false;
        }

        s2 = StringUtils.toLowerCase(s2);
        s1 = StringUtils.toLowerCase(s1);

        //匹配第一个单词
        String[] subStr1 = s2.split(" ");
        String[] subStr2 = s1.split(" ");

        if (StringUtils.isEqual(subStr1[0], subStr2[0])) {
            return true;
        } else {
            return false;
        }
    }

    public static String toLowerCase(String q) {
        if (isEmpty(q) || isEmpty(q.trim())) {
            return EMPTY_STRING;
        }

        return q.trim().toLowerCase();
    }

    /**
     * q :
     * 全部转为小写
     * 去除两端空格
     */
    public static String getSearchKey(String q) {
        return toLowerCase(q);
    }
}
