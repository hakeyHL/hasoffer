package hasoffer.thread;

import hasoffer.base.thread.HasofferThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    public static void main(String[] args) {
        HasofferThreadFactory factory = new HasofferThreadFactory("FetchUrlWorker");
        ExecutorService es = Executors.newCachedThreadPool(factory);

        for (int i = 0; i < 100; i++) {
            es.execute(new ThreadWorker());
        }
    }
}

