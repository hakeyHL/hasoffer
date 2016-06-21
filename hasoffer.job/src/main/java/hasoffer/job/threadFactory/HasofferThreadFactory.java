package hasoffer.job.threadFactory;

import java.util.concurrent.ThreadFactory;

public class HasofferThreadFactory implements ThreadFactory {

    private String name;
    private int thread_no;

    public HasofferThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, name + "-" + (thread_no++));
    }
}