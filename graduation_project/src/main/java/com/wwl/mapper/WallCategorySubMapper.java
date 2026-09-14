package com.wwl.mapper;

import com.wwl.model.entity.WallCategorySub;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 琛ㄧ櫧澧欎簩绾у瓙鍒嗙被 Mapper
 */
@Mapper
public interface WallCategorySubMapper {

    /**
     * 鏉′欢鏌ヨ浜岀骇鍒嗙被
     * @param categoryId 涓?绾у垎绫籌D
     * @param name 瀛愬垎绫诲悕绉?
     * @param isShow 鏄惁灞曠ず
     */
    List<WallCategorySub> selectSubList(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            @Param("isShow") Integer isShow
    );

    /**
     * 鏍规嵁ID鏌ヨ浜岀骇鍒嗙被
     */
    WallCategorySub selectSubById(@Param("id") Long id);

    /**
     * 鏂板浜岀骇鍒嗙被
     */
    int insertSub(WallCategorySub entity);

    /**
     * 淇敼浜岀骇鍒嗙被
     */
    int updateSub(WallCategorySub entity);

    /**
     * 閫昏緫鍒犻櫎浜岀骇鍒嗙被
     */
    int logicDeleteSub(@Param("id") Long id);
}
