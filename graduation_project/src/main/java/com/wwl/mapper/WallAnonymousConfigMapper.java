package com.wwl.mapper;

import com.wwl.model.entity.WallAnonymousConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 鍖垮悕閰嶇疆 Mapper鎺ュ彛
 * 鍘熺敓MyBatis锛屾棤MyBatis-Plus渚濊禆
 * 鎻愪緵瀹屾暣CRUD鎿嶄綔
 */
@Mapper
public interface WallAnonymousConfigMapper {

    /**
     * 鏂板涓?鏉″尶鍚嶉厤缃?
     * @param config 鍖垮悕閰嶇疆瀹炰綋
     * @return 鍙楀奖鍝嶈鏁?
     */
    int insertAnonymousConfig(WallAnonymousConfig config);

    /**
     * 鏍规嵁涓婚敭ID淇敼鍖垮悕閰嶇疆
     * @param config 寰呬慨鏀圭殑瀹炰綋鏁版嵁
     * @return 鍙楀奖鍝嶈鏁?
     */
    int updateAnonymousConfig(WallAnonymousConfig config);

    /**
     * 閫昏緫鍒犻櫎鍖垮悕閰嶇疆
     * @param id 閰嶇疆涓婚敭ID
     * @return 鍙楀奖鍝嶈鏁?
     */
    int deleteAnonymousConfigById(@Param("id") Long id);

    /**
     * 鏍规嵁ID鏌ヨ鍗曟潯鍖垮悕閰嶇疆
     * @param id 閰嶇疆涓婚敭ID
     * @return 鍖垮悕閰嶇疆瀹炰綋
     */
    WallAnonymousConfig selectAnonymousConfigById(@Param("id") Long id);

    /**
     * 鏌ヨ鎵?鏈夋甯告湭鍒犻櫎鐨勫尶鍚嶉厤缃垪琛?
     * 鐢ㄤ簬甯栧瓙銆佽瘎璁哄尶鍚嶉殢鏈哄彇鍊?
     * @return 鍖垮悕閰嶇疆闆嗗悎
     */
    List<WallAnonymousConfig> selectAllNormalAnonymousConfig();

}
