package per.nonlone.utilsExtend;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springside.modules.utils.collection.type.ConcurrentHashSet;

import java.net.NetworkInterface;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class Bit10Generator {

    /**
     * 年份起始偏移
     */
    private static final int START_YEAR_OFFSET = 2019;

    /**
     * 16进制
     */
    private static final int HEX = 16;


    /**
     * Sequence 最大值
     */
    private static final int MAX_SEQUENCE = Double.valueOf(Math.pow(Integer.valueOf(Base62Utils.BASE_62).doubleValue(), 2d)).intValue();

    /**
     * 静态常量化 MachineId
     */
    private static final char MACHINE_ID = getMachineId();

    /**
     * 重入锁
     */
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Set<String> testSet = new ConcurrentHashSet<>();
    /**
     * 序列
     */
    private static volatile AtomicInteger sequence = new AtomicInteger(0);
    /**
     * 时间戳
     */
    private static volatile long lastTimeStamp = 0;

    /**
     * 获取62进制的时间戳，时间格式为 yMDhms，年份获取当前年份和 {@link Bit10Generator#START_YEAR_OFFSET} 进行偏移
     *
     * @return
     */
    private static String getTimeStamp(long longDateTime) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(longDateTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        StringBuilder sbOfDate = new StringBuilder();
        sbOfDate.append(convertToNS62String(localDateTime.getYear() - START_YEAR_OFFSET));
        sbOfDate.append(convertToNS62String(localDateTime.getMonthValue()));
        sbOfDate.append(convertToNS62String(localDateTime.getDayOfMonth()));
        sbOfDate.append(convertToNS62String(localDateTime.getHour()));
        sbOfDate.append(convertToNS62String(localDateTime.getMinute()));
        sbOfDate.append(convertToNS62String(localDateTime.getSecond()));
        return sbOfDate.toString();
    }

    /**
     * 转换到62位字符
     *
     * @param value
     * @return
     */
    private static String convertToNS62String(int value) {
        if (value < 0 || value > Base62Utils.BASE_62) {
            return "";
        }
        return Base62Utils.numberTo62Char(value);
    }

    /**
     * 获取机器Id，通过获取物理网卡的 MAC 地址，形如 55-73-84-1e-a5-c3，
     * 以每两个十六进制数位为一组，共6组，转换成10进制之后累加得到数值再转换成一位62进制字符
     *
     * @return
     */
    private static char getMachineId() {
        Set<NetworkInterface> networkInterfaceSet = NetworkUtils.getPhysicalNetworkInterfaceCardSet();
        int totalMacAddressTo10 = 0;
        for (NetworkInterface networkInterface : networkInterfaceSet) {
            String macAddress = NetworkUtils.getMacAddress(networkInterface, "-");
            String[] macAddressBits = macAddress.split("-");
            // 累加Mac地址每两个十六进制数值
            int macAddressTo10 = 0;
            for (String macAddressBit : macAddressBits) {
                macAddressTo10 += Integer.parseInt(macAddressBit, HEX);
            }
            totalMacAddressTo10 += macAddressTo10;
        }
        return convertToNS62(downToNS62NotZero(totalMacAddressTo10));
    }

    /**
     * 数字压缩到62内，对value进行61求余数，得到结果之后+1偏移，保证值不为0
     *
     * @param value
     * @return
     */
    private static int downToNS62NotZero(int value) {
        if (value < 0) {
            return 0;
        }
        return (value % (Base62Utils.BASE_62 - 1)) + 1;
    }

    /**
     * 转换到1位62进制
     *
     * @param value
     * @return
     */
    private static char convertToNS62(long value) {
        if (value < 0) {
            return '0';
        }
        return Base62Utils.numberTo62Char(value % Base62Utils.BASE_62).charAt(0);
    }

    /**
     * 获取应用Id，转换成1位62进制
     *
     * @param appId
     * @return
     */
    private static char getAppId(String appId) {
        return convertToNS62(Base62Utils.char62ToNumber(appId));
    }

    public static String nextId() {
        return nextId(null);
    }

    /**
     * 生成序列Id
     *
     * @param appId
     * @return
     */
    public static String nextId(String appId) {
        String id = null;
        do {
            id = doNextId(appId);
        } while (StringUtils.isBlank(id));
        return id;
    }

    /**
     * 真正生成序列Id
     *
     * @param appId
     * @return
     */
    protected static String doNextId(String appId) {
        lock.lock();
        long currentTimeMillis = System.currentTimeMillis();
        long currentTimeStamp = currentTimeMillis / 1000;
        int testInt = 0;
        try {
            if (lastTimeStamp > currentTimeStamp) {
                // 时间同步导致回拨问题
                log.error("Clock moved backwards.  Refusing to generate id, currentTimestamp={}, lastTimestamp={}", currentTimeStamp, lastTimeStamp);
                throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
            } else if (lastTimeStamp == currentTimeStamp) {
                sequence.getAndIncrement();
                // 出现碰撞
                if (sequence.get() >= MAX_SEQUENCE - 1) {
                    // 循环空转
                    currentTimeMillis = System.currentTimeMillis();
                    currentTimeStamp = currentTimeMillis / 1000;
                    while (currentTimeStamp <= lastTimeStamp) {
                        currentTimeMillis = System.currentTimeMillis();
                        currentTimeStamp = currentTimeMillis / 1000;
                    }
                    // 空转结束
                    sequence.compareAndSet(MAX_SEQUENCE, 0);
                }
            } else {
                // 没有出现碰撞
                sequence.set(0);
            }
            lastTimeStamp = currentTimeStamp;
            // 拼接返回发号器
            return String.valueOf(MACHINE_ID) + getAppId(appId) + convertToNS62LeftPadding(sequence.get(), 2) + getTimeStamp(currentTimeMillis);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 转换十进制到62进制，左部不补齐
     *
     * @param value
     * @param size
     * @return
     */
    private static String convertToNS62LeftPadding(long value, int size) {
        String ns62 = Base62Utils.numberTo62Char(value);
        if (ns62.length() > size) {
            return ns62.substring(0, size);
        } else {
            return StringUtils.leftPad(ns62, size, '0');
        }
    }
}
