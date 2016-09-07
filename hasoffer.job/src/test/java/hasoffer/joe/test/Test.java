package hasoffer.joe.test;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;

public class Test {

    public static void main(String[] args) {
        //for (int i = 1; ; i++) {
        //    System.out.println(i);
        //}
        String timeStr= DateFormatUtils.format(new Date(),"HH");
        System.out.println(timeStr);
    }
}
