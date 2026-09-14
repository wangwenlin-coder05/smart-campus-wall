package com.wwl.common.util;

import org.springframework.beans.BeanUtils;

/**
 * 閫氱敤瀵硅薄杞崲宸ュ叿绫? * 鎻愪緵Entity銆丏TO銆乂O涔嬮棿鐨勭浉浜掕浆鎹㈡柟娉? * 浣跨敤BeanUtils鑷姩鎷疯礉鍚屽悕瀛楁锛岀嫭鏈夊瓧娈垫墜鍔ㄨ祴鍊? */
public class ModelConverter {

    /**
     * DTO杞珽ntity
     * 鐢ㄤ簬鎺ユ敹鍓嶇璇锋眰鍚庯紝杞崲涓烘暟鎹簱瀹炰綋杩涜瀛樺偍
     *
     * @param dto 璇锋眰鍙傛暟DTO
     * @param entityClass 鐩爣Entity绫?     * @return 鏁版嵁搴撳疄浣揈ntity
     */
    public static <T> T dtoToEntity(Object dto, Class<T> entityClass) {
        if (dto == null) {
            return null;
        }
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(dto, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("DTO杞珽ntity澶辫触", e);
        }
    }

    /**
     * Entity杞琕O
     * 鐢ㄤ簬鏁版嵁搴撴煡璇㈠悗锛岃浆鎹负瑙嗗浘瀵硅薄杩斿洖鍓嶇
     *
     * @param entity 鏁版嵁搴撳疄浣揈ntity
     * @param voClass 鐩爣VO绫?     * @return 瑙嗗浘瀵硅薄VO
     */
    public static <T> T entityToVo(Object entity, Class<T> voClass) {
        if (entity == null) {
            return null;
        }
        try {
            T vo = voClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        } catch (Exception e) {
            throw new RuntimeException("Entity杞琕O澶辫触", e);
        }
    }

    /**
     * Entity鍒楄〃杞琕O鍒楄〃
     *
     * @param entities Entity鍒楄〃
     * @param voClass 鐩爣VO绫?     * @return VO鍒楄〃
     */
    public static <T> java.util.List<T> entityListToVoList(java.util.List<?> entities, Class<T> voClass) {
        if (entities == null || entities.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        java.util.List<T> voList = new java.util.ArrayList<>();
        for (Object entity : entities) {
            voList.add(entityToVo(entity, voClass));
        }
        return voList;
    }
}
