package com.wwl.mapper;

import com.wwl.model.entity.OrderCategory;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 璁㈠崟鍒嗙被鎸佷箙灞傛帴鍙?
 */
@Mapper
public interface OrderCategoryMapper {

    /**
     * 鏌ヨ鎵?鏈変竴绾у垎绫?
     * @return 涓?绾у垎绫婚泦鍚?
     */
    List<OrderCategory> selectFirstCategory();

    /**
     * 鏍规嵁鐖剁骇id鏌ヨ涓嬬骇瀛愬垎绫?
     * @param parentId 鐖跺垎绫籭d
     * @return 涓嬬骇瀛愬垎绫婚泦鍚?
     */
    List<OrderCategory> selectChildByParentId(Integer parentId);

    /**
     * 鏂板鍒嗙被鏁版嵁
     * @param category 鍒嗙被瀵硅薄
     * @return 鍙楀奖鍝嶈鏁?
     */
    int insertCategory(OrderCategory category);

    /**
     * 淇敼鍒嗙被淇℃伅
     * @param category 鍒嗙被瀵硅薄
     * @return 鍙楀奖鍝嶈鏁?
     */
    int updateCategory(OrderCategory category);

    /**
     * 鏍规嵁id鍒犻櫎鍒嗙被
     * @param id 鍒嗙被id
     * @return 鍙楀奖鍝嶈鏁?
     */
    int deleteCategory(Integer id);
}
