package hasoffer.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by glx on 2016/7/14.
 */
public class IdWorker {
    private static final Logger logger = LoggerFactory.getLogger(IdWorker.class);
    private static final int ID_LEN = 16;
    private static final long TWEPOCH = (new Date(2016 - 1900, 7 - 1, 22, 13, 28, 0)).getTime();//系统运行后，永远不要更新这个值   NEVER UPDATE THIS VALUE
    private static final Map<String, IdWorker> MAP = new HashMap<String, IdWorker>();

    private static final long MAX_ID = 0x7FFFFFFFFFFFFFFFL;

    private static final long MAX_SECOND = 0x7FFFFFFFL;
    private static final long MAX_MACHINE = 0xFFL;
    private static final long MAX_SN = 0xFFFFFFL;
    private final AtomicLong serialNumber = new AtomicLong(MAX_SN);//3个字�?;//四个字节
    private long seconds = (System.currentTimeMillis() - TWEPOCH) / (1000L);
    private int machine = 0;//1个字�?

    private IdWorker(byte machine) {
        if (machine > MAX_MACHINE) {
            throw new RuntimeException();
        }

        this.machine = machine;
    }

    public synchronized static IdWorker getInstance(Class<?> cls, byte machine) {
        if (MAP.get(cls.getName()) == null) {
            MAP.put(cls.getName(), new IdWorker(machine));
        }

        return MAP.get(cls.getName());
    }

    public synchronized static IdWorker getInstance(Class<?> cls) {
        if (MAP.get(cls.getName()) == null) {
            String machineStr = System.getenv("ID_WORKER");

            byte machine = 0;
            if (machineStr != null && machineStr.trim().length() > 0) {
                logger.info("获取到ID_WORKER,ID_WORKER=" + machineStr);
                machine = (byte) Integer.parseInt(machineStr);
            } else {
                logger.warn("没有发现ID_WORKER，如果部署多个实例，必须对每台机器设置不同的ID_WORKER；例如ID_WORKER=1");
            }
            MAP.put(cls.getName(), new IdWorker(machine));
        }

        return MAP.get(cls.getName());
    }

    public static Date getDate(String id) {
        long s = getSecond(id);
        return new Date(s * 1000 + TWEPOCH);
    }

    public static Date getDate(long id) {
        return getDate(Long.toHexString(id));
    }


    public static long getSecond(String id) {
        String str = len(id, ID_LEN);
        return (long) Long.parseLong(str.substring(0, 8), 16);
    }

    public static long getSecond(long id) {
        return getSecond(Long.toHexString(id));
    }

    public static byte getMachine(String id) {
        String str = len(id, ID_LEN);
        return (byte) Integer.parseInt(str.substring(8, 10), 16);
    }

    public static byte getMachine(long id) {
        return getMachine(Long.toHexString(id));
    }


    public static long getSn(String id) {
        String str = len(id, ID_LEN);
        return (long) Long.parseLong(str.substring(10, 16), 16);
    }

    public static long getSn(long id) {
        return getSn(Long.toHexString(id));
    }

    private static String len(String str, int len) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < len - str.length(); i++) {
            stringBuffer.append("0");
        }

        return stringBuffer.toString() + str;
    }

    public static void main(String[] args) {
        final IdWorker stringIdWorker = IdWorker.getInstance(IdWorker.class);
        long t = (System.currentTimeMillis());

        for (int i = 0; i < 1000; i++) {
            long id = stringIdWorker.nextLong();


            System.out.println(getDate(id));
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                   String str =  stringIdWorker.next();
                    System.out.println(str);

                }
            }).start();*/
        }
        System.out.println(System.currentTimeMillis() - t);


    }

    public long nextLong() {
        return Long.parseLong(nextString(), 16);
    }

    public synchronized String nextString() {
        long nowSecond = (System.currentTimeMillis() - TWEPOCH) / (1000L);
        if (nowSecond > MAX_SECOND) {
            throw new RuntimeException();
        }

        long sn = 0;
        if (seconds == nowSecond) {
            sn = serialNumber.addAndGet(1);
            if (sn > MAX_SN) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return nextString();
            }
        } else {
            serialNumber.set(0);
            seconds = nowSecond;
            sn = serialNumber.addAndGet(1);
        }

        String daysHex = len(Long.toHexString(seconds), 8);

        String machineHex = len(Integer.toHexString(machine), 2);

        String snHex = len(Long.toHexString(sn), 6);

        String ret = daysHex + machineHex + snHex;

        return ret.toUpperCase();
    }
}
