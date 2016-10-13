package hasoffer.core.test;

import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 2016/10/9.
 */
public class QueueTest {

    @Test
    public void test() throws Exception {

        final ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
        final ConcurrentLinkedQueue testQueue = new ConcurrentLinkedQueue();
//
        queue.add(1);
        queue.add(2);
        queue.add(3);
        queue.add(4);
        queue.add(5);
        queue.add(6);
        queue.add(7);
        queue.add(8);
        queue.add(9);
        queue.add(10);

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < 3; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Object poll = queue.poll();

                    if (poll != null) {
                        testQueue.add(queue.poll());
                    }
                }
            });
        }


//        for (int i = 0; i < 3; i++) {
//            executorService.execute(new Runnable() {
//                @Override
//                public void run() {
//                    while (true) {
//
//                        Object poll = queue.poll();
//                        if (poll == null) {
//                            continue;
//                        } else {
//                            System.out.println(Thread.currentThread().getName() + "--------" + (Integer) poll);
//                            try {
//                                Thread.sleep(10);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            queue.add(poll);
//                            System.out.println(Thread.currentThread().getName() + "----back----" + (Integer) poll);
//                            try {
//                                Thread.sleep(10);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    }
//                }
//            });
//        }

        while (true) {

        }

//        while (!queue.isEmpty()) {
//
//            Integer poll = (Integer) queue.poll();
//            System.out.println(poll);
//            queue.add(poll);
//
//        }

    }

}
