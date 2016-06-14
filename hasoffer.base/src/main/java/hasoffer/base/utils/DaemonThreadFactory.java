package hasoffer.base.utils;

/**
 * Date : 2016/1/14
 * Function :
 */
public class DaemonThreadFactory {

    public static Thread create(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    }

}
