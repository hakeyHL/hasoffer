package hasoffer.core.test.basetest;

import hasoffer.core.utils.IdWorker;
import org.junit.Test;

/**
 * Created by chevy on 2016/7/13.
 */
public class IDTest {

    @Test
    public void f() {
        IdWorker idWorker = new IdWorker();
        System.out.println(idWorker.nextId());
    }

}
