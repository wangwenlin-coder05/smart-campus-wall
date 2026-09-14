package com.wwl.common.util;

import com.wwl.model.entity.WallAnonymousConfig;
import java.util.List;
import java.util.Random;

/**
 * 通用匿名处理工具�?
 * 支持所有含�?isAnonymous、nickname、avatar 属性的实体
 * 适配后续任意类型匿名场景扩展
 */
public class AnonymousUtil {

    // 全局随机对象
    private static final Random RANDOM = new Random();
    // 默认兜底匿名昵称
    private static final String DEFAULT_NICK = "匿名用户";

    /**
     * 通用匿名赋值方�?
     * @param isAnonymous 是否匿名标识 1=匿名
     * @param targetNick 需要覆盖的昵称字段
     * @param targetAvatar 需要覆盖的头像字段
     * @param configList 匿名配置列表
     * @return 处理后的昵称
     */
    public static String[] handleAnonymous(Integer isAnonymous, String targetNick, String targetAvatar,
                                           List<WallAnonymousConfig> configList) {
        // 非匿名直接返回原有昵称头?
        if (isAnonymous == null || isAnonymous != 1) {
            return new String[]{targetNick, targetAvatar};
        }

        // 匿名状态，随机赋?
        if (configList != null && !configList.isEmpty()) {
            int index = RANDOM.nextInt(configList.size());
            WallAnonymousConfig config = configList.get(index);
            return new String[]{config.getAnonymousNickname(), config.getAnonymousAvatar()};
        }
        // 配置为空兜底
        return new String[]{DEFAULT_NICK, null};
    }
}
