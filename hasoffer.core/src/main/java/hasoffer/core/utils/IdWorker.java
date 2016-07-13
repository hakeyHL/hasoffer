package hasoffer.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public class IdWorker {
    private static final long twepoch = 1463464402094L;//永远不要更新这个这个值   NEVER UPDATE THIS VALUE
    private static final String IDWORKER_CONFIG = "IDWORKER_CONFIG";
    private static Logger logger = LoggerFactory.getLogger(IdWorker.class);
    private static String IDWORKER_CONFIG_VALUE;

    static {
        //获取环境变量
        IDWORKER_CONFIG_VALUE = System.getenv(IDWORKER_CONFIG);
        //判断环境变量是否合法
        if (!configVerify(IDWORKER_CONFIG_VALUE)) {
            logger.warn("无效的IDWORKER_CONFIG，使用默认值1_1");
            IDWORKER_CONFIG_VALUE = "1_1";
        }
    }

    private long workerId;
    private long datacenterId;
    //毫秒级内序列号
    private long sequence = 0L;
    //起始纪元时间(2016-05-17 13:53:22)，时间标识 = 当前时间-此时间
    //工作机器占用bit数
    private long workerIdBits = 6L;
    //处理中心占用bit数
    private long datacenterIdBits = 3L;
    //最大工作机器ID 111111 -> 64
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    //最大处理中心ID 111 -> 8
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    //毫秒级内序列号占用bit数
    private long sequenceBits = 12L;
    //工作机器ID左偏移量 12
    private long workerIdShift = sequenceBits;
    //处理中心ID左偏移量 12+6=18
    private long datacenterIdShift = sequenceBits + workerIdBits;
    //时间标识左偏移量 12+6+3=21
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    //毫秒级内序列号掩码（最大值）
    private long sequenceMask = -1L ^ (-1L << sequenceBits);
    //上次生成ID的时间标记
    private long lastTimestamp = -1L;

    /**
     * 创建ID生成器
     */
    public IdWorker() {
        String[] configArgs = IDWORKER_CONFIG_VALUE.split("_");
        long sysDatacenterId = String2long(configArgs[0]);
        long sysWorkerId = String2long(configArgs[1]);
        // 验证输入的工作机器ID是否合法
        if (sysWorkerId > maxWorkerId || sysWorkerId < 0) {
            throw new IllegalArgumentException(
                    String.format("工作机器ID不能大于%d或者小于0!", maxWorkerId));
        }
        //验证输入的处理中心ID是否合法
        if (sysDatacenterId > maxDatacenterId || sysDatacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("处理中心ID不能大于%d或者小于0!", maxDatacenterId));
        }
        this.workerId = sysWorkerId;
        this.datacenterId = sysDatacenterId;
    }

    /**
     * 验证环境变量的值是否符合要求
     *
     * @param environmentVar 环境变量的值
     * @return 环境变量是否合法，合法返回true,非法返回false
     */
    protected static boolean configVerify(String environmentVar) {
        logger.info("IDWORKER_CONFIG=" + environmentVar);
        if (StringUtils.isEmpty(environmentVar)) {
            return false;
        }
        String reg = "^[0-7]_[0-9]{1,2}$";
        Pattern pattern = Pattern.compile(reg);
        return pattern.matcher(environmentVar).matches();
    }

    /**
     * 获取全局自增唯一ID
     *
     * @return 全局自增唯一ID
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String
                    .format("时间被调回，导致上次生成ID时间大于本次时间，不能生成ID，两次相差%d毫秒！",
                            lastTimestamp - timestamp));
        }

        //同一毫秒级内操作
        if (lastTimestamp == timestamp) {
            //毫秒级内序列号进行+1操作,并验证
            sequence = (sequence + 1) & sequenceMask;
            //当前毫秒内，序列号已用完（1111 1111 1111 -> 4096）
            if (sequence == 0) {
                //等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            //非同一毫秒级内，理论上计数器归零
            //sequence = 0;
            //但为保证尾数随机性大一些，毫秒级计数器归为0-9的随机数
            sequence = new SecureRandom().nextInt(10);
        }

        //当前生成ID时间设置为最后生成时间
        lastTimestamp = timestamp;
        //进行bit拼接
        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (
                workerId << workerIdShift) | sequence;
    }

    /**
     * 等待下一毫秒
     *
     * @param lastTimestamp 上次生成ID的时间 毫秒级
     * @return 下一毫秒时间
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取系统当前时间
     *
     * @return 当前系统时间的毫秒级值
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 字符串转long类型
     *
     * @param value long类型字符串
     * @return long类型结果
     */
    protected long String2long(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            throw new RuntimeException("配置的信息转换为long类型失败！");
        }
    }
}
