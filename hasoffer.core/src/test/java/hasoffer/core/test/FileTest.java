package hasoffer.core.test;

import jodd.io.FileUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created on 2016/6/30.
 */
public class FileTest {

    @Test
    public void test() throws IOException {

        String[] strings = FileUtil.readLines(new File("C:/Users/wing/Desktop/wing.txt"));

        for (String str : strings) {
            System.out.println(str);
        }

    }
}
