package com.wwl.common.util;

import java.security.SecureRandom;

/**
 * 订单号生成工具类
 * 生成格式：前缀 + 10位秒级时间戳 + 5位随机数
 * 示例：DD174734123401234
 */
public class OrderNoGenerator {

    // 默认订单号前缀
    private static final String ORDER_PREFIX = "DD";
    // 安全随机数对象
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成默认前缀订单号
     * 格式：DD + 10位秒级时间戳 + 5位随机数
     * @return 完整订单号
     */
    public static String generate() {
        // 获取秒级时间戳
        long timestamp = System.currentTimeMillis() / 1000;
        // 生成0-99999随机数
        int randomNum = secureRandom.nextInt(100000);
        // 不足5位自动补0拼接
        return String.format("%s%d%05d", ORDER_PREFIX, timestamp, randomNum);
    }

    /**
     * 自定义前缀生成订单号
     * @param prefix 自定义前缀 例：DD跑腿、TK电影票、KH客户
     * @return 拼接完成订单号
     */
    public static String generateWithPrefix(String prefix) {
        long timestamp = System.currentTimeMillis() / 1000;
        int randomNum = secureRandom.nextInt(100000);
        return String.format("%s%d%05d", prefix, timestamp, randomNum);
    }
}
