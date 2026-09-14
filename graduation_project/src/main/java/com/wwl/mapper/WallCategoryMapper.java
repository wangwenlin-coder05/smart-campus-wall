package com.wwl.mapper;

import com.wwl.model.entity.WallCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 琛ㄧ櫧澧欎竴绾у垎绫?Mapper
 */
@Mapper
public interface WallCategoryMapper {

    /**
     * 鏉′欢鏌ヨ涓?绾у垎绫诲垪琛?
     * @param name 鍒嗙被鍚嶇О(妯＄硦)
     * @param isShow 鏄惁鍓嶇灞曠ず
     * @return 鍒嗙被鍒楄〃
     */
    List<WallCategory> selectCategoryList(
            @Param("name") String name,
            @Param("isShow") Integer isShow
    );

    /**
     * 鏍规嵁ID鏌ヨ鍗曚釜涓?绾у垎绫?
     * @param id 鍒嗙被ID
     */
    WallCategory selectCategoryById(@Param("id") Long id);

    /**
     * 鏂板涓?绾у垎绫?
     */
    int insertCategory(WallCategory entity);

    /**
     * 淇敼涓?绾у垎绫?
     */
    int updateCategory(WallCategory entity);

    /**
     * 閫昏緫鍒犻櫎涓?绾у垎绫?
     * @param id 鍒嗙被ID
     */
    int logicDeleteCategory(@Param("id") Long id);
}
