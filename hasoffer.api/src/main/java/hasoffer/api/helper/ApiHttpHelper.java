package hasoffer.api.helper;

import hasoffer.core.utils.ConstantUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * Created by hs on 2016/7/4.
 */
public class ApiHttpHelper {
    public static void sendJsonMessage(String message, HttpServletResponse response) {
        PrintWriter out = null;
        response.setCharacterEncoding(ConstantUtil.API_NAME_VARIABLE_ENCODE_UTF8);
        response.setContentType("application/json");
        try {
            out = response.getWriter();
            out.println(message);
        } catch (Exception ee) {
        } finally {
            out.close();
        }
    }
}
