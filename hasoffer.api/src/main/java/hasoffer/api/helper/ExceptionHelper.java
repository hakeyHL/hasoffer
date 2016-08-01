package hasoffer.api.helper;

/**
 * Created by hs on 2016年08月01日.
 * Time 14:24
 */
public class ExceptionHelper {

    public static String getExceptionMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.fillInStackTrace()).append("\n");
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append("ClassName  :").append(stackTraceElement.getClassName()).append("\n");
            sb.append("MethodName :").append(stackTraceElement.getMethodName()).append("\n");
            sb.append("LineNumber :").append(stackTraceElement.getLineNumber()).append("\n");
        }
        return sb.toString();
    }
}
