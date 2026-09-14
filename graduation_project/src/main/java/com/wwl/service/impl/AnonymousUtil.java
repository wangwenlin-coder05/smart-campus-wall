package com.wwl.service.impl;

import com.wwl.model.entity.WallAnonymousConfig;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

/**
 * 閫氱敤鍖垮悕澶勭悊宸ュ叿绫?
 * 鏀寔鎵?鏈夊寘鍚?nickname / avatar / isAnonymous 灞炴?х殑瀹炰綋
 * 閫傞厤甯栧瓙銆佽瘎璁恒?佺暀瑷?銆佸姩鎬佺瓑浠绘剰鍖垮悕鍦烘櫙锛屾棤闇?閲嶅鍐欎唬鐮?
 */
public class AnonymousUtil {

    /** 鍏ㄥ眬闅忔満瀵硅薄 */
    private static final Random RANDOM = new Random();

    // ===================== 鍏ㄥ眬鍏滃簳榛樿閰嶇疆锛屼竴澶勭粺涓?淇敼 =====================
    /** 榛樿鍖垮悕鏄电О */
    public static final String DEFAULT_ANON_NICK = "鍖垮悕鐢ㄦ埛";
    /** 榛樿鍏滃簳鍖垮悕澶村儚 */
    public static final String DEFAULT_ANON_AVATAR = "https://schoolwall-wwl.oss-cn-beijing.aliyuncs.com/anonymous_active/nonymousAvatar%202.webp";

    /**
     * 閫氱敤鍖垮悕澶勭悊鏂规硶
     * 鍙瀹炰綋鍚湁 isAnonymous銆乶ickname銆乤vatar 鍗冲彲浣跨敤
     * @param obj 闇?瑕佸鐞嗗尶鍚嶇殑瀹炰綋瀵硅薄锛堝笘瀛?璇勮/鍏朵粬閮藉彲浠ワ級
     * @param configList 鏁版嵁搴撹鍙栫殑鍖垮悕閰嶇疆鍒楄〃
     */
    public static void handleAnonymous(Object obj, List<WallAnonymousConfig> configList) {
        // 绌哄璞＄洿鎺ヨ繑鍥?
        if (obj == null) {
            return;
        }

        try {
            // 1. 鑾峰彇鏄惁鍖垮悕鐨勫瓧娈靛??
            Method getIsAnonymous = getReadMethod(obj.getClass(), "isAnonymous");
            if (getIsAnonymous == null) {
                return;
            }
            Integer isAnonymous = (Integer) getIsAnonymous.invoke(obj);

            // 闈炲尶鍚嶇洿鎺ヨ烦杩?
            if (isAnonymous == null || isAnonymous != 1) {
                return;
            }

            // 2. 鍒ゅ畾浣跨敤闅忔満閰嶇疆杩樻槸鍏滃簳榛樿鍊?
            String nick;
            String avatar;
            if (configList != null && !configList.isEmpty()) {
                int index = RANDOM.nextInt(configList.size());
                WallAnonymousConfig config = configList.get(index);
                nick = config.getAnonymousNickname();
                avatar = config.getAnonymousAvatar();
            } else {
                // 閰嶇疆琛ㄤ负绌猴紝浣跨敤鍏ㄥ眬鍏滃簳甯搁噺
                nick = DEFAULT_ANON_NICK;
                avatar = DEFAULT_ANON_AVATAR;
            }

            // 3. 鍙嶅皠璁剧疆鏄电О鍜屽ご鍍?
            Method setNickname = getWriteMethod(obj.getClass(), "nickname");
            Method setAvatar = getWriteMethod(obj.getClass(), "avatar");

            if (setNickname != null) {
                setNickname.invoke(obj, nick);
            }
            if (setAvatar != null) {
                setAvatar.invoke(obj, avatar);
            }

        } catch (Exception e) {
            // 鍙嶅皠寮傚父鍏滃簳锛屼笉褰卞搷涓讳笟鍔¤繍琛?
            e.printStackTrace();
        }
    }

    /**
     * 鑾峰彇灞炴?ц鏂规硶锛坓etter锛?
     */
    private static Method getReadMethod(Class<?> clazz, String fieldName) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
        return pd.getReadMethod();
    }

    /**
     * 鑾峰彇灞炴?у啓鏂规硶锛坰etter锛?
     */
    private static Method getWriteMethod(Class<?> clazz, String fieldName) throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
        return pd.getWriteMethod();
    }
}
